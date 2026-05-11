package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

// All level data: 13 progressive bullet-hell waves, no checkpoint enemies.
// Circles (B3/B4/B5) are 96f and used for area denial.
public final class LevelData {

    private static final float GW = 1280f * (2f / 3f);
    private static final float GH = 720f;
    private static final float MARGIN = 48f;
    private static final float SPEED = 180f;

    private static final String B1  = "projectiles/bullet1.png";
    private static final String B2  = "projectiles/bullet2.png";
    private static final String B3  = "projectiles/bullet3.png";
    private static final String B4  = "projectiles/bullet4.png";
    private static final String B5  = "projectiles/bullet5.png";
    private static final String B6  = "projectiles/bullet6.png";
    private static final String B7  = "projectiles/bullet7.png";
    private static final String B8  = "projectiles/bullet8.png";
    private static final String B12 = "projectiles/bullet12.png";
    private static final String ANIM = "kitsune_big";

    private static final float SM = 40f;
    private static final float MD = 48f;
    private static final float LG = 96f;

    private LevelData() {}

    public static HashMap<Integer, Queue<Wave>> build() {
        HashMap<Integer, Queue<Wave>> map = new HashMap<>();
        map.put(1, buildLevel1());
        return map;
    }

    public static BackgroundConfig getBackgroundConfig(int level) {
        if (level == 1)
            return new BackgroundConfig(1, new Color(43f / 255f, 64f / 255f, 31f / 255f, 1f), "audio/music/【Ado】ルル.mp3");
        throw new IllegalArgumentException("No background config for level " + level);
    }

    private static Queue<Wave> buildLevel1() {
        Queue<Wave> q = new LinkedList<>();
        q.add(wave01_curtainRise());
        q.add(wave02_aimedPair());
        q.add(wave03_threeWayFan());
        q.add(wave04_circleDenial());
        q.add(wave05_crossingLanes());
        q.add(wave06_pinwheel());
        q.add(wave07_aimedSwarm());
        q.add(wave08_shotgunBlast());
        q.add(wave09_circleCage());
        q.add(wave10_spinSpiral());
        q.add(wave11_denseAssault());
        q.add(wave12_bulletCurtain());
        q.add(wave13_totalHell());
        return q;
    }

    private static Wave wave01_curtainRise() {
        float cx = GW / 2f;
        float hy = GH * 0.72f;
        return new Wave(
            e(path(cx - 190, GH + MARGIN, cx - 190, hy, cx - 190, -MARGIN),
                SPEED, ShootBehavior.fixed(1.2f, 1.6f, 270f, 200f, MD, 1f, 1f), 2f, 0f, B1),
            e(path(cx, GH + MARGIN, cx, hy, cx, -MARGIN),
                SPEED, ShootBehavior.fixed(1.2f, 1.6f, 270f, 200f, MD, 1f, 1f), 2f, 0.5f, B2),
            e(path(cx + 190, GH + MARGIN, cx + 190, hy, cx + 190, -MARGIN),
                SPEED, ShootBehavior.fixed(1.2f, 1.6f, 270f, 200f, MD, 1f, 1f), 2f, 0.5f, B1)
        );
    }

    private static Wave wave02_aimedPair() {
        float hy = GH * 0.62f;
        return new Wave(
            e(path(-MARGIN, hy, GW * 0.28f, hy, -MARGIN, hy),
                SPEED * 1.1f, ShootBehavior.aimed(0.6f, 1.4f, 260f, MD, 1f, 1f), 2f, 0f, B2),
            e(path(GW + MARGIN, hy, GW * 0.72f, hy, GW + MARGIN, hy),
                SPEED * 1.1f, ShootBehavior.aimed(0.6f, 1.4f, 260f, MD, 1f, 1f), 2f, 0.6f, B1)
        );
    }

