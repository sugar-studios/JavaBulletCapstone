package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Manages and renders every visual layer that sits beneath — or in front of —
 * the core game objects (player, enemies, bullets).
 */
public class BackgroundLayer {
    // Background Colour
    private static final float BG_R = 43f / 255f;
    private static final float BG_G = 64f /255f;
    private static final float BG_B = 31f /255f;


    // Flanking Treeline
    private static final String TREELINE_ASSET = "background/tree.png";
    private static final float TREELINE_WORLD_WIDTH = 55f;
    private static final float TREELINE_SCROLL_FACTOR = 0.65f;
    private static final float TREELINE_RIGHT_Y_OFFSET = 290f;

    // Props
    private static final float MINIMUM_PROP_TIMER = 2f;
    private static final float PROP_CHANCE = .3f;
    private static final float PROP_EDGE_MARGIN = 24f;
    private static final float PROP_TINT_MIN = 0.45f;
    private static final float PROP_TINT_MAX = 0.75f;
    private static final float PROP_SCALE_MIN = 0.6f;
    private static final float PROP_SCALE_MAX = 1.4f;
    private static final float PROP_ALPHA = .35f;

    // Fog
    private static final float FOG_ALPHA = 0.55f;


    private final float gameplayWorldWidth;
    private final float worldHeight;
    private final float scrollSpeed;

    private final float propSpawnXMin;
    private final float propSpawnXMax;

    private Texture whitePixel;
    private Texture treelineTexture;
    private final Map<PropType, Texture> propTextures = new EnumMap<>(PropType.class);

    private final ProbabilityQueue<PropType> propQueue;
    private final List<ActiveProp> activeProps = new ArrayList<>();

    private float treelineScrollY    = 0f;
    private float timeSinceLastProp  = 0f;

    public BackgroundLayer(float gameplayWorldWidth, float worldHeight, float scrollSpeed) {
        this.gameplayWorldWidth = gameplayWorldWidth;
        this.worldHeight = worldHeight;
        this.scrollSpeed = scrollSpeed;

        this.propSpawnXMin = TREELINE_WORLD_WIDTH + PROP_EDGE_MARGIN;
        this.propSpawnXMax = gameplayWorldWidth - TREELINE_WORLD_WIDTH - PROP_EDGE_MARGIN;

        List<PropType> allTypes = new ArrayList<>(Arrays.asList(PropType.values()));
        this.propQueue = new ProbabilityQueue<>(allTypes);
    }

    public void load() {
        whitePixel = createWhitePixel();

        treelineTexture = loadTexture(TREELINE_ASSET,
            Texture.TextureWrap.ClampToEdge,
            Texture.TextureWrap.Repeat);

        for (PropType type : PropType.values()) {
            propTextures.put(type, loadTexture(type.assetFile,
                Texture.TextureWrap.ClampToEdge,
                Texture.TextureWrap.ClampToEdge));
        }
    }

    /**
     * Advances scroll offsets, moves props downward, culls off-screen props,
     * and potentially spawns a new prop.
     */
    public void update(float delta, float scrollMultiplier) {
        float effective = scrollSpeed * scrollMultiplier;

        treelineScrollY = (treelineScrollY + effective * TREELINE_SCROLL_FACTOR * delta) % worldHeight;

        Iterator<ActiveProp> it = activeProps.iterator();
        while (it.hasNext()) {
            ActiveProp prop = it.next();
            prop.y -= effective * delta;
            if (prop.y + prop.type.displayHeight < 0f) it.remove();
        }

        timeSinceLastProp += delta;
        if (timeSinceLastProp >= MINIMUM_PROP_TIMER) {
            if (MathUtils.random() < PROP_CHANCE * delta) {
                spawnNextProp();
                timeSinceLastProp = 0f;
            }
        }
    }

    /**
     * Draws: background fill → background-layer props → fog → treelines.
     */
    public void renderBackground(SpriteBatch batch) {
        batch.begin();
        drawFill(batch);
        drawBackgroundProps(batch);
        drawFogProps(batch);
        drawTreelines(batch);
        batch.end();
    }

    public void renderForeground(SpriteBatch batch) {
        batch.begin();
        for (ActiveProp prop : activeProps) {
            if (prop.type.isOfuda) drawOfuda(batch, prop);   // fog removed from here
        }
        batch.end();
    }

    public void dispose() {
        if (whitePixel != null) whitePixel.dispose();
        if (treelineTexture != null) treelineTexture.dispose();
        for (Texture tex : propTextures.values()) if (tex != null) tex.dispose();
    }


