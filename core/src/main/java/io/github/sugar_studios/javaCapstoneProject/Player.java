package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.sugar_studios.javaCapstoneProject.Keybinds.Action;
import java.util.ArrayList;

// Player-controlled entity. Handles movement, animation, dashing, shooting,
// sword slash, bomb, shot/sword tier progression, and the life-loss cycle.
//
// Life state machine:
//   ALIVE       → take hit      → DYING      (death anim plays, no input)
//   DYING       → anim ends     → RESPAWNING (teleport to spawn, respawn anim)
//   RESPAWNING  → anim ends     → INVULN     (0.5 s invuln window, full control)
//   INVULN      → timer ends    → ALIVE
public class Player extends Entity {

    private static final byte MAX_HEALTH = 3;

    private static final float PLAYER_W = 96f;
    private static final float PLAYER_H = 96f;
    private static final float HITBOX_W = 8f;
    private static final float HITBOX_H = 8f;
    private static final float HITBOX_X_OFFSET_BECAUSE_THE_SPRITE_IS_LOPSIDED_BECAUSE_F_ME = 19f;

    private static final float PLAYER_SPEED = 750f;
    private static final float VERTICAL_SPEED_BONUS = 1.15f;
    private static final float FOCUS_SPEED = 300f;

    private static final int FRAME_COUNT = 6;
    private static final float FRAME_DUR_NORMAL = 1f / 15f;
    private static final float FRAME_DUR_BACKWARD = 1f / 10f;

    private static final float FOCUS_SPRITE_ALPHA = 0.4f;
    private static final float FOCUS_CIRCLE_SIZE = HITBOX_H;
    private static final byte FOCUS_CIRCLE_RES = 32;

    private static final float DASH_SPEED = 2500f;
    private static final float DASH_LENGTH = 200f;
    private static final float DASH_COOLDOWN = .5f;

    private static final byte TRAIL_COUNT = 5;
    private static final float TRAIL_INTERVAL = 0.04f;
    private static final float TRAIL_LIFETIME = 0.2f;
    private static final float TRAIL_ALPHA_MAX = 0.4f;

    public static final float BACKWARD_SCROLL_MULTIPLIER = 0.85f;

    private static final float PLAYER_BULLET_SIZE = 32f;
    private static final float PLAYER_BULLET_SPEED = 2100f;
    private static final float PLAYER_BULLET_ACCELERATION = 1f;
    private static final float PLAYER_BULLET_DAMAGE = .25f;
    private static final float PLAYER_FOCUS_BULLET_DAMAGE = 1f;
    private static final float SHOOT_COOLDOWN = 0.05f;
    private static final float SHOOT_COOLDOWN_FOCUS = SHOOT_COOLDOWN / 0.75f;
    private static final String PATH_PLAYER_BULLET = "projectiles/bullet11.png";

    private static final Vector2 SHOT_ANCHOR_CENTER = new Vector2(0f, 48f);
    private static final Vector2 SHOT_ANCHOR_BOTTOM_LEFT = new Vector2(-40f, 0f);
    private static final Vector2 SHOT_ANCHOR_BOTTOM_RIGHT = new Vector2(40f, 0f);

    private static final int MAX_SHOT_TIER = 4;
    private static final float RED_METER_BASE = 100f;
    private static final float RED_METER_PER_TIER = 50f;

    private static final String PATH_SLASH_ACTIVE = "player/slash/active/attack_normal";
    private static final String PATH_SLASH_END = "player/slash/end/attack_normal_end";
    private static final int SLASH_ACTIVE_FRAMES = 9;
    private static final int SLASH_END_FRAMES = 6;
    private static final float SLASH_FRAME_DUR = 1f / 30f;
    private static final float SLASH_END_FRAME_DUR = 1f / 30f;
    private static final int SLASH_HIT_FIRST = 1;
    private static final int SLASH_HIT_LAST = 4;
    private static final float SLASH_SPRITE_OFFSET_X = 0f;
    private static final float SLASH_SPRITE_OFFSET_Y = 20f;
    private static final float[] SLASH_DAMAGE_BY_TIER = { 8f, 12f, 18f, 22f, 27f };
    private static final int MAX_SWORD_TIER = 4;
    private static final float BLACK_METER_BASE = 100f;
    private static final float BLACK_METER_PER_TIER = 50f;
    private static final float SLASH_HITBOX_W = 140f;
    private static final float SLASH_HITBOX_H = 100f;

