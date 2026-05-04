package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.math.Vector2;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Static factory for the complete level → wave map consumed by {@link WaveManager}.
 *
 * Data structure hierarchy
 * ────────────────────────
 *   HashMap&lt;Integer, Queue&lt;Wave&gt;&gt;
 *     key   → level number
 *     value → LinkedList-backed Queue of Wave objects (FIFO, polled one at a time)
 *               └─ Wave  holds a fixed WaveEntry[]  (scripted, immutable order)
 *                           └─ WaveEntry = Enemy + spawnDelay + isCheckpoint flag
 *
 * Adding a new level: create a buildLevelN() method and register it in build().
 *
 * DESIGN NOTE — checkpoint entry ordering:
 *   A checkpoint entry blocks ALL subsequent entries in the same wave until the
 *   checkpoint enemy dies.  Therefore any enemies that should be alive AT THE SAME
 *   TIME as the checkpoint must appear BEFORE it in the WaveEntry array.
 *   The checkpoint is always the last entry of its wave.
 */
import com.badlogic.gdx.math.Vector2;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public final class LevelData {

    private static final float GW = 1280f * (2f / 3f);
    private static final float GH = 720f;
    private static final float MARGIN = 48f;

    private static final float SPEED = 180f;
    private static final float NORMAL_HEALTH = 3f;
    private static final float BOSS_HEALTH = 6f;

    private LevelData() {}

    public static HashMap<Integer, Queue<Wave>> build() {
        HashMap<Integer, Queue<Wave>> map = new HashMap<>();
        map.put(1, buildLevel1());
        return map;
    }

    private static Queue<Wave> buildLevel1() {
        Queue<Wave> q = new LinkedList<>();
        q.add(wave1_openingSalvo());
        q.add(wave2_commanderAndFlankers());
        q.add(wave3_crossfire());
        return q;
    }

    private static Wave wave1_openingSalvo() {
        float cx    = GW / 2f;
        float holdY = GH * 0.70f;

        return new Wave(
            entry(basic(
                    path(cx - 160, GH + MARGIN, cx - 160, holdY, cx - 160, -MARGIN),
                    ShootBehavior.fixed(1.2f, 99f, 270f, 320f, 64, 1f, 1f)),
                1.5f, false, "kitsune_big", "projectiles/bullet1.png"),

            entry(basic(
                    path(cx, GH + MARGIN, cx, holdY, cx, -MARGIN),
                    ShootBehavior.fixed(1.2f, 99f, 270f, 320f, 64f, 1f, 1f)),
                1.5f, false, "kitsune_big", "projectiles/bullet2.png"),

            entry(basic(
                    path(cx + 160, GH + MARGIN, cx + 160, holdY, cx + 160, -MARGIN),
                    ShootBehavior.fixed(1.2f, 99f, 270f, 320f, 64f, 1f, 1f)),
                0f, false, "kitsune_big", "projectiles/bullet3.png")
        );
    }

    private static Wave wave2_commanderAndFlankers() {
        float cx = GW / 2f;
        float holdY = GH * 0.62f;
        float fY = GH * 0.58f;

        return new Wave(
            entry(basic(
                    path(-MARGIN, fY, GW * 0.25f, fY, -MARGIN, fY),
                    ShootBehavior.aimed(0.8f, 2.2f, 300f, 64f, 1f, 1f)),
                0.8f, false, "kitsune_big", "projectiles/bullet1.png"),

            entry(basic(
                    path(GW + MARGIN, fY, GW * 0.75f, fY, GW + MARGIN, fY),
                    ShootBehavior.aimed(0.8f, 2.2f, 300f, 64f, 1f, 1f)),
                1.5f, false, "kitsune_big", "projectiles/bullet2.png"),

            entry(boss(
                    path(cx, GH + MARGIN, cx, holdY),
                    ShootBehavior.aimed(1.0f, 1.3f, 380f, 128f, 1f, 1f)),
                0f, true, "kitsune_big", "projectiles/bullet8.png")
        );
    }

    private static Wave wave3_crossfire() {
        float rowA = GH * 0.65f;
        float rowB = GH * 0.40f;

        return new Wave(
            entry(basic(
                    path(-MARGIN, rowA, GW * 0.28f, rowA, -MARGIN, rowA),
                    ShootBehavior.fixed(0.6f, 1.8f, 0f, 350f, 64f, 1f, 1f)),
                0.8f, false, "kitsune_big", "projectiles/bullet1.png"),

            entry(basic(
                    path(GW + MARGIN, rowA, GW * 0.72f, rowA, GW + MARGIN, rowA),
                    ShootBehavior.fixed(0.6f, 1.8f, 180f, 350f, 64f, 1f, 1f)),
                0.8f, false, "kitsune_big", "projectiles/bullet2.png"),

            entry(basic(
                    path(-MARGIN, rowB, GW * 0.28f, rowB, -MARGIN, rowB),
                    ShootBehavior.fixed(0.6f, 1.8f, 0f, 350f, 64f, 1f, 1f)),
                0.8f, false, "kitsune_big", "projectiles/bullet3.png"),

            entry(basic(
                    path(GW + MARGIN, rowB, GW * 0.72f, rowB, GW + MARGIN, rowB),
                    ShootBehavior.fixed(0.6f, 1.8f, 180f, 350f, 64f, 1f, 1f)),
                0f, false, "kitsune_big", "projectiles/bullet4.png")
        );
    }

    private static Vector2[] path(float... coords) {
        if (coords.length % 2 != 0)
            throw new IllegalArgumentException("path() requires an even number of coordinates.");
        Vector2[] pts = new Vector2[coords.length / 2];
        for (int i = 0; i < pts.length; i++)
            pts[i] = new Vector2(coords[i * 2], coords[i * 2 + 1]);
        return pts;
    }

    private static Enemy basic(Vector2[] waypoints, ShootBehavior behavior) {
        return new Enemy(waypoints, SPEED, false, behavior, NORMAL_HEALTH);
    }

    private static Enemy boss(Vector2[] waypoints, ShootBehavior behavior) {
        return new Enemy(waypoints, SPEED * 0.85f, true, behavior, BOSS_HEALTH);
    }

    private static WaveEntry entry(Enemy enemy, float spawnDelay, boolean checkpoint, String animKey, String bulletPath) {
        return new WaveEntry(enemy, spawnDelay, checkpoint, animKey, bulletPath);
    }
}