    // prop spawning
    private void spawnNextProp() {
        PropType type = propQueue.selectNext();
        float spawnY = worldHeight + type.displayHeight;
        float spawnX;
        boolean flipX = false;

        if (type.isFog) {
            boolean onLeft = MathUtils.randomBoolean();
            if (onLeft) {
                spawnX = 0f;
                flipX  = true;
            } else {
                spawnX = gameplayWorldWidth - type.displayWidth;
                flipX  = false;
            }
        } else if (type.isOfuda) {
            spawnX = 0f;
        } else {
            float maxX = propSpawnXMax - type.displayWidth;
            spawnX = (propSpawnXMin < maxX)
                ? MathUtils.random(propSpawnXMin, maxX)
                : propSpawnXMin;
        }

        boolean isFg = type.isForeground();
        float scale = isFg ? 1f : MathUtils.random(PROP_SCALE_MIN, PROP_SCALE_MAX);
        float tint = isFg ? 1f : MathUtils.random(PROP_TINT_MIN,  PROP_TINT_MAX);

        activeProps.add(new ActiveProp(type, spawnX, spawnY, flipX, scale, tint));
    }

    private void drawFill(SpriteBatch batch) {
        batch.setColor(BG_R, BG_G, BG_B, 1f);
        batch.draw(whitePixel, 0f, 0f, gameplayWorldWidth, worldHeight);
        batch.setColor(Color.WHITE);
    }

    private void drawBackgroundProps(SpriteBatch batch) {
        for (ActiveProp prop : activeProps) {
            if (!prop.type.isForeground()) {
                float drawW = prop.type.displayWidth  * prop.scale;
                float drawH = prop.type.displayHeight * prop.scale;
                float drawX = prop.x + (prop.type.displayWidth  - drawW) / 2f;
                float drawY = prop.y + (prop.type.displayHeight - drawH) / 2f;

                batch.setColor(prop.tint, prop.tint, prop.tint, PROP_ALPHA);
                drawSprite(batch, propTextures.get(prop.type),
                    drawX, drawY, drawW, drawH, prop.flipX);
                batch.setColor(Color.WHITE);
            }
        }
    }

    private void drawTreelines(SpriteBatch batch) {
        if (treelineTexture == null) return;
        float rightScrollY = (treelineScrollY + TREELINE_RIGHT_Y_OFFSET) % worldHeight;

        drawTiledTreeline(batch, 0f, false, treelineScrollY);
        drawTiledTreeline(batch, gameplayWorldWidth - TREELINE_WORLD_WIDTH, true, rightScrollY);
    }

    private void drawTiledTreeline(SpriteBatch batch,
                                   float x, boolean flipX, float currentScrollY) {
        for (int copy = 0; copy < 2; copy++) {
            float drawY = -currentScrollY + (copy * worldHeight);
            drawSprite(batch, treelineTexture,
                x, drawY,
                TREELINE_WORLD_WIDTH, worldHeight,
                flipX);
        }
    }

    // foreground rendering
    private void drawFog(SpriteBatch batch, ActiveProp prop) {
        Texture tex = propTextures.get(PropType.FOG);
        if (tex == null) return;
        batch.setColor(1f, 1f, 1f, FOG_ALPHA);
        drawSprite(batch, tex,
            prop.x, prop.y,
            prop.type.displayWidth, prop.type.displayHeight,
            prop.flipX);
        batch.setColor(Color.WHITE);
    }

    private void drawOfuda(SpriteBatch batch, ActiveProp prop) {
        Texture tex = propTextures.get(PropType.OFUDA);
        if (tex == null) return;
        batch.setColor(1f, 1f, 1f, 0.5f);
        float tileW = prop.type.displayWidth;
        float tileH = prop.type.displayHeight;
        float drawX = 0f;
        while (drawX < gameplayWorldWidth) {
            batch.draw(tex, drawX, prop.y, tileW, tileH);
            drawX += tileW;
        }
        batch.setColor(Color.WHITE);
    }

    private void drawFogProps(SpriteBatch batch) {
        for (ActiveProp prop : activeProps) {
            if (prop.type.isFog) drawFog(batch, prop);
        }
    }


    private static void drawSprite(SpriteBatch batch, Texture tex, float x, float y, float width, float height, boolean flipX) {
        if (tex == null) return;
        if (flipX) {
            batch.draw(tex, x + width, y, -width, height);
        } else {
            batch.draw(tex, x, y, width, height);
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