    private static final String PATH_FORWARD = "player/run_forward/run_forward_trim";
    private static final String PATH_BACKWARD = "player/run_backward/run_backward";
    private static final String PATH_LEFT = "player/run_left/run_left_trim";
    private static final String PATH_RIGHT = "player/run_right/run_right_trim";
    private static final String PATH_DASH = "player/dash/dodge.png";
    private static final String PATH_BOMB = "projectiles/bomb.png";
    private static final String PATH_DEATH = "player/die/hurt";
    private static final String PATH_RESPAWN = "player/spawn/respawn";
    private static final String PNG = ".png";

    private static final int DEATH_FRAME_COUNT = 18;
    private static final int RESPAWN_FRAME_COUNT = 12;
    private static final float DEATH_FRAME_DUR = 1f / 18f;
    private static final float RESPAWN_FRAME_DUR = 1f / 15f;
    private static final float INVULN_DURATION = 0.5f;
    private static final float INVULN_FLICKER_SPEED = 30f;

    private static final float BOMB_MAX_SIZE = 1200f;
    private static final float BOMB_DURATION = 1f;
    private static final float BOMB_SPIN_SPEED = 720f;

    public enum Direction { FORWARD, BACKWARD, LEFT, RIGHT }
    private enum SlashState { NONE, ACTIVE, ENDLAG }
    private enum LifeState { ALIVE, DYING, RESPAWNING, INVULN }

    private final Texture[][] frames;
    private final Texture dashTexture;
    private final Texture focusCircleTexture;
    private final Texture bulletTexture;
    private final Texture bombTexture;
    private final Texture[] slashActiveFrames;
    private final Texture[] slashEndFrames;
    private final Texture[] deathFrames;
    private final Texture[] respawnFrames;

    private Direction currentDirection = Direction.FORWARD;
    private int frameIndex = 0;
    private float frameTimer = 0f;

    private boolean isFocused = false;
    float baseDamage = 0f;

    private boolean isDashing = false;
    private float dashDirX = 0f;
    private float dashDirY = 0f;
    private float dashDistRemaining = 0f;
    private float dashCooldownTimer = 0f;
    private boolean dashFacingRight = true;

    private final Ghost[] trail = new Ghost[TRAIL_COUNT];
    private int trailHead = 0;
    private float trailTimer = 0f;

    private float shootCooldownTimer = 0f;

    private int shotTier = 0;
    private float redMeter = 0f;

    private int swordTier = 0;
    private float blackMeter = 0f;

    private SlashState slashState = SlashState.NONE;
    private int slashFrame = 0;
    private float slashFrameTimer = 0f;
    private boolean slashJustStarted = false;
    private final Rectangle slashHitbox = new Rectangle();

    private LifeState lifeState = LifeState.ALIVE;
    private int lifeAnimFrame = 0;
    private float lifeAnimTimer = 0f;
    private float invulnTimer = 0f;

    private boolean isBombing = false;
    private float bombProgress = 0f;
    private float bombRotation = 0f;
    private float bombX = 0f;
    private float bombY = 0f;

    private final float spawnX;
    private final float spawnY;
    private final float boundsW;
    private final float boundsH;

