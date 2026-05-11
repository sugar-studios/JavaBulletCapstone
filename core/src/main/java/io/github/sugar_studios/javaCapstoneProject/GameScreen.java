package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Main gameplay screen. Owns the game loop, all rendering, and collision resolution.
public class GameScreen extends ScreenAdapter {

    private static final float WORLD_WIDTH = 1280f;
    private static final float WORLD_HEIGHT = 720f;

    private static final float GAMEPLAY_FRACTION = 2f / 3f;
    private static final float UI_FRACTION = 1f / 3f;
    private static final float GAMEPLAY_WORLD_WIDTH = WORLD_WIDTH * GAMEPLAY_FRACTION;
    private static final float UI_WORLD_WIDTH = WORLD_WIDTH * UI_FRACTION;
    private static final float UI_WORLD_X = GAMEPLAY_WORLD_WIDTH;

    private static final float BACKGROUND_SCROLL_SPEED = 750f;

    private static final float UI_PADDING = 24f;
    private static final float UI_LINE_HEIGHT = 50f;

    private static final Color UI_BG_COLOR = Color.BLACK;
    private static final Color UI_TEXT_COLOR = Color.WHITE;
    private static final Color DIVIDER_COLOR = new Color(0.8f, 0.8f, 0.8f, 1f);
    private static final float DIVIDER_WIDTH = 3f;

    private static final float PLAYER_HITBOX_HALF_W = 6f;
    private static final float PLAYER_HITBOX_HALF_H = 6f;

    private static final int START_LEVEL = 1;

    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Main main;

    private FitViewport viewport;
    private Texture whitePixel;
    private BackgroundLayer backgroundLayer;
    private Player player;
    private WaveManager waveManager;
    private Music music;

    private int fitX, fitY, fitW, fitH;
    private int gameplayScissorX, gameplayScissorW;
    private int uiScissorX, uiScissorW;

    private final List<Token> activeTokens = new ArrayList<>();
    private TokenSpawner tokenSpawner;
    private final java.util.Set<Enemy> slashHitEnemies = new java.util.HashSet<>();

    private static int score = 0;

    public static void addScore(int amount) { score += amount; }

    public GameScreen(Main main) {
        this.batch = (SpriteBatch) main.getBatch();
        this.shapeRenderer = main.getShapeRenderer();
        this.font = main.getFontEN();
        this.main = main;
    }

    @Override
    public void show() {
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        whitePixel = createWhitePixel();
        font.setColor(UI_TEXT_COLOR);

        BackgroundConfig bgConfig = LevelData.getBackgroundConfig(START_LEVEL);
        backgroundLayer = new BackgroundLayer(GAMEPLAY_WORLD_WIDTH, WORLD_HEIGHT, BACKGROUND_SCROLL_SPEED, bgConfig);
        backgroundLayer.load();

        music = Gdx.audio.newMusic(Gdx.files.internal(bgConfig.musicPath));
        music.setLooping(true);
        music.setVolume(0.25f);
        music.play();

        player = new Player(
            GAMEPLAY_WORLD_WIDTH / 2f - PLAYER_HITBOX_HALF_W,
            WORLD_HEIGHT / 2f - PLAYER_HITBOX_HALF_H,
            GAMEPLAY_WORLD_WIDTH, WORLD_HEIGHT);

        EnemyAnimRegistry.load();

        BulletPool.clear();
        EnemyPool.clear();

        waveManager = new WaveManager(LevelData.build());
        waveManager.startLevel(START_LEVEL);

        loadTokenAssets();
        SfxPlayer.load();
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        fitX = viewport.getScreenX();
        fitY = viewport.getScreenY();
        fitW = viewport.getScreenWidth();
        fitH = viewport.getScreenHeight();

        gameplayScissorX = fitX;
        gameplayScissorW = Math.round(fitW * GAMEPLAY_FRACTION);
        uiScissorX = fitX + gameplayScissorW;
        uiScissorW = fitW - gameplayScissorW;
    }

    @Override
    public void dispose() {
        if (whitePixel != null) whitePixel.dispose();
        if (backgroundLayer != null) backgroundLayer.dispose();
        if (player != null) player.dispose();
        if (music != null) { music.stop(); music.dispose(); }
        EnemyAnimRegistry.dispose();
        BulletPool.clear();
        EnemyPool.clear();
        if (tokenSpawner != null) tokenSpawner.dispose();
        SfxPlayer.dispose();
    }

