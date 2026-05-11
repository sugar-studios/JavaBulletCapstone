package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import java.util.HashMap;


/**
 * Central registry for enemy animations and shared bullet textures.
 */
public final class EnemyAnimRegistry {

    private static final HashMap<String, EnemyAnimData> registry   = new HashMap<>();
    private static final HashMap<String, Texture>       bulletCache = new HashMap<>();

    private EnemyAnimRegistry() {}

    public static void load() {
        registry.put("placeholder", makePlaceholder());
        registry.put("kitsune_big", loadFromPaths(
            "enemies/kitsune_big/big_kitsune", 12, 1f / 12f, 96f, 96f));
    }

    public static EnemyAnimData get(String key) {
        EnemyAnimData d = registry.get(key);
        if (d == null) {
            Gdx.app.error("EnemyAnimRegistry", "Unknown key '" + key + "' — falling back to placeholder");
            return registry.get("placeholder");
        }
        return d;
    }

    public static Texture getBulletTexture(String path) {
        if (path == null) return getPlaceholderBullet();
        return bulletCache.computeIfAbsent(path, p -> {
            Texture t = new Texture(Gdx.files.internal(p));
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            return t;
        });
    }

    public static void dispose() {
        registry.values().forEach(EnemyAnimData::dispose);
        registry.clear();
        bulletCache.values().forEach(t -> { if (t != null) t.dispose(); });
        bulletCache.clear();
    }

    private static EnemyAnimData makePlaceholder() {
        Pixmap pm = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pm.setColor(Color.RED);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return new EnemyAnimData(new Texture[]{ t }, 0.1f, 32f, 32f);
    }

    private static EnemyAnimData loadFromPaths(
        String basePath, int frameCount, float frameDur,
        float displayW, float displayH) {
        Texture[] frames = new Texture[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = load(basePath + (i + 1) + ".png");
        }
        return new EnemyAnimData(frames, frameDur, displayW, displayH);
    }

    private static Texture getPlaceholderBullet() {
        return bulletCache.computeIfAbsent("__placeholder_bullet__", k -> {
            Pixmap pm = new Pixmap(10, 10, Pixmap.Format.RGBA8888);
            pm.setColor(Color.YELLOW);
            pm.fill();
            Texture t = new Texture(pm);
            pm.dispose();
            return t;
        });
    }

    private static Texture load(String path) {
        Texture t = new Texture(Gdx.files.internal(path));
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return t;
    }
}
