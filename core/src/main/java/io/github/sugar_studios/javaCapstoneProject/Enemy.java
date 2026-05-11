package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

// An enemy that follows waypoints and fires using a ShootBehavior.
public class Enemy extends Entity {

    private final Vector2[] waypoints;
    private int waypointIndex;
    private final float[] segmentSpeeds;
    private final boolean isCheckpoint;

    private final ShootBehavior shootBehavior;
    private float shotTimer;

    private EnemyAnimData anim;
    private Texture bulletTexture;
    private int frame;
    private float frameTimer;

    private boolean markedForRemoval;

    public Enemy(Vector2[] waypoints, float[] segmentSpeeds, boolean isCheckpoint, ShootBehavior behavior, float health) {
        super(waypoints[0].x, waypoints[0].y, 0f, 0f, null, Faction.ENEMY, health);
        this.waypoints = waypoints;
        this.waypointIndex = 1;
        this.segmentSpeeds = segmentSpeeds;
        this.isCheckpoint = isCheckpoint;
        this.shootBehavior = behavior;
        this.shotTimer = (behavior != null) ? behavior.firstShotDelay : Float.MAX_VALUE;
    }

    public Enemy(Vector2[] waypoints, float speed, boolean isCheckpoint, ShootBehavior behavior, float health) {
        this(waypoints, new float[]{ speed }, isCheckpoint, behavior, health);
    }

    public void initAnim(String animKey, String bulletPath) {
        anim = EnemyAnimRegistry.get(animKey != null ? animKey : "placeholder");
        rect.width = anim.displayW();
        rect.height = anim.displayH();
        bulletTexture = EnemyAnimRegistry.getBulletTexture(bulletPath);
    }

    public void update(float delta, Player player) {
        if (anim == null || !isAlive()) { markedForRemoval = true; return; }
        updateMovement(delta);
        updateShooting(delta, player);
        updateAnimation(delta);
    }

    @Override public void update(float delta) { update(delta, null); }

    @Override
    public void draw(Batch batch) {
        if (anim == null) return;
        batch.draw(anim.frames()[frame], rect.x, rect.y, rect.width, rect.height);
    }

    private void updateMovement(float delta) {
        if (waypointIndex >= waypoints.length) {
            if (!isCheckpoint) markedForRemoval = true;
            return;
        }
        // Clamp so the last speed value covers any extra segments.
        float speed = segmentSpeeds[Math.min(waypointIndex - 1, segmentSpeeds.length - 1)];
        Vector2 target = waypoints[waypointIndex];
        float dx = target.x - rect.x;
        float dy = target.y - rect.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        float step = speed * delta;
        if (dist <= step) {
            rect.x = target.x;
            rect.y = target.y;
            waypointIndex++;
            if (waypointIndex >= waypoints.length && !isCheckpoint) markedForRemoval = true;
        } else {
            rect.x += (dx / dist) * step;
            rect.y += (dy / dist) * step;
        }
    }

    private void updateShooting(float delta, Player player) {
        if (shootBehavior == null) return;
        shotTimer -= delta;
        if (shotTimer <= 0f) {
            shotTimer = shootBehavior.interval;
            fireBullet(player);
        }
    }

    // Fires one or more bullets according to ShootBehavior mode.
    // SPREAD and SPIN can produce multiple bullets per shot.
    private void fireBullet(Player player) {
        float size = shootBehavior.bulletSize;
        float bx = rect.x + (rect.width - size) / 2f;
        float by = rect.y + (rect.height - size) / 2f;
        float cx = rect.x + rect.width / 2f;
        float cy = rect.y + rect.height / 2f;

        float[] angles = shootBehavior.getFireAngles(player, cx, cy);
        for (float angle : angles) {
            BulletPool.add(new Bullet(
                bx, by, size, size, bulletTexture,
                Faction.ENEMY,
                shootBehavior.damage,
                shootBehavior.bulletSpeed,
                angle,
                shootBehavior.acceleration,
                0f
            ));
        }
    }

    private void updateAnimation(float delta) {
        if (anim.frames().length <= 1) return;
        frameTimer += delta;
        if (frameTimer >= anim.frameDur()) {
            frameTimer -= anim.frameDur();
            frame = (frame + 1) % anim.frames().length;
        }
    }

    public boolean isMarkedForRemoval() { return markedForRemoval; }
    public boolean isCheckpointEnemy() { return isCheckpoint; }
}