    public Player(float spawnX, float spawnY, float boundsW, float boundsH) {
        super(spawnX, spawnY, HITBOX_W, HITBOX_H, null, Faction.PLAYER, MAX_HEALTH);
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.boundsW = boundsW;
        this.boundsH = boundsH;
        this.frames = loadFrames();
        this.dashTexture = load(PATH_DASH);
        this.focusCircleTexture = createFocusCircle();
        this.bulletTexture = load(PATH_PLAYER_BULLET);
        this.bombTexture = load(PATH_BOMB);
        this.slashActiveFrames = loadIndexedFrames(PATH_SLASH_ACTIVE, SLASH_ACTIVE_FRAMES);
        this.slashEndFrames = loadIndexedFrames(PATH_SLASH_END, SLASH_END_FRAMES);
        this.deathFrames = loadIndexedFrames(PATH_DEATH, DEATH_FRAME_COUNT);
        this.respawnFrames = loadIndexedFrames(PATH_RESPAWN, RESPAWN_FRAME_COUNT);
        for (int i = 0; i < TRAIL_COUNT; i++) trail[i] = new Ghost();
    }

    // Blocks damage during any non-ALIVE state (dying, respawning, invuln).
    @Override
    public void onBulletHit(Bullet bullet) {
        if (lifeState != LifeState.ALIVE) return;
        super.onBulletHit(bullet);
        if (isAlive()) startDying();
        // if not alive, GameScreen handles game over via isAlive()
    }

    @Override
    public void update(float deltaTime) {
        slashJustStarted = false;

        // Fade ghost trail regardless of life state.
        for (Ghost g : trail) g.alpha -= (TRAIL_ALPHA_MAX / TRAIL_LIFETIME) * deltaTime;

        // During DYING or RESPAWNING: only advance the life animation, no input.
        if (lifeState == LifeState.DYING || lifeState == LifeState.RESPAWNING) {
            updateLifeState(deltaTime);
            return;
        }

        // During INVULN: tick the timer, but full control is restored.
        if (lifeState == LifeState.INVULN) updateLifeState(deltaTime);

        boolean left = Keybinds.isHeld(Action.LEFT);
        boolean right = Keybinds.isHeld(Action.RIGHT);

        if (dashCooldownTimer > 0f) dashCooldownTimer -= deltaTime;
        if (shootCooldownTimer > 0f) shootCooldownTimer -= deltaTime;

        if (isBombing) {
            bombProgress += deltaTime / BOMB_DURATION;
            bombRotation += BOMB_SPIN_SPEED * deltaTime;
            if (bombProgress >= 1f) {
                isBombing = false;
                bombProgress = 0f;
                bombRotation = 0f;
            }
        }

        if (isDashing) {
            updateDash(deltaTime);
        } else {
            isFocused = Keybinds.isHeld(Action.FOCUS);

            if (!isFocused && Keybinds.isJustPressed(Action.DASH) && dashCooldownTimer <= 0f) {
                startDash();
            } else {
                handleMovement(deltaTime);

                if (Keybinds.isJustPressed(Action.SWORD_SLASH) && slashState != SlashState.ACTIVE) {
                    startSlash();
                }

                if (Keybinds.isJustPressed(Action.BOMB) && !isBombing && shotTier >= 1) {
                    isBombing = true;
                    bombProgress = 0f;
                    bombRotation = 0f;
                    bombX = rect.x + HITBOX_W / 2f;
                    bombY = rect.y + HITBOX_H / 2f;
                    shotTier--;
                    redMeter = 0f;
                }

                updateSlash(deltaTime);

                if (slashState == SlashState.NONE && Keybinds.isHeld(Action.SHOOT) && shootCooldownTimer <= 0f) {
                    tryShoot();
                }

                if (slashState == SlashState.NONE) updateAnimation(deltaTime);
            }
        }

        if (isDashing) {
            trailTimer += deltaTime;
            if (trailTimer >= TRAIL_INTERVAL) {
                trailTimer = 0f;
                Ghost g = trail[trailHead];
                g.x = rect.x;
                g.y = rect.y;
                g.alpha = TRAIL_ALPHA_MAX;
                g.facingRight = dashFacingRight;
                trailHead = (trailHead + 1) % TRAIL_COUNT;
            }
        }
    }

