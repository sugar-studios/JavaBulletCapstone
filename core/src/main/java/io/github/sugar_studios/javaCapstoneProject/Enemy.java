package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

/**
 * An enemy that follows waypoints and fires using a ShootBehavior.
 */
public class Enemy extends Entity {

    private final Vector2[] waypoints;
    private int waypointIndex;
    private final float speed;
    private final boolean isCheckpoint;

    private final ShootBehavior shootBehavior;
    private float shotTimer;

    private EnemyAnimData anim;
    private Texture bulletTexture; // reference only — owned by EnemyAnimRegistry cache
    private int idleFrame;
    private float idleTimer;
    private boolean isAttacking;
    private int attackFrame;
    private float attackTimer;

    private boolean markedForRemoval;

    public Enemy(Vector2[] waypoints, float speed, boolean isCheckpoint, ShootBehavior behavior, float health) {
        super(waypoints[0].x, waypoints[0].y, 0f, 0f, null, Faction.ENEMY, health);
        this.waypoints = waypoints;
        this.waypointIndex = 1;
        this.speed = speed;
        this.isCheckpoint = isCheckpoint;
        this.shootBehavior = behavior;
        this.shotTimer = (behavior != null) ? behavior.firstShotDelay : Float.MAX_VALUE;
    }

    public void initAnim(String animKey, String bulletPath) {
        anim = EnemyAnimRegistry.get(animKey != null ? animKey : "placeholder");
        rect.width    = anim.displayW();
        rect.height   = anim.displayH();
        bulletTexture = EnemyAnimRegistry.getBulletTexture(bulletPath);
    }

    public void update(float delta, Player player) {
        if (anim == null || !isAlive()) { markedForRemoval = true; return; }
        updateMovement(delta);
        updateShooting(delta, player);
        updateAnimation(delta);
    }

    @Override
    public void update(float delta) { update(delta, null); }

    @Override
    public void draw(Batch batch) {
        if (anim == null) return;
        Texture frame = (isAttacking && anim.hasAttackAnim())
            ? anim.attackFrames()[attackFrame]
            : anim.idleFrames()[idleFrame];
        batch.draw(frame, rect.x, rect.y, rect.width, rect.height);
    }

    private void updateMovement(float delta) {
        if (waypointIndex >= waypoints.length) {
            if (!isCheckpoint) markedForRemoval = true;
            return;
        }
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

    private void fireBullet(Player player) {
        float size = shootBehavior.bulletSize;
        float bx = rect.x + (rect.width  - size) / 2f;
        float by = rect.y + (rect.height - size) / 2f;
        Bullet b;
        if (shootBehavior.aimsAtPlayer && player != null) {
            b = new Bullet(bx, by, size, size, bulletTexture,
                Faction.ENEMY, shootBehavior.damage, shootBehavior.bulletSpeed,
                player, shootBehavior.acceleration, 0f);
        } else {
            b = new Bullet(bx, by, size, size, bulletTexture,
                Faction.ENEMY, shootBehavior.damage, shootBehavior.bulletSpeed,
                shootBehavior.fixedAngle, shootBehavior.acceleration, 0f);
        }
        BulletPool.add(b);
        if (anim.hasAttackAnim()) {
            isAttacking = true;
            attackFrame = 0;
            attackTimer = 0f;
        }
    }

    private void updateAnimation(float delta) {
        idleTimer += delta;
        if (idleTimer >= anim.idleFrameDur() && anim.idleFrames().length > 1) {
            idleTimer -= anim.idleFrameDur();
            idleFrame = (idleFrame + 1) % anim.idleFrames().length;
        }
        if (!isAttacking || !anim.hasAttackAnim()) return;
        attackTimer += delta;
        if (attackTimer >= anim.attackFrameDur()) {
            attackTimer -= anim.attackFrameDur();
            attackFrame++;
            if (attackFrame >= anim.attackFrames().length) {
                isAttacking = false;
                attackFrame = 0;
            }
        }
    }

    public boolean isMarkedForRemoval() { return markedForRemoval; }
    public boolean isCheckpointEnemy()  { return isCheckpoint; }
}