    private void loadTokenAssets() {
        tokenSpawner = new TokenSpawner(
            new Texture(Gdx.files.internal(Token.TEXTURE_BLUE_T1)),
            new Texture(Gdx.files.internal(Token.TEXTURE_BLUE_T2)),
            new Texture(Gdx.files.internal(Token.TEXTURE_BLUE_T3)),
            new Texture(Gdx.files.internal(Token.TEXTURE_RED_T1)),
            new Texture(Gdx.files.internal(Token.TEXTURE_RED_T2)),
            new Texture(Gdx.files.internal(Token.TEXTURE_RED_T3)),
            new Texture(Gdx.files.internal(Token.TEXTURE_BLACK_T1)),
            new Texture(Gdx.files.internal(Token.TEXTURE_BLACK_T2)),
            new Texture(Gdx.files.internal(Token.TEXTURE_BLACK_T3)),
            new Texture(Gdx.files.internal(Token.TEXTURE_PURPLE))
        );
    }

    private void update(float delta) {
        player.update(delta);
        backgroundLayer.update(delta, player.getScrollMultiplier());

        waveManager.update(delta);
        EnemyPool.update(delta, player);
        BulletPool.update(delta, GAMEPLAY_WORLD_WIDTH, WORLD_HEIGHT);

        handleCollisions();

        if (player.isBombActive()) clearEnemyBullets();

        // Move tokens, resolve player contact, cull anything that has fallen off-screen.
        Iterator<Token> it = activeTokens.iterator();
        while (it.hasNext()) {
            Token t = it.next();
            t.update(delta, player);
            if (!t.isActive() || t.rect.y + t.rect.height < 0) {
                if (t.isActive()) t.deactivate();
                it.remove();
            }
        }

        if (player.isAlive() && waveManager.isComplete()) {
            music.stop();
            main.setScreen(new VictoryScreen(main, score));
        }
        if (!player.isAlive()) {
            music.stop();
            main.setScreen(new LossScreen(main, score));
            score = 0;
        }
    }

    // Marks all enemy bullets for removal — called every frame while bomb is active.
    private void clearEnemyBullets() {
        Bullet[] bullets = BulletPool.getSlots();
        for (int i = 0; i < BulletPool.CAPACITY; i++) {
            Bullet b = bullets[i];
            if (b != null && !b.isMarkedForRemoval() && b.getFaction() == Faction.ENEMY)
                b.markForRemoval();
        }
    }

    // One bullet hits one target per frame. Marks bullet for removal on contact,
    // then spawns tokens at the enemy's centre if the hit was lethal.
    // Also resolves sword slash: each enemy can only be hit once per swing.
    private void handleCollisions() {
        Bullet[] bullets = BulletPool.getSlots();
        ArrayList<Enemy> enemies = EnemyPool.getActive();

        // Clear slash hit set at the start of each new swing.
        if (player.isSlashJustStarted()) slashHitEnemies.clear();

        // Sword slash vs enemies — one hit per enemy per swing.
        if (player.isSlashHitboxActive()) {
            com.badlogic.gdx.math.Rectangle slashRect = player.getSlashHitbox();
            for (int j = 0; j < enemies.size(); j++) {
                Enemy e = enemies.get(j);
                if (!slashHitEnemies.contains(e) && !e.isMarkedForRemoval() && e.isAlive() && slashRect.overlaps(e.rect)) {
                    e.onSlashHit(player.getSlashDamage());
                    SfxPlayer.playSwordHit();
                    slashHitEnemies.add(e);
                    if (!e.isAlive()) {
                        SfxPlayer.playKill();
                        float cx = e.rect.x + e.rect.width * 0.5f;
                        float cy = e.rect.y + e.rect.height * 0.5f;
                        int tokenCount = (int) Math.ceil(e.getMaxHealth() * 0.25f);
                        if (tokenCount > 0) activeTokens.addAll(tokenSpawner.spawnTokens(cx, cy, tokenCount));
                    }
                }
            }
        }

        for (int i = 0; i < BulletPool.CAPACITY; i++) {
            Bullet b = bullets[i];
            if (b == null || b.isMarkedForRemoval()) continue;

            if (b.getFaction() == Faction.ENEMY) {
                if (b.rect.overlaps(player.rect)) {
                    player.onBulletHit(b);
                    b.markForRemoval();
                }
            } else {
                for (int j = 0; j < enemies.size(); j++) {
                    Enemy e = enemies.get(j);
                    if (!e.isMarkedForRemoval() && e.isAlive() && b.rect.overlaps(e.rect)) {
                        e.onBulletHit(b);
                        b.markForRemoval();
                        if (!e.isAlive()) {
                            SfxPlayer.playKill();
                            float cx = e.rect.x + e.rect.width * 0.5f;
                            float cy = e.rect.y + e.rect.height * 0.5f;
                            int tokenCount = (int) Math.ceil(e.getMaxHealth() * 0.25f);
                            if (tokenCount > 0) activeTokens.addAll(tokenSpawner.spawnTokens(cx, cy, tokenCount));
                        }
                        break;
                    }
                }
            }
        }
    }