    @Override
    public void draw(Batch batch) {
        float baseX = rect.x - (PLAYER_W - HITBOX_W) / 2f
            + HITBOX_X_OFFSET_BECAUSE_THE_SPRITE_IS_LOPSIDED_BECAUSE_F_ME;
        float baseY = rect.y - (PLAYER_H - HITBOX_H) / 2f;

        if (lifeState == LifeState.DYING) {
            batch.draw(deathFrames[lifeAnimFrame], baseX, baseY, PLAYER_W, PLAYER_H);
            return;
        }

        if (lifeState == LifeState.RESPAWNING) {
            batch.draw(respawnFrames[lifeAnimFrame], baseX, baseY, PLAYER_W, PLAYER_H);
            return;
        }

        // INVULN: flicker alpha.
        float drawAlpha = 1f;
        if (lifeState == LifeState.INVULN) {
            drawAlpha = 0.4f + 0.6f * (0.5f + 0.5f * MathUtils.sin(invulnTimer * INVULN_FLICKER_SPEED));
        }

        for (Ghost g : trail) {
            if (g.alpha <= 0f) continue;
            float gx = g.x - (PLAYER_W - HITBOX_W) / 2f
                + HITBOX_X_OFFSET_BECAUSE_THE_SPRITE_IS_LOPSIDED_BECAUSE_F_ME;
            float gy = g.y - (PLAYER_H - HITBOX_H) / 2f;
            batch.setColor(1f, 1f, 1f, g.alpha);
            drawSprite(batch, dashTexture, gx, gy, g.facingRight);
        }
        batch.setColor(1f, 1f, 1f, drawAlpha);

        if (isDashing) {
            drawSprite(batch, dashTexture, baseX, baseY, dashFacingRight);
        } else if (slashState == SlashState.ACTIVE) {
            float sw = PLAYER_W * 1.4f;
            float sh = PLAYER_H * 1.4f;
            float sx = baseX - (sw - PLAYER_W) / 2f + SLASH_SPRITE_OFFSET_X;
            float sy = baseY - (sh - PLAYER_H) / 2f + SLASH_SPRITE_OFFSET_Y;
            drawSprite(batch, slashActiveFrames[slashFrame], sx, sy, sw, sh, dashFacingRight);
        } else if (slashState == SlashState.ENDLAG) {
            float sw = PLAYER_W * 1.4f;
            float sh = PLAYER_H * 1.4f;
            float sx = baseX - (sw - PLAYER_W) / 2f + SLASH_SPRITE_OFFSET_X;
            float sy = baseY - (sh - PLAYER_H) / 2f + SLASH_SPRITE_OFFSET_Y;
            drawSprite(batch, slashEndFrames[slashFrame], sx, sy, sw, sh, dashFacingRight);
        } else if (isFocused) {
            batch.setColor(1f, 1f, 1f, FOCUS_SPRITE_ALPHA);
            batch.draw(frames[currentDirection.ordinal()][frameIndex], baseX, baseY, PLAYER_W, PLAYER_H);
            batch.setColor(Color.WHITE);
            float cx = rect.x + HITBOX_W / 2f - FOCUS_CIRCLE_SIZE / 2f;
            float cy = rect.y + HITBOX_H / 2f - FOCUS_CIRCLE_SIZE / 2f;
            batch.draw(focusCircleTexture, cx, cy, FOCUS_CIRCLE_SIZE, FOCUS_CIRCLE_SIZE);
        } else {
            batch.draw(frames[currentDirection.ordinal()][frameIndex], baseX, baseY, PLAYER_W, PLAYER_H);
        }
        batch.setColor(Color.WHITE);

        if (isBombing) {
            float size = BOMB_MAX_SIZE * (float) Math.sqrt(bombProgress) * 1.75f;
            int iw = bombTexture.getWidth();
            int ih = bombTexture.getHeight();
            batch.draw(bombTexture,
                bombX - size / 2f, bombY - size / 2f,
                size / 2f, size / 2f,
                size, size,
                1f, 1f, bombRotation,
                0, 0, iw, ih,
                false, false);
        }
    }

