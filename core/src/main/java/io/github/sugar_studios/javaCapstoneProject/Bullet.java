package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Bullet extends GameObject {

    private final Faction faction;
    private final float damage;
    private final float spriteSize;

    private final Vector2 direction = new Vector2();
    private final Vector2 velocity = new Vector2();
    private final Vector2 targetVelocity = new Vector2();

    private static final Vector2 TMP_VEC2 = new Vector2();

    private final float acceleration;
    private float delayTimer;
    private float rotation;

    private boolean markedForRemoval = false;

    public Bullet(
        float x, float y, float w, float h, Texture texture,
        Faction faction, float damage, float speed,
        float angleDeg, float acceleration, float delayTime) {

        super(x, y, w * hitboxRatio(faction, w), h * hitboxRatio(faction, w), texture);
        this.spriteSize = w;
        this.faction = faction;
        this.damage = damage;
        this.acceleration = MathUtils.clamp(acceleration, 0f, 1f);
        this.delayTimer = Math.max(0.01f, delayTime);
        speed = Math.max(0.01f, speed);

        direction.set(1, 0).setAngleDeg(angleDeg).nor();
        targetVelocity.set(direction).scl(speed);
        velocity.setZero();
        this.rotation = angleDeg;
    }

    public Bullet(
        float x, float y, float w, float h, Texture texture,
        Faction faction, float damage, float speed,
        GameObject target, float acceleration, float delayTime) {

        super(x, y, w * hitboxRatio(faction, w), h * hitboxRatio(faction, w), texture);
        this.spriteSize = w;
        this.faction = faction;
        this.damage = damage;
        this.acceleration = MathUtils.clamp(acceleration, 0f, 1f);
        this.delayTimer = Math.max(0f, delayTime);

        direction.set(computeDirection(x, y, target));
        targetVelocity.set(direction).scl(speed);
        velocity.setZero();
        this.rotation = direction.angleDeg();
    }

    @Override
    public void update(float deltaTime) {
        if (delayTimer > 0f) { delayTimer -= deltaTime; return; }

        float lerpFactor = 1f - (float) Math.pow(1f - acceleration, deltaTime * 60f);
        velocity.lerp(targetVelocity, lerpFactor);

        rect.x += velocity.x * deltaTime;
        rect.y += velocity.y * deltaTime;

        if (!velocity.isZero(0.0001f)) rotation = velocity.angleDeg();
    }

    @Override
    public void draw(Batch batch) {
        if (texture == null) return;
        float offset = (spriteSize - rect.width) * 0.5f;
        batch.draw(texture,
            rect.x - offset, rect.y - offset,
            spriteSize * 0.5f, spriteSize * 0.5f,
            spriteSize, spriteSize,
            1f, 1f, rotation - 90f,
            0, 0, texture.getWidth(), texture.getHeight(),
            false, false);
    }

    public boolean isOffScreen(float worldWidth, float worldHeight) {
        return rect.x + rect.width < 0f || rect.x > worldWidth ||
            rect.y + rect.height < 0f || rect.y > worldHeight;
    }

    public void markForRemoval() { markedForRemoval = true; }
    public boolean isMarkedForRemoval() { return markedForRemoval; }
    public Faction getFaction() { return faction; }
    public float getDamage() { return damage; }

    private static float hitboxRatio(Faction faction, float size) {
        if (faction == Faction.PLAYER) return 1.0f;
        if (size >= 90f) return 0.75f;
        return 0.55f;
    }

    private static Vector2 computeDirection(float sx, float sy, GameObject target) {
        Vector2 out = new Vector2();
        target.getCenter(TMP_VEC2);
        float dx = TMP_VEC2.x - sx;
        float dy = TMP_VEC2.y - sy;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len != 0f) { out.x = dx / len; out.y = dy / len; }
        return out;
    }
}