    private void draw() {
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);

        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

        // Gameplay panel
        Gdx.gl.glScissor(gameplayScissorX, fitY, gameplayScissorW, fitH);
        backgroundLayer.renderBackground(batch);

        batch.begin();
        BulletPool.draw(batch);
        EnemyPool.draw(batch);
        for (Token t : activeTokens) t.draw(batch);
        player.draw(batch);

        batch.end();


        /*
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        player.drawDebug(shapeRenderer, Color.RED);
        player.drawSlashDebug(shapeRenderer, Color.ORANGE);
        for (Enemy e : EnemyPool.getActive()) e.drawDebug(shapeRenderer, Color.LIME);
        for (Token t : activeTokens) t.drawDebug(shapeRenderer, Color.YELLOW);
        Bullet[] bullets = BulletPool.getSlots();
        for (int i = 0; i < BulletPool.CAPACITY; i++) {
            Bullet b = bullets[i];
            if (b == null || b.isMarkedForRemoval()) continue;
            Color c = b.getFaction() == Faction.ENEMY ? Color.CYAN : Color.WHITE;
            b.drawDebug(shapeRenderer, c);
        }
        shapeRenderer.end();
        */


        backgroundLayer.renderForeground(batch);

        // UI panel
        Gdx.gl.glScissor(uiScissorX, fitY, uiScissorW, fitH);
        drawUiBackground();
        drawUiContent();

        // Divider
        Gdx.gl.glScissor(fitX, fitY, fitW, fitH);
        drawDivider();

        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
    }

    private void drawUiBackground() {
        batch.begin();
        batch.setColor(UI_BG_COLOR);
        batch.draw(whitePixel, UI_WORLD_X, 0f, UI_WORLD_WIDTH, WORLD_HEIGHT);
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void drawUiContent() {
        float textX = UI_WORLD_X + UI_PADDING;
        float topLineY = WORLD_HEIGHT - UI_PADDING;

        int currentHp = (int) player.getCurrentHealth();
        int maxHp = (int) player.getMaxHealth();

        String shotPower = String.format("%.1f", player.getShotPowerDisplay());
        String swordPower = String.format("%.1f", player.getSwordPowerDisplay());

        batch.begin();
        font.draw(batch, "SCORE", textX, topLineY);
        font.draw(batch, String.valueOf(score), textX, topLineY - UI_LINE_HEIGHT);
        font.draw(batch, "LIVES", textX, topLineY - UI_LINE_HEIGHT * 3f);
        font.draw(batch, currentHp + " / " + maxHp, textX, topLineY - UI_LINE_HEIGHT * 4f);
        font.draw(batch, "SPIRIT POWER", textX, topLineY - UI_LINE_HEIGHT * 6f);
        font.draw(batch, shotPower, textX, topLineY - UI_LINE_HEIGHT * 7f);
        font.draw(batch, "SWORD POWER", textX, topLineY - UI_LINE_HEIGHT * 8f);
        font.draw(batch, swordPower, textX, topLineY - UI_LINE_HEIGHT * 9f);
        font.draw(batch, "STAGE", textX, topLineY - UI_LINE_HEIGHT * 12f);
        font.draw(batch, "1", textX, topLineY - UI_LINE_HEIGHT * 13f);
        batch.end();
    }

    private void drawDivider() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(DIVIDER_COLOR);
        shapeRenderer.rect(GAMEPLAY_WORLD_WIDTH - DIVIDER_WIDTH / 2f, 0f, DIVIDER_WIDTH, WORLD_HEIGHT);
        shapeRenderer.end();
    }

    private static Texture createWhitePixel() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }
}
