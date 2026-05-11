package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Manages and renders every visual layer beneath or in front of
 * the core game objects (player, enemies, bullets).
 *
 * Asset paths and sky colour come from a BackgroundConfig,
 * so each level supplies its own artwork without touching this class.
 */
public class BackgroundLayer {

    // Treeline
    private static final float TREELINE_WORLD_WIDTH = 55f;
    private static final float TREELINE_SCROLL_FACTOR = 0.65f;
    private static final float TREELINE_RIGHT_Y_OFFSET = 290f;

    // Props
    private static final float MINIMUM_PROP_TIMER = 2f;
    private static final float PROP_CHANCE = 0.3f;
    private static final float PROP_EDGE_MARGIN = 24f;
    private static final float PROP_TINT_MIN = 0.45f;
    private static final float PROP_TINT_MAX = 0.75f;
    private static final float PROP_SCALE_MIN = 3.4f;
    private static final float PROP_SCALE_MAX = 7.4f;
    private static final float PROP_ALPHA = 0.35f;
    private static final float FOG_ALPHA = 0.55f;
    private static final float FOREGROUND_ALPHA = 0.5f;

    private final float gameplayWorldWidth;
    private final float worldHeight;
    private final float scrollSpeed;
    private final BackgroundConfig config;

    private final float propSpawnXMin;
    private final float propSpawnXMax;

    private Texture whitePixel;
    private Texture treelineTexture;
    private Texture fogTexture;
    private Texture foregroundTexture;
    private final Texture[] propTextures = new Texture[5];

    private final ProbabilityQueue<PropType> propQueue;
    private final List<ActiveProp> activeProps = new ArrayList<>();

    private float treelineScrollY = 0f;
    private float timeSinceLastProp = 0f;

    public BackgroundLayer(float gameplayWorldWidth, float worldHeight, float scrollSpeed, BackgroundConfig config) {
        this.gameplayWorldWidth = gameplayWorldWidth;
        this.worldHeight = worldHeight;
        this.scrollSpeed = scrollSpeed;
        this.config = config;

        propSpawnXMin = TREELINE_WORLD_WIDTH + PROP_EDGE_MARGIN;
        propSpawnXMax = gameplayWorldWidth - TREELINE_WORLD_WIDTH - PROP_EDGE_MARGIN;

        List<PropType> all = new ArrayList<>(Arrays.asList(PropType.values()));
        propQueue = new ProbabilityQueue<>(all);
    }

