package io.github.sugar_studios.javaCapstoneProject;

/**
 * Defines how an enemy fires bullets (timing, direction, and stats).
 */
public final class ShootBehavior {

    public final float   firstShotDelay;
    public final float   interval;
    public final boolean aimsAtPlayer;
    public final float   fixedAngle;
    public final float   bulletSpeed;
    public final float   bulletSize;
    public final float   acceleration;
    public final float   damage;

    private ShootBehavior(float firstShotDelay, float interval, boolean aimsAtPlayer, float fixedAngle, float bulletSpeed, float bulletSize, float acceleration, float damage) {
        this.firstShotDelay = firstShotDelay;
        this.interval= interval;
        this.aimsAtPlayer = aimsAtPlayer;
        this.fixedAngle = fixedAngle;
        this.bulletSpeed = bulletSpeed;
        this.bulletSize = bulletSize;
        this.acceleration = acceleration;
        this.damage = damage;
    }

    public static ShootBehavior aimed(float firstDelay, float interval, float bulletSpeed, float bulletSize, float acceleration, float damage) {
        return new ShootBehavior(firstDelay, interval, true, 0f, bulletSpeed, bulletSize, acceleration, damage);
    }

    public static ShootBehavior fixed(float firstDelay, float interval, float angleDeg, float bulletSpeed, float bulletSize, float acceleration, float damage) {
        return new ShootBehavior(firstDelay, interval, false, angleDeg, bulletSpeed, bulletSize, acceleration, damage);
    }
}