    private static Wave wave03_threeWayFan() {
        float cx = GW / 2f;
        float hy = GH * 0.68f;
        return new Wave(
            e(path(cx - 200, GH + MARGIN, cx - 200, hy, cx - 200, -MARGIN),
                SPEED, ShootBehavior.spread(1.0f, 1.3f, 270f, 40f, 3, 240f, MD, 1f, 1f), 3f, 0f, B6),
            e(path(cx, GH + MARGIN, cx, hy, cx, -MARGIN),
                SPEED, ShootBehavior.spread(1.0f, 1.3f, 270f, 40f, 3, 240f, MD, 1f, 1f), 3f, 0.4f, B6),
            e(path(cx + 200, GH + MARGIN, cx + 200, hy, cx + 200, -MARGIN),
                SPEED, ShootBehavior.spread(1.0f, 1.3f, 270f, 40f, 3, 240f, MD, 1f, 1f), 3f, 0.4f, B6)
        );
    }

    private static Wave wave04_circleDenial() {
        float cx = GW / 2f;
        float hy = GH * 0.75f;
        float[] seg = speeds(SPEED * 0.3f, SPEED * 2.2f);
        return new Wave(
            e(path(cx - 170, GH + MARGIN, cx - 170, hy, cx - 170, -MARGIN),
                seg, ShootBehavior.fixed(0.8f, 1.0f, 270f, 160f, LG, 1f, 1f), 3f, 0f, B3),
            e(path(cx, GH + MARGIN, cx, hy, cx, -MARGIN),
                seg, ShootBehavior.fixed(0.8f, 1.0f, 270f, 160f, LG, 1f, 1f), 3f, 0.5f, B4),
            e(path(cx + 170, GH + MARGIN, cx + 170, hy, cx + 170, -MARGIN),
                seg, ShootBehavior.fixed(0.8f, 1.0f, 270f, 160f, LG, 1f, 1f), 3f, 0.5f, B5)
        );
    }

    private static Wave wave05_crossingLanes() {
        float rowA = GH * 0.65f;
        float rowB = GH * 0.42f;
        return new Wave(
            e(path(-MARGIN, rowA, GW + MARGIN, rowA),
                SPEED * 1.2f, ShootBehavior.fixed(0.4f, 0.8f, 0f, 320f, SM, 1f, 1f), 3f, 0f, B7),
            e(path(GW + MARGIN, rowA, -MARGIN, rowA),
                SPEED * 1.2f, ShootBehavior.fixed(0.4f, 0.8f, 180f, 320f, SM, 1f, 1f), 3f, 0.3f, B8),
            e(path(-MARGIN, rowB, GW + MARGIN, rowB),
                SPEED * 1.2f, ShootBehavior.fixed(0.4f, 0.8f, 0f, 320f, SM, 1f, 1f), 3f, 0.6f, B7),
            e(path(GW + MARGIN, rowB, -MARGIN, rowB),
                SPEED * 1.2f, ShootBehavior.fixed(0.4f, 0.8f, 180f, 320f, SM, 1f, 1f), 3f, 0.3f, B8)
        );
    }

    private static Wave wave06_pinwheel() {
        float cx = GW / 2f;
        float hy = GH * 0.65f;
        return new Wave(
            e(path(cx - 170, GH + MARGIN, cx - 170, hy, cx - 170, -MARGIN),
                SPEED * 1.1f, ShootBehavior.spin(0.6f, 0.13f, 270f, 22f, 300f, SM, 1f, 1f), 3f, 0f, B6),
            e(path(cx, GH + MARGIN, cx, hy, cx, -MARGIN),
                SPEED * 1.1f, ShootBehavior.spin(0.6f, 0.13f, 30f, 22f, 300f, SM, 1f, 1f), 3f, 0.3f, B6),
            e(path(cx + 170, GH + MARGIN, cx + 170, hy, cx + 170, -MARGIN),
                SPEED * 1.1f, ShootBehavior.spin(0.6f, 0.13f, 150f, 22f, 300f, SM, 1f, 1f), 3f, 0.3f, B6)
        );
    }