    public void load() {
        whitePixel = createWhitePixel();
        treelineTexture = loadTexture(config.path("treeline.png"), Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.Repeat);
        fogTexture = loadTexture(config.path("fog.png"), Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        foregroundTexture = loadTexture(config.path("foreground.png"), Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        for (int i = 0; i < 5; i++) {
            propTextures[i] = loadTexture(config.path("prop" + (i + 1) + ".png"),
                Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        }
    }

    public void update(float delta, float scrollMultiplier) {
        float effective = scrollSpeed * scrollMultiplier;
        treelineScrollY = (treelineScrollY + effective * TREELINE_SCROLL_FACTOR * delta) % worldHeight;

        Iterator<ActiveProp> it = activeProps.iterator();
        while (it.hasNext()) {
            ActiveProp prop = it.next();
            prop.y -= effective * delta;
            if (prop.y + prop.displayHeight < 0f) it.remove();
        }

        timeSinceLastProp += delta;
        if (timeSinceLastProp >= MINIMUM_PROP_TIMER && MathUtils.random() < PROP_CHANCE * delta) {
            spawnNextProp();
            timeSinceLastProp = 0f;
        }
    }

    /** Draws: sky fill → background props → fog → treelines. */
    public void renderBackground(SpriteBatch batch) {
        batch.begin();
        drawFill(batch);
        drawBackgroundProps(batch);
        drawFogProps(batch);
        drawTreelines(batch);
        batch.end();
    }

    /** Draws tiled foreground strips on top of game objects. */
    public void renderForeground(SpriteBatch batch) {
        batch.begin();
        for (ActiveProp prop : activeProps) {
            if (prop.type.isTiled()) drawTiled(batch, prop);
        }
        batch.end();
    }

    public void dispose() {
        if (whitePixel != null) whitePixel.dispose();
        if (treelineTexture != null) treelineTexture.dispose();
        if (fogTexture != null) fogTexture.dispose();
        if (foregroundTexture != null) foregroundTexture.dispose();
        for (Texture t : propTextures) if (t != null) t.dispose();
    }

    //Spawning

    private void spawnNextProp() {
        PropType type = propQueue.selectNext();
        Texture tex = textureFor(type);
        if (tex == null) return;

        float w = tex.getWidth();
        float h = tex.getHeight();
        boolean flipX = false;
        float spawnX;

        if (type.isFog()) {
            boolean left = MathUtils.randomBoolean();
            spawnX = left ? 0f : gameplayWorldWidth - w;
            flipX  = !left;
        } else if (type.isTiled()) {
            spawnX = 0f;
        } else {
            float maxX = propSpawnXMax - w;
            spawnX = (propSpawnXMin < maxX) ? MathUtils.random(propSpawnXMin, maxX) : propSpawnXMin;
        }

        boolean fg = type.isForeground();
        float scale = fg ? 1f : MathUtils.random(PROP_SCALE_MIN, PROP_SCALE_MAX);
        float tint = fg ? 1f : MathUtils.random(PROP_TINT_MIN,  PROP_TINT_MAX);

        activeProps.add(new ActiveProp(type, spawnX, worldHeight + h, flipX, scale, tint, w, h));
    }

    //Drawing

    private void drawFill(SpriteBatch batch) {
        Color c = config.color;
        batch.setColor(c.r, c.g, c.b, 1f);
        batch.draw(whitePixel, 0f, 0f, gameplayWorldWidth, worldHeight);
        batch.setColor(Color.WHITE);
    }

    private void drawBackgroundProps(SpriteBatch batch) {
        for (ActiveProp prop : activeProps) {
            if (prop.type.isForeground()) continue;
            float drawW = prop.displayWidth  * prop.scale;
            float drawH = prop.displayHeight * prop.scale;
            float drawX = prop.x + (prop.displayWidth  - drawW) / 2f;
            float drawY = prop.y + (prop.displayHeight - drawH) / 2f;
            batch.setColor(prop.tint, prop.tint, prop.tint, PROP_ALPHA);
            drawSprite(batch, textureFor(prop.type), drawX, drawY, drawW, drawH, prop.flipX);
            batch.setColor(Color.WHITE);
        }
    }

    private void drawFogProps(SpriteBatch batch) {
        for (ActiveProp prop : activeProps) {
            if (!prop.type.isFog()) continue;
            batch.setColor(1f, 1f, 1f, FOG_ALPHA);
            drawSprite(batch, fogTexture, prop.x, prop.y, prop.displayWidth, prop.displayHeight, prop.flipX);
            batch.setColor(Color.WHITE);
        }
    }

    /** Tiles the foreground texture horizontally across the full gameplay width. */
    private void drawTiled(SpriteBatch batch, ActiveProp prop) {
        if (foregroundTexture == null) return;
        batch.setColor(1f, 1f, 1f, FOREGROUND_ALPHA);
        float x = 0f;
        float tileW = prop.displayWidth * prop.scale;
        float tileH = prop.displayHeight * prop.scale;
        while (x < gameplayWorldWidth) {
            batch.draw(foregroundTexture, x, prop.y, tileW, tileH);
            x += tileW;
        }
        batch.setColor(Color.WHITE);
    }

    private void drawTreelines(SpriteBatch batch) {
        if (treelineTexture == null) return;
        float rightScrollY = (treelineScrollY + TREELINE_RIGHT_Y_OFFSET) % worldHeight;
        drawTiledTreeline(batch, 0f,                                  false, treelineScrollY);
        drawTiledTreeline(batch, gameplayWorldWidth - TREELINE_WORLD_WIDTH, true,  rightScrollY);
    }

    private void drawTiledTreeline(SpriteBatch batch, float x, boolean flipX, float scrollY) {
        for (int copy = 0; copy < 2; copy++) {
            drawSprite(batch, treelineTexture,
                x, -scrollY + copy * worldHeight,
                TREELINE_WORLD_WIDTH, worldHeight, flipX);
        }
    }

    //Helpers

    private Texture textureFor(PropType type) {
        switch (type) {
            case PROP_1:
                return propTextures[0];
            case PROP_2:
                return propTextures[1];
            case PROP_3:
                return propTextures[2];
            case PROP_4:
                return propTextures[3];
            case PROP_5:
                return propTextures[4];
            case FOG:
                return fogTexture;
            case FOREGROUND:
                return foregroundTexture;
            default:
                return null;
        }
    }

    private static void drawSprite(SpriteBatch batch, Texture tex, float x, float y, float w, float h, boolean flipX) {
        if (tex == null) return;
        if (flipX) {
            batch.draw(tex, x + w, y, -w, h);
        }
        else {
            batch.draw(tex, x, y, w, h);
        }
    }

    private static Texture loadTexture(String path, Texture.TextureWrap wrapU, Texture.TextureWrap wrapV) {
        Texture tex = new Texture(Gdx.files.internal(path));
        tex.setWrap(wrapU, wrapV);
        tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return tex;
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
