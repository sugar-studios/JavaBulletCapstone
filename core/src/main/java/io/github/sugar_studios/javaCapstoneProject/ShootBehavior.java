package io.github.sugar_studios.javaCapstoneProject;

// Describes how an enemy fires. All four modes share the same timing fields.
// SPREAD fires bulletCount bullets in a fan each shot.
// SPIN rotates by spinRate degrees each shot — mutable, not safe to share between enemies.
public class ShootBehavior {

    public enum Mode { FIXED, AIMED, SPREAD, SPIN }

    public final Mode mode;
    public final float firstShotDelay;
    public final float interval;
    public final float bulletSpeed;
    public final float bulletSize;
    public final float damage;
    public final float acceleration;

    // FIXED / SPREAD center / SPIN start angle
    public final float fixedAngle;
    // SPREAD
    public final float spreadDeg;
    public final int bulletCount;
    // SPIN
    public final float spinRate;
    private float currentAngle;

    // Legacy field — kept for any existing code that reads it
    public final boolean aimsAtPlayer;

    private ShootBehavior(Mode mode, float firstShotDelay, float interval,
                          float angle, float speed, float size, float damage, float accel,
                          float spreadDeg, int bulletCount, float spinRate) {
        this.mode = mode;
        this.firstShotDelay = firstShotDelay;
        this.interval = interval;
        this.fixedAngle = angle;
        this.currentAngle = angle;
        this.bulletSpeed = speed;
        this.bulletSize = size;
        this.damage = damage;
        this.acceleration = accel;
        this.spreadDeg = spreadDeg;
        this.bulletCount = bulletCount;
        this.spinRate = spinRate;
        this.aimsAtPlayer = (mode == Mode.AIMED);
    }

    // Original signatures — unchanged.
    public static ShootBehavior fixed(float firstShotDelay, float interval,
                                      float angle, float speed, float bulletSize, float damage, float accel) {
        return new ShootBehavior(Mode.FIXED, firstShotDelay, interval,
            angle, speed, bulletSize, damage, accel, 0f, 1, 0f);
    }

    public static ShootBehavior aimed(float firstShotDelay, float interval,
                                      float speed, float bulletSize, float damage, float accel) {
        return new ShootBehavior(Mode.AIMED, firstShotDelay, interval,
            0f, speed, bulletSize, damage, accel, 0f, 1, 0f);
    }

    // Fan of bulletCount bullets spread over spreadDeg degrees, centred on centerAngle.
    public static ShootBehavior spread(float firstShotDelay, float interval,
                                       float centerAngle, float spreadDeg, int bulletCount,
                                       float speed, float bulletSize, float damage, float accel) {
        return new ShootBehavior(Mode.SPREAD, firstShotDelay, interval,
            centerAngle, speed, bulletSize, damage, accel, spreadDeg, bulletCount, 0f);
    }

    // Rotates startAngle by spinRate degrees every shot.
    public static ShootBehavior spin(float firstShotDelay, float interval,
                                     float startAngle, float spinRate,
                                     float speed, float bulletSize, float damage, float accel) {
        return new ShootBehavior(Mode.SPIN, firstShotDelay, interval,
            startAngle, speed, bulletSize, damage, accel, 0f, 1, spinRate);
    }

    // Returns the angles (degrees) to fire this shot. Called once per Enemy shot.
    public float[] getFireAngles(Player player, float enemyCx, float enemyCy) {
        switch (mode) {
            case AIMED: {
                if (player == null) return new float[]{ 270f };
                float dx = (player.rect.x + player.rect.width / 2f) - enemyCx;
                float dy = (player.rect.y + player.rect.height / 2f) - enemyCy;
                float angle = (float)(Math.atan2(dy, dx) * 180.0 / Math.PI);
                return new float[]{ angle };
            }
            case SPREAD: {
                float[] angles = new float[bulletCount];
                float step = bulletCount > 1 ? spreadDeg / (bulletCount - 1) : 0f;
                float start = fixedAngle - spreadDeg / 2f;
                for (int i = 0; i < bulletCount; i++) angles[i] = start + step * i;
                return angles;
            }
            case SPIN: {
                float angle = currentAngle;
                currentAngle += spinRate;
                return new float[]{ angle };
            }
            default:
                return new float[]{ fixedAngle };
        }
    }
}