    private static Wave wave07_aimedSwarm() {
        float hy = GH * 0.60f;
        float[] xs = { GW * 0.1f, GW * 0.3f, GW * 0.5f, GW * 0.7f, GW * 0.9f };
        return new Wave(
            e(path(xs[0], GH + MARGIN, xs[0], hy, xs[0], -MARGIN), SPEED * 1.2f, ShootBehavior.aimed(0.5f, 1.0f, 320f, SM, 1f, 1f), 4f, 0f,  B2),
            e(path(xs[1], GH + MARGIN, xs[1], hy, xs[1], -MARGIN), SPEED * 1.2f, ShootBehavior.aimed(0.5f, 1.0f, 320f, SM, 1f, 1f), 4f, 0.4f, B1),
            e(path(xs[2], GH + MARGIN, xs[2], hy, xs[2], -MARGIN), SPEED * 1.2f, ShootBehavior.aimed(0.5f, 1.0f, 320f, SM, 1f, 1f), 4f, 0.4f, B2),
            e(path(xs[3], GH + MARGIN, xs[3], hy, xs[3], -MARGIN), SPEED * 1.2f, ShootBehavior.aimed(0.5f, 1.0f, 320f, SM, 1f, 1f), 4f, 0.4f, B1),
            e(path(xs[4], GH + MARGIN, xs[4], hy, xs[4], -MARGIN), SPEED * 1.2f, ShootBehavior.aimed(0.5f, 1.0f, 320f, SM, 1f, 1f), 4f, 0.4f, B2)
        );
    }

    private static Wave wave08_shotgunBlast() {
        float cx = GW / 2f;
        float hy = GH * 0.58f;
        return new Wave(
            e(path(cx - 220, GH + MARGIN, cx - 220, hy, cx - 220, -MARGIN),
                SPEED * 1.1f, ShootBehavior.spread(0.8f, 1.1f, 270f, 60f, 5, 340f, MD, 1f, 1f), 4f, 0f, B12),
            e(path(cx, GH + MARGIN, cx, hy, cx, -MARGIN),
                SPEED * 1.1f, ShootBehavior.spread(0.8f, 1.1f, 270f, 60f, 5, 340f, MD, 1f, 1f), 4f, 0.5f, B12),
            e(path(cx + 220, GH + MARGIN, cx + 220, hy, cx + 220, -MARGIN),
                SPEED * 1.1f, ShootBehavior.spread(0.8f, 1.1f, 270f, 60f, 5, 340f, MD, 1f, 1f), 4f, 0.5f, B12)
        );
    }

    private static Wave wave09_circleCage() {
        float cx = GW / 2f;
        float hyC = GH * 0.72f;
        float hyA = GH * 0.52f;
        return new Wave(
            e(path(cx - 150, GH + MARGIN, cx - 150, hyC, cx - 150, -MARGIN),
                SPEED * 0.9f, ShootBehavior.fixed(0.6f, 0.9f, 270f, 150f, LG, 1f, 1f), 4f, 0f, B5),
            e(path(cx + 150, GH + MARGIN, cx + 150, hyC, cx + 150, -MARGIN),
                SPEED * 0.9f, ShootBehavior.fixed(0.6f, 0.9f, 270f, 150f, LG, 1f, 1f), 4f, 0f, B3),
            e(path(-MARGIN, hyA, GW * 0.30f, hyA, -MARGIN, hyA),
                SPEED * 1.2f, ShootBehavior.aimed(0.4f, 0.9f, 340f, SM, 1f, 1f), 4f, 0.8f, B8),
            e(path(GW + MARGIN, hyA, GW * 0.70f, hyA, GW + MARGIN, hyA),
                SPEED * 1.2f, ShootBehavior.aimed(0.4f, 0.9f, 340f, SM, 1f, 1f), 4f, 0.8f, B7)
        );
    }