    public boolean isSlashJustStarted() { return slashJustStarted; }
    public boolean isBombActive() { return isBombing; }

    public boolean isSlashHitboxActive() {
        return slashState == SlashState.ACTIVE
            && slashFrame >= SLASH_HIT_FIRST
            && slashFrame <= SLASH_HIT_LAST;
    }

    public Rectangle getSlashHitbox() {
        float cy = rect.y + HITBOX_H / 2f - SLASH_HITBOX_H / 2f;
        float hx = dashFacingRight ? rect.x + HITBOX_W : rect.x - SLASH_HITBOX_W;
        slashHitbox.set(hx, cy, SLASH_HITBOX_W, SLASH_HITBOX_H);
        return slashHitbox;
    }

    public float getSlashDamage() { return SLASH_DAMAGE_BY_TIER[swordTier]; }

    public void drawSlashDebug(com.badlogic.gdx.graphics.glutils.ShapeRenderer sr, Color color) {
        if (!isSlashHitboxActive()) return;
        Rectangle h = getSlashHitbox();
        sr.setColor(color);
        sr.rect(h.x, h.y, h.width, h.height);
    }

    public void addRedProgress(float amount) {
        if (shotTier >= MAX_SHOT_TIER) return;
        redMeter += amount;
        if (redMeter >= redMeterCap()) { redMeter = 0f; shotTier++; }
    }

    public void addBlackProgress(float amount) {
        if (swordTier >= MAX_SWORD_TIER) return;
        blackMeter += amount;
        if (blackMeter >= blackMeterCap()) { blackMeter = 0f; swordTier++; }
    }

    public int getShotTier() { return shotTier; }
    public float getRedMeter() { return redMeter; }
    public float getRedMeterCap() { return redMeterCap(); }
    public int getSwordTier() { return swordTier; }
    public float getBlackMeter() { return blackMeter; }
    public float getBlackMeterCap() { return blackMeterCap(); }

    public float getShotPowerDisplay() {
        if (shotTier >= MAX_SHOT_TIER) return MAX_SHOT_TIER;
        return shotTier + redMeter / redMeterCap();
    }

    public float getSwordPowerDisplay() {
        if (swordTier >= MAX_SWORD_TIER) return MAX_SWORD_TIER;
        return swordTier + blackMeter / blackMeterCap();
    }

    public float getScrollMultiplier() {
        if (isDashing) return 1f;
        return (currentDirection == Direction.BACKWARD) ? BACKWARD_SCROLL_MULTIPLIER : 1f;
    }

    public void dispose() {
        for (Texture[] dir : frames)
            for (Texture t : dir) if (t != null) t.dispose();
        if (dashTexture != null) dashTexture.dispose();
        if (focusCircleTexture != null) focusCircleTexture.dispose();
        if (bulletTexture != null) bulletTexture.dispose();
        if (bombTexture != null) bombTexture.dispose();
        for (Texture t : slashActiveFrames) if (t != null) t.dispose();
        for (Texture t : slashEndFrames) if (t != null) t.dispose();
        for (Texture t : deathFrames) if (t != null) t.dispose();
        for (Texture t : respawnFrames) if (t != null) t.dispose();
    }

    // Transitions to DYING — cancels all actions.
    private void startDying() {
        lifeState = LifeState.DYING;
        lifeAnimFrame = 0;
        lifeAnimTimer = 0f;
        isDashing = false;
        isBombing = false;
        slashState = SlashState.NONE;
        slashFrame = 0;
        slashFrameTimer = 0f;
        SfxPlayer.playDeath();
    }

