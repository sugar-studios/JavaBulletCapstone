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

    private static final HashMap<String, EnemyAnimData> registry  = new HashMap<>();
    private static final HashMap<String, Texture>       bulletCache = new HashMap<>();

    private EnemyAnimRegistry() {}

    public static void load() {
        registry.put("placeholder", makePlaceholder());
        registry.put("kitsune_big", loadFromPaths(
            "enemies/kitsune_big/big_kitsune", 12, 1f / 12f,
            null, 0, 0f,
            96f, 96f));
    }

    public static EnemyAnimData get(String key) {
        EnemyAnimData d = registry.get(key);
        if (d == null) {
            Gdx.app.error("EnemyAnimRegistry", "Unknown key '" + key + "' — falling back to placeholder");
            return registry.get("placeholder");
        }
        return d;
    }

    // Bullet textures are cached here so they outlive the enemy that fired them.
    public static Texture getBulletTexture(String path) {
        if (path == null) return getPlaceholderBullet();
        Texture t = bulletCache.get(path);
        if (t == null) {
            t = new Texture(Gdx.files.internal(path));
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            bulletCache.put(path, t);
        }
        return t;
    }

    public static void dispose() {
        for (EnemyAnimData d : registry.values()) d.dispose();
        registry.clear();
        for (Texture t : bulletCache.values()) if (t != null) t.dispose();
        bulletCache.clear();
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

    private static EnemyAnimData makePlaceholder() {
        Pixmap pm = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pm.setColor(Color.RED);
        pm.fill();
        Texture idle = new Texture(pm);
        pm.dispose();
        return new EnemyAnimData(new Texture[]{ idle }, 0.1f, null, 0f, 32f, 32f);
    }

    private static EnemyAnimData loadFromPaths(
        String idlePath, int idleCount, float idleDur,
        String attackPath, int attackCount, float attackDur,
        float displayW, float displayH) {

        Texture[] idle = new Texture[idleCount];
        for (int i = 0; i < idleCount; i++)
            idle[i] = load(idlePath + (i + 1) + ".png");

        Texture[] attack = null;
        if (attackPath != null && attackCount > 0) {
            attack = new Texture[attackCount];
            for (int i = 0; i < attackCount; i++)
                attack[i] = load(attackPath + (i + 1) + ".png");
        }

        return new EnemyAnimData(idle, idleDur, attack, attackDur, displayW, displayH);
    }

    private static Texture load(String path) {
        Texture t = new Texture(Gdx.files.internal(path));
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return t;
    }
}