    private static Wave wave10_spinSpiral() {
        float cx = GW / 2f;
        float hy = GH * 0.62f;
        return new Wave(
            e(path(cx - 240, GH + MARGIN, cx - 240, hy, cx - 240, -MARGIN),
                SPEED * 1.2f, ShootBehavior.spin(0.5f, 0.11f, 270f, 20f, 360f, SM, 1f, 1f), 5f, 0f, B1),
            e(path(cx - 80, GH + MARGIN, cx - 80, hy, cx - 80, -MARGIN),
                SPEED * 1.2f, ShootBehavior.spin(0.5f, 0.11f, 90f, -20f, 360f, SM, 1f, 1f), 5f, 0.3f, B2),
            e(path(cx + 80, GH + MARGIN, cx + 80, hy, cx + 80, -MARGIN),
                SPEED * 1.2f, ShootBehavior.spin(0.5f, 0.11f, 270f, 20f, 360f, SM, 1f, 1f), 5f, 0.3f, B1),
            e(path(cx + 240, GH + MARGIN, cx + 240, hy, cx + 240, -MARGIN),
                SPEED * 1.2f, ShootBehavior.spin(0.5f, 0.11f, 90f, -20f, 360f, SM, 1f, 1f), 5f, 0.3f, B2)
        );
    }

    private static Wave wave11_denseAssault() {
        float cx = GW / 2f;
        float hyT = GH * 0.68f;
        float hyM = GH * 0.50f;
        return new Wave(
            e(path(cx - 200, GH + MARGIN, cx - 200, hyT, cx - 200, -MARGIN),
                SPEED * 1.3f, ShootBehavior.spread(0.7f, 1.0f, 270f, 50f, 4, 360f, SM, 1f, 1f), 5f, 0f, B6),
            e(path(cx + 200, GH + MARGIN, cx + 200, hyT, cx + 200, -MARGIN),
                SPEED * 1.3f, ShootBehavior.spread(0.7f, 1.0f, 270f, 50f, 4, 360f, SM, 1f, 1f), 5f, 0f, B6),
            e(path(cx - 100, GH + MARGIN, cx - 100, hyM, cx - 100, -MARGIN),
                SPEED * 1.3f, ShootBehavior.spin(0.5f, 0.12f, 270f, 18f, 380f, SM, 1f, 1f), 5f, 0.5f, B12),
            e(path(cx + 100, GH + MARGIN, cx + 100, hyM, cx + 100, -MARGIN),
                SPEED * 1.3f, ShootBehavior.spin(0.5f, 0.12f, 90f, -18f, 380f, SM, 1f, 1f), 5f, 0.5f, B12),
            e(path(-MARGIN, hyM, GW * 0.22f, hyM, -MARGIN, hyM),
                SPEED * 1.3f, ShootBehavior.aimed(0.4f, 0.8f, 380f, SM, 1f, 1f), 5f, 0.8f, B8),
            e(path(GW + MARGIN, hyM, GW * 0.78f, hyM, GW + MARGIN, hyM),
                SPEED * 1.3f, ShootBehavior.aimed(0.4f, 0.8f, 380f, SM, 1f, 1f), 5f, 0.8f, B7)
        );
    }

    private static Wave wave12_bulletCurtain() {
        float cx = GW / 2f;
        float hyS = GH * 0.65f;
        float hyC = GH * 0.55f;
        return new Wave(
            e(path(cx - 180, GH + MARGIN, cx - 180, hyS, cx - 180, -MARGIN),
                SPEED * 1.3f, ShootBehavior.spread(0.6f, 0.9f, 270f, 80f, 7, 380f, SM, 1f, 1f), 6f, 0f, B2),
            e(path(cx + 180, GH + MARGIN, cx + 180, hyS, cx + 180, -MARGIN),
                SPEED * 1.3f, ShootBehavior.spread(0.6f, 0.9f, 270f, 80f, 7, 380f, SM, 1f, 1f), 6f, 0f, B1),
            e(path(cx, GH + MARGIN, cx, hyC, cx, -MARGIN),
                SPEED * 0.9f, ShootBehavior.fixed(0.5f, 0.85f, 270f, 140f, LG, 1f, 1f), 6f, 0.4f, B4),
            e(path(-MARGIN, GH * 0.48f, GW * 0.25f, GH * 0.48f, -MARGIN, GH * 0.48f),
                SPEED * 1.4f, ShootBehavior.aimed(0.3f, 0.7f, 400f, SM, 1f, 1f), 6f, 0.6f, B7),
            e(path(GW + MARGIN, GH * 0.48f, GW * 0.75f, GH * 0.48f, GW + MARGIN, GH * 0.48f),
                SPEED * 1.4f, ShootBehavior.aimed(0.3f, 0.7f, 400f, SM, 1f, 1f), 6f, 0.6f, B8)
        );
    }