    private void updateLifeState(float deltaTime) {
        switch (lifeState) {
            case DYING: {
                lifeAnimTimer += deltaTime;
                if (lifeAnimTimer >= DEATH_FRAME_DUR) {
                    lifeAnimTimer -= DEATH_FRAME_DUR;
                    lifeAnimFrame++;
                    if (lifeAnimFrame >= DEATH_FRAME_COUNT) {
                        // Teleport to spawn, begin respawn anim.
                        rect.x = spawnX;
                        rect.y = spawnY;
                        lifeState = LifeState.RESPAWNING;
                        lifeAnimFrame = 0;
                        lifeAnimTimer = 0f;
                    }
                }
                break;
            }
            case RESPAWNING: {
                lifeAnimTimer += deltaTime;
                if (lifeAnimTimer >= RESPAWN_FRAME_DUR) {
                    lifeAnimTimer -= RESPAWN_FRAME_DUR;
                    lifeAnimFrame++;
                    if (lifeAnimFrame >= RESPAWN_FRAME_COUNT) {
                        lifeState = LifeState.INVULN;
                        lifeAnimFrame = 0;
                        lifeAnimTimer = 0f;
                        invulnTimer = INVULN_DURATION;
                    }
                }
                break;
            }
            case INVULN: {
                invulnTimer -= deltaTime;
                if (invulnTimer <= 0f) lifeState = LifeState.ALIVE;
                break;
            }
            default:
                break;
        }
    }

    private void startSlash() {
        slashState = SlashState.ACTIVE;
        slashFrame = 0;
        slashFrameTimer = 0f;
        slashJustStarted = true;
        SfxPlayer.playSwordSwing();
    }

    private void updateSlash(float deltaTime) {
        if (slashState == SlashState.NONE) return;
        float dur = (slashState == SlashState.ACTIVE) ? SLASH_FRAME_DUR : SLASH_END_FRAME_DUR;
        int total = (slashState == SlashState.ACTIVE) ? SLASH_ACTIVE_FRAMES : SLASH_END_FRAMES;
        slashFrameTimer += deltaTime;
        if (slashFrameTimer >= dur) {
            slashFrameTimer -= dur;
            slashFrame++;
            if (slashFrame >= total) {
                slashState = (slashState == SlashState.ACTIVE) ? SlashState.ENDLAG : SlashState.NONE;
                slashFrame = 0;
            }
        }
    }

    private void handleMovement(float deltaTime) {
        boolean up = Keybinds.isHeld(Action.UP);
        boolean down = Keybinds.isHeld(Action.DOWN);
        boolean left = Keybinds.isHeld(Action.LEFT);
        boolean right = Keybinds.isHeld(Action.RIGHT);
        if (right && left) right = left = false;

        float dx = 0f, dy = 0f;
        if (left) dx -= 1f;
        if (right) dx += 1f;
        if (up) dy += 1f;
        if (down) dy -= 1f;

        if (isFocused) {
            if (dx != 0f && dy != 0f) { float inv = 1f / (float) Math.sqrt(2.0); dx *= inv; dy *= inv; }
            rect.x = MathUtils.clamp(rect.x + dx * FOCUS_SPEED * deltaTime, 0f, boundsW - rect.width);
            rect.y = MathUtils.clamp(rect.y + dy * FOCUS_SPEED * deltaTime, 0f, boundsH - rect.height);
        } else {
            if (dx != 0f && dy != 0f) { float inv = 1f / (float) Math.sqrt(2.0); dx *= inv; dy *= inv; }
            rect.x = MathUtils.clamp(rect.x + dx * PLAYER_SPEED * deltaTime, 0f, boundsW - rect.width);
            rect.y = MathUtils.clamp(rect.y + dy * PLAYER_SPEED * VERTICAL_SPEED_BONUS * deltaTime, 0f, boundsH - rect.height);
        }

        if (slashState == SlashState.NONE) {
            if (right) dashFacingRight = true;
            if (left) dashFacingRight = false;
        }

        Direction next;
        if (down) next = Direction.BACKWARD;
        else if (left) next = Direction.LEFT;
        else if (right) next = Direction.RIGHT;
        else next = Direction.FORWARD;
        if (slashState == SlashState.NONE) currentDirection = next;
    }

