package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.Gdx;
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

/**
 * Main gameplay screen that updates and renders all game systems.
 */
public class GameScreen extends ScreenAdapter {

    private static final float WORLD_WIDTH  = 1280f;
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

    private int fitX, fitY, fitW, fitH;
    private int gameplayScissorX, gameplayScissorW;
    private int uiScissorX, uiScissorW;

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

        backgroundLayer = new BackgroundLayer(GAMEPLAY_WORLD_WIDTH, WORLD_HEIGHT, BACKGROUND_SCROLL_SPEED);
        backgroundLayer.load();

        player = new Player(GAMEPLAY_WORLD_WIDTH / 2f - PLAYER_HITBOX_HALF_W, WORLD_HEIGHT/2f - PLAYER_HITBOX_HALF_H, GAMEPLAY_WORLD_WIDTH, WORLD_HEIGHT);

        EnemyAnimRegistry.load();

        BulletPool.clear();
        EnemyPool.clear();

        waveManager = new WaveManager(LevelData.build());
        waveManager.startLevel(START_LEVEL);
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
        EnemyAnimRegistry.dispose();
        BulletPool.clear();
        EnemyPool.clear();
    }

    private void update(float delta) {
        player.update(delta);
        backgroundLayer.update(delta, player.getScrollMultiplier());

        // Wave machine: may push new enemies into EnemyPool this frame
        waveManager.update(delta);

        // Move enemies; each enemy may also push bullets into BulletPool
        EnemyPool.update(delta, player);

        // Move bullets; evicts any that went off-screen or were pre-marked
        BulletPool.update(delta, GAMEPLAY_WORLD_WIDTH, WORLD_HEIGHT);

        // Damage resolution — must run after both pools have moved
        handleCollisions();

        //If player is still alive, and there are no more enemies, you win!
        if (player.isAlive() && waveManager.isComplete()) {
            main.setScreen(new VictoryScreen(main));
        }

        //If player died go to game over.
        if(!player.isAlive()) {
            main.setScreen(new LossScreen(main));
        }
    }

    /**
     * Resolves hits between bullets and their opposing faction.
     * A bullet is marked for removal the instant a hit is detected, so it
     * can only damage one target per frame.  The pool evicts it at the top
     * of the next BulletPool.update() call
     */
    private void handleCollisions() {
        Bullet[] bullets = BulletPool.getSlots();
        ArrayList<Enemy> enemies = EnemyPool.getActive();

        for (int i = 0; i < BulletPool.CAPACITY; i++) {
            Bullet b = bullets[i];
            if (b == null || b.isMarkedForRemoval()) continue;

            if (b.getFaction() == Faction.ENEMY) {
                //nemy bullet vs player
                if (b.rect.overlaps(player.rect)) {
                    player.onBulletHit(b);
                    b.markForRemoval();
                }

            } else {
                //player bullet vs first enemy it overlaps
                for (int j = 0; j < enemies.size(); j++) {
                    Enemy e = enemies.get(j);
                    if (!e.isMarkedForRemoval() && e.isAlive() && b.rect.overlaps(e.rect)) {
                        e.onBulletHit(b);
                        b.markForRemoval();
                        break; // one bullet, one target
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

        //gameplay panel
        Gdx.gl.glScissor(gameplayScissorX, fitY, gameplayScissorW, fitH);
        backgroundLayer.renderBackground(batch);

        batch.begin();
        BulletPool.draw(batch); // bullets rendered behind everything
        EnemyPool.draw(batch); // enemies
        player.draw(batch); // player on top
        batch.end();

        /*
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        player.drawDebug(shapeRenderer, Color.RED);
        for (Enemy e : EnemyPool.getActive()) {
            e.drawDebug(shapeRenderer, Color.LIME);
        }
        shapeRenderer.end();
        */

        backgroundLayer.renderForeground(batch);

        //UI panel
        Gdx.gl.glScissor(uiScissorX, fitY, uiScissorW, fitH);
        drawUiBackground();
        drawUiContent();

        //divider
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

        batch.begin();
        font.draw(batch, "SCORE", textX, topLineY);
        font.draw(batch, "0", textX, topLineY - UI_LINE_HEIGHT);
        font.draw(batch, "LIVES", textX, topLineY - UI_LINE_HEIGHT * 3f);
        font.draw(batch, currentHp + " / " + maxHp, textX, topLineY - UI_LINE_HEIGHT * 4f);
        font.draw(batch, "SPIRIT POWER", textX, topLineY - UI_LINE_HEIGHT * 6f);
        font.draw(batch, "1", textX, topLineY - UI_LINE_HEIGHT * 7f);
        font.draw(batch, "SWORD POWER",  textX, topLineY - UI_LINE_HEIGHT * 8f);
        font.draw(batch, "1", textX, topLineY - UI_LINE_HEIGHT * 9f);
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