    private static Wave wave13_totalHell() {
        float cx = GW / 2f;
        float hyT = GH * 0.70f;
        float hyM = GH * 0.52f;
        float hyL = GH * 0.38f;
        return new Wave(
            e(path(cx - 220, GH + MARGIN, cx - 220, hyT, cx - 220, -MARGIN),
                SPEED * 1.5f, ShootBehavior.spin(0.3f, 0.09f, 270f, 24f, 460f, SM, 1f, 1f), 7f, 0f, B6),
            e(path(cx, GH + MARGIN, cx, hyT, cx, -MARGIN),
                SPEED * 1.5f, ShootBehavior.spin(0.3f, 0.09f, 30f, 24f, 460f, SM, 1f, 1f), 7f, 0.2f, B6),
            e(path(cx + 220, GH + MARGIN, cx + 220, hyT, cx + 220, -MARGIN),
                SPEED * 1.5f, ShootBehavior.spin(0.3f, 0.09f, 150f, 24f, 460f, SM, 1f, 1f), 7f, 0.2f, B6),
            e(path(cx - 130, GH + MARGIN, cx - 130, hyM, cx - 130, -MARGIN),
                SPEED * 1.4f, ShootBehavior.spread(0.5f, 0.85f, 270f, 70f, 6, 420f, SM, 1f, 1f), 7f, 0.4f, B12),
            e(path(cx + 130, GH + MARGIN, cx + 130, hyM, cx + 130, -MARGIN),
                SPEED * 1.4f, ShootBehavior.spread(0.5f, 0.85f, 270f, 70f, 6, 420f, SM, 1f, 1f), 7f, 0.4f, B12),
            e(path(cx, GH + MARGIN, cx, hyL, cx, -MARGIN),
                SPEED * 0.9f, ShootBehavior.fixed(0.4f, 0.75f, 270f, 130f, LG, 1f, 1f), 8f, 0.7f, B5),
            e(path(-MARGIN, GH * 0.35f, GW * 0.20f, GH * 0.35f, -MARGIN, GH * 0.35f),
                SPEED * 1.5f, ShootBehavior.aimed(0.2f, 0.55f, 450f, SM, 1f, 1f), 7f, 0.5f, B7),
            e(path(GW + MARGIN, GH * 0.35f, GW * 0.80f, GH * 0.35f, GW + MARGIN, GH * 0.35f),
                SPEED * 1.5f, ShootBehavior.aimed(0.2f, 0.55f, 450f, SM, 1f, 1f), 7f, 0.5f, B8)
        );
    }


    private static WaveEntry e(Vector2[] path, float speed, ShootBehavior b,
                               float health, float delay, String bullet) {
        return e(path, new float[]{ speed }, b, health, delay, bullet);
    }

    private static WaveEntry e(Vector2[] path, float[] speeds, ShootBehavior b,
                               float health, float delay, String bullet) {
        return new WaveEntry(new Enemy(path, speeds, false, b, health), delay, false, ANIM, bullet);
    }

    private static float[] speeds(float... s) { return s; }

    private static Vector2[] path(float... coords) {
        Vector2[] pts = new Vector2[coords.length / 2];
        for (int i = 0; i < pts.length; i++)
            pts[i] = new Vector2(coords[i * 2], coords[i * 2 + 1]);
        return pts;
    }
}