    private void updateAnimation(float deltaTime) {
        float frameDur = (currentDirection == Direction.BACKWARD) ? FRAME_DUR_BACKWARD : FRAME_DUR_NORMAL;
        frameTimer += deltaTime;
        if (frameTimer >= frameDur) {
            frameTimer -= frameDur;
            frameIndex = (frameIndex + 1) % FRAME_COUNT;
        }
    }

    private void tryShoot() {
        SfxPlayer.playShoot();
        baseDamage = isFocused
            ? PLAYER_FOCUS_BULLET_DAMAGE
            : PLAYER_BULLET_DAMAGE;
        switch (shotTier) {
            case 0:
            case 1:
                spawnBullet(SHOT_ANCHOR_CENTER, 90f, baseDamage);
                break;
            case 2:
                spawnBullet(SHOT_ANCHOR_BOTTOM_LEFT, 90f, baseDamage);
                spawnBullet(SHOT_ANCHOR_BOTTOM_RIGHT, 90f, baseDamage);
                break;
            case 3:
                float homing3 = angleToNearestEnemy();
                spawnBullet(SHOT_ANCHOR_BOTTOM_LEFT, 90f, baseDamage);
                spawnBullet(SHOT_ANCHOR_CENTER, homing3, baseDamage * 0.25f);
                spawnBullet(SHOT_ANCHOR_BOTTOM_RIGHT, 90f, baseDamage);
                break;
            case 4:
                float homing4 = angleToNearestEnemy();
                spawnBullet(SHOT_ANCHOR_BOTTOM_LEFT, 90f, baseDamage);
                spawnBullet(SHOT_ANCHOR_CENTER, homing4, baseDamage * 0.25f);
                spawnBullet(SHOT_ANCHOR_CENTER, 90f, baseDamage);
                spawnBullet(SHOT_ANCHOR_BOTTOM_RIGHT, 90f, baseDamage);
                break;
        }
        shootCooldownTimer = isFocused ? SHOOT_COOLDOWN_FOCUS : SHOOT_COOLDOWN;
    }

    private void spawnBullet(Vector2 anchor, float angle, float damage) {
        float cx = rect.x + HITBOX_W / 2f;
        float cy = rect.y + HITBOX_H / 2f;
        float bx = cx + anchor.x - PLAYER_BULLET_SIZE / 2f;
        float by = cy + anchor.y - PLAYER_BULLET_SIZE / 2f;
        BulletPool.add(new Bullet(bx, by, PLAYER_BULLET_SIZE, PLAYER_BULLET_SIZE,
            bulletTexture, Faction.PLAYER, damage,
            PLAYER_BULLET_SPEED, angle, PLAYER_BULLET_ACCELERATION, 0f));
    }

    private float angleToNearestEnemy() {
        ArrayList<Enemy> enemies = EnemyPool.getActive();
        float cx = rect.x + HITBOX_W / 2f;
        float cy = rect.y + HITBOX_H / 2f;
        float bestDist = Float.MAX_VALUE;
        Enemy nearest = null;
        for (Enemy e : enemies) {
            if (!e.isAlive() || e.isMarkedForRemoval()) continue;
            float ex = e.rect.x + e.rect.width / 2f;
            float ey = e.rect.y + e.rect.height / 2f;
            float d = (ex - cx) * (ex - cx) + (ey - cy) * (ey - cy);
            if (d < bestDist) { bestDist = d; nearest = e; }
        }
        if (nearest == null) return 90f;
        float ex = nearest.rect.x + nearest.rect.width / 2f;
        float ey = nearest.rect.y + nearest.rect.height / 2f;
        return MathUtils.atan2(ey - cy, ex - cx) * MathUtils.radiansToDegrees;
    }

    private float redMeterCap() { return RED_METER_BASE + RED_METER_PER_TIER * shotTier; }
    private float blackMeterCap() { return BLACK_METER_BASE + BLACK_METER_PER_TIER * swordTier; }

    private void startDash() {
        slashState = SlashState.NONE;
        slashFrame = 0;
        slashFrameTimer = 0f;
        swordTier = Math.max(0, swordTier - 1);
        blackMeter = 0f;

        boolean up = Keybinds.isHeld(Action.UP);
        boolean down = Keybinds.isHeld(Action.DOWN);
        boolean left = Keybinds.isHeld(Action.LEFT);
        boolean right = Keybinds.isHeld(Action.RIGHT);

        float dx = 0f, dy = 0f;
        if (left) dx -= 1f;
        if (right) dx += 1f;
        if (up) dy += 1f;
        if (down) dy -= 1f;

        if (dx == 0f && dy == 0f) {
            dx = dashFacingRight ? 1f : -1f;
        } else if (dx != 0f && dy != 0f) {
            float inv = 1f / (float) Math.sqrt(2.0);
            dx *= inv; dy *= inv;
        }

        dashDirX = dx;
        dashDirY = dy;
        dashDistRemaining = DASH_LENGTH;
        dashCooldownTimer = DASH_COOLDOWN;
        isDashing = true;
        trailTimer = TRAIL_INTERVAL;
    }

    private void updateDash(float deltaTime) {
        float move = DASH_SPEED * deltaTime;
        float newX = rect.x + dashDirX * move;
        float newY = rect.y + dashDirY * move;
        float clampX = MathUtils.clamp(newX, 0f, boundsW - rect.width);
        float clampY = MathUtils.clamp(newY, 0f, boundsH - rect.height);
        boolean hitBounds = (clampX != newX || clampY != newY);
        rect.x = clampX;
        rect.y = clampY;
        dashDistRemaining -= move;
        if (dashDistRemaining <= 0f || hitBounds) isDashing = false;
    }

    private static void drawSprite(Batch batch, Texture tex, float drawX, float drawY, boolean facingRight) {
        if (facingRight) batch.draw(tex, drawX, drawY, PLAYER_W, PLAYER_H);
        else batch.draw(tex, drawX + PLAYER_W, drawY, -PLAYER_W, PLAYER_H);
    }

    private static void drawSprite(Batch batch, Texture tex, float drawX, float drawY, float w, float h, boolean facingRight) {
        if (facingRight) batch.draw(tex, drawX, drawY, w, h);
        else batch.draw(tex, drawX + w, drawY, -w, h);
    }

    private static Texture[][] loadFrames() {
        Texture[][] f = new Texture[Direction.values().length][FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            int n = i + 1;
            f[Direction.FORWARD.ordinal()][i] = load(PATH_FORWARD + n + PNG);
            f[Direction.BACKWARD.ordinal()][i] = load(PATH_BACKWARD + n + PNG);
            f[Direction.LEFT.ordinal()][i] = load(PATH_LEFT + n + PNG);
            f[Direction.RIGHT.ordinal()][i] = load(PATH_RIGHT + n + PNG);
        }
        return f;
    }

    private static Texture[] loadIndexedFrames(String basePath, int count) {
        Texture[] arr = new Texture[count];
        for (int i = 0; i < count; i++) arr[i] = load(basePath + (i + 1) + PNG);
        return arr;
    }

    private static Texture createFocusCircle() {
        Pixmap pm = new Pixmap(FOCUS_CIRCLE_RES, FOCUS_CIRCLE_RES, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fillCircle(FOCUS_CIRCLE_RES / 2, FOCUS_CIRCLE_RES / 2, FOCUS_CIRCLE_RES / 2 - 1);
        Texture t = new Texture(pm);
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pm.dispose();
        return t;
    }

    private static Texture load(String path) {
        Texture t = new Texture(Gdx.files.internal(path));
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return t;
    }

    private static class Ghost {
        float x, y, alpha;
        boolean facingRight;
    }
}
