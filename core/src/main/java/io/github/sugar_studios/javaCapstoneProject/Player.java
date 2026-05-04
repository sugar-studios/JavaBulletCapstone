package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import io.github.sugar_studios.javaCapstoneProject.Keybinds.Action;

/**
 * The player-controlled entity handling movement, actions, and shooting.
 * Uses the Keybinds class to determine inputs
 * by defualt ->
 * up is up
 * down is down
 * left is left
 * right is right
 * space is dash
 * z is shoot
 * and shift is focus
 *
 * @TODO X is SWORD ATTACK Z IS BOMB
 */
public class Player extends Entity {
    private static final byte  MAX_HEALTH = 3;

    private static final float PLAYER_W = 96f;
    private static final float PLAYER_H = 96f;
    private static final float HITBOX_W = 8f;
    private static final float HITBOX_H = 8f;
    private static final float HITBOX_X_OFFSET_BECAUSE_THE_SPRITE_IS_LOPSIDED_BECAUSE_F_ME = 19f;

    private static final float PLAYER_SPEED = 750f;
    private static final float VERTICAL_SPEED_BONUS = 1.15f;
    private static final float FOCUS_SPEED = 300f;

    private static final int   FRAME_COUNT = 6;
    private static final float FRAME_DUR_NORMAL = 1f / 15f;
    private static final float FRAME_DUR_BACKWARD  = 1f / 10f;

    private static final float FOCUS_SPRITE_ALPHA = 0.4f;
    private static final float FOCUS_CIRCLE_SIZE = HITBOX_H;
    private static final byte  FOCUS_CIRCLE_RES = 32;

    private static final float DASH_SPEED = 2500f;
    private static final float DASH_LENGTH = 200f;
    private static final float DASH_COOLDOWN  = .5f;

    private static final byte  TRAIL_COUNT = 5;
    private static final float TRAIL_INTERVAL = 0.04f;
    private static final float TRAIL_LIFETIME = 0.2f;
    private static final float TRAIL_ALPHA_MAX = 0.4f;

    public static final float BACKWARD_SCROLL_MULTIPLIER = 0.85f;

    private static final float PLAYER_BULLET_SIZE = 32f;
    private static final float PLAYER_BULLET_SPEED = 2100f;
    private static final float PLAYER_BULLET_ACCELERATION = 1f;
    private static final float PLAYER_BULLET_DAMAGE = 1f;
    private static final float SHOOT_COOLDOWN = 0.05f;
    private static final String PATH_PLAYER_BULLET = "projectiles/bullet11.png";

    private static final String PATH_FORWARD = "player/run_forward/run_forward_trim";
    private static final String PATH_BACKWARD = "player/run_backward/run_backward";
    private static final String PATH_LEFT = "player/run_left/run_left_trim";
    private static final String PATH_RIGHT = "player/run_right/run_right_trim";
    private static final String PATH_DASH = "player/dash/dodge.png";
    private static final String PNG = ".png";

    public enum Direction { FORWARD, BACKWARD, LEFT, RIGHT }

    private final Texture[][] frames;
    private final Texture     dashTexture;
    private final Texture     focusCircleTexture;
    private final Texture     bulletTexture;

    private Direction currentDirection = Direction.FORWARD;
    private int       frameIndex       = 0;
    private float     frameTimer       = 0f;

    private boolean isFocused = false;

    private boolean isDashing          = false;
    private float   dashDirX           = 0f;
    private float   dashDirY           = 0f;
    private float   dashDistRemaining  = 0f;
    private float   dashCooldownTimer  = 0f;
    private boolean dashFacingRight    = true;

    //AI
    private final Ghost[] trail = new Ghost[TRAIL_COUNT];
    private int   trailHead     = 0;
    private float trailTimer    = 0f;

    private float shootCooldownTimer = 0f;

    private final float boundsW;
    private final float boundsH;


    public Player(float spawnX, float spawnY, float boundsW, float boundsH) {
        super(spawnX, spawnY, HITBOX_W, HITBOX_H, null, Faction.PLAYER, MAX_HEALTH);
        this.boundsW = boundsW;
        this.boundsH = boundsH;
        this.frames = loadFrames();
        this.dashTexture = load(PATH_DASH);
        this.focusCircleTexture = createFocusCircle();
        this.bulletTexture  = load(PATH_PLAYER_BULLET);
        for (int i = 0; i < TRAIL_COUNT; i++) trail[i] = new Ghost(); //AI
    }


    @Override
    public void update(float deltaTime) {
        boolean left  = Keybinds.isHeld(Action.LEFT);
        boolean right = Keybinds.isHeld(Action.RIGHT);
        if (right) dashFacingRight = true;
        if (left)  dashFacingRight = false;

        if (dashCooldownTimer  > 0f) dashCooldownTimer  -= deltaTime;
        if (shootCooldownTimer > 0f) shootCooldownTimer -= deltaTime;

        if (isDashing) {
            updateDash(deltaTime);
        } else {
            isFocused = Keybinds.isHeld(Action.FOCUS);

            if (!isFocused && Keybinds.isJustPressed(Action.DASH) && dashCooldownTimer <= 0f) {
                startDash();
            } else {
                handleMovement(deltaTime);
                updateAnimation(deltaTime);

                if (Keybinds.isHeld(Action.SHOOT) && shootCooldownTimer <= 0f) {
                    tryShoot();
                }
            }
        }

        //AI
        for (Ghost g : trail) g.alpha -= (TRAIL_ALPHA_MAX / TRAIL_LIFETIME) * deltaTime;

        //AI
        if (isDashing) {
            trailTimer += deltaTime;
            if (trailTimer >= TRAIL_INTERVAL) {
                trailTimer = 0f;
                Ghost g = trail[trailHead];
                g.x           = rect.x;
                g.y           = rect.y;
                g.alpha       = TRAIL_ALPHA_MAX;
                g.facingRight = dashFacingRight;
                trailHead     = (trailHead + 1) % TRAIL_COUNT;
            }
        }
    }


    @Override
    public void draw(Batch batch) {
        float baseX = rect.x - (PLAYER_W - HITBOX_W) / 2f
            + HITBOX_X_OFFSET_BECAUSE_THE_SPRITE_IS_LOPSIDED_BECAUSE_F_ME;
        float baseY = rect.y - (PLAYER_H - HITBOX_H) / 2f;

        // Trail
        for (Ghost g : trail) {
            if (g.alpha <= 0f) continue;
            float gx = g.x - (PLAYER_W - HITBOX_W) / 2f
                + HITBOX_X_OFFSET_BECAUSE_THE_SPRITE_IS_LOPSIDED_BECAUSE_F_ME;
            float gy = g.y - (PLAYER_H - HITBOX_H) / 2f;
            batch.setColor(1f, 1f, 1f, g.alpha);
            drawSprite(batch, dashTexture, gx, gy, g.facingRight);
        }
        batch.setColor(Color.WHITE);

        if (isDashing) {
            drawSprite(batch, dashTexture, baseX, baseY, dashFacingRight);
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
    }


    public float getScrollMultiplier() {
        if (isDashing) return 1f;
        return (currentDirection == Direction.BACKWARD) ? BACKWARD_SCROLL_MULTIPLIER : 1f;
    }

    public void dispose() {
        for (Texture[] dir : frames)
            for (Texture t : dir)
                if (t != null) t.dispose();
        if (dashTexture != null) dashTexture.dispose();
        if (focusCircleTexture != null) focusCircleTexture.dispose();
        if (bulletTexture != null) bulletTexture.dispose();
    }


    private void handleMovement(float deltaTime) {
        boolean up = Keybinds.isHeld(Action.UP);
        boolean down = Keybinds.isHeld(Action.DOWN);
        boolean left = Keybinds.isHeld(Action.LEFT);
        boolean right = Keybinds.isHeld(Action.RIGHT);
        if (right && left) right = left = false;

        float dx = 0f, dy = 0f;
        if (left)  dx -= 1f;
        if (right) dx += 1f;
        if (up)    dy += 1f;
        if (down)  dy -= 1f;

        if (isFocused) {
            if (dx != 0f && dy != 0f) { float inv = 1f / (float) Math.sqrt(2.0); dx *= inv; dy *= inv; }
            rect.x = MathUtils.clamp(rect.x + dx * FOCUS_SPEED * deltaTime, 0f, boundsW - rect.width);
            rect.y = MathUtils.clamp(rect.y + dy * FOCUS_SPEED * deltaTime, 0f, boundsH - rect.height);
        } else {
            if (dx != 0f && dy != 0f) { float inv = 1f / (float) Math.sqrt(2.0); dx *= inv; dy *= inv; }
            rect.x = MathUtils.clamp(rect.x + dx * PLAYER_SPEED * deltaTime, 0f, boundsW - rect.width);
            rect.y = MathUtils.clamp(rect.y + dy * PLAYER_SPEED * VERTICAL_SPEED_BONUS * deltaTime, 0f, boundsH - rect.height);
        }

        Direction next;
        if (down) next = Direction.BACKWARD;
        else if (left) next = Direction.LEFT;
        else if (right) next = Direction.RIGHT;
        else next = Direction.FORWARD;
        currentDirection = next;
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
        float spawnX = rect.x + HITBOX_W / 2f - PLAYER_BULLET_SIZE / 2f;
        float spawnY = rect.y + (PLAYER_H + HITBOX_H) / 2f;

        Bullet b = new Bullet(
            spawnX, spawnY,
            PLAYER_BULLET_SIZE, PLAYER_BULLET_SIZE,
            bulletTexture,
            Faction.PLAYER,
            PLAYER_BULLET_DAMAGE,
            PLAYER_BULLET_SPEED,
            90f,
            PLAYER_BULLET_ACCELERATION,
            0f
        );

        BulletPool.add(b);
        shootCooldownTimer = SHOOT_COOLDOWN;
    }

    private void startDash() {
        boolean up = Keybinds.isHeld(Action.UP);
        boolean down = Keybinds.isHeld(Action.DOWN);
        boolean left = Keybinds.isHeld(Action.LEFT);
        boolean right = Keybinds.isHeld(Action.RIGHT);

        float dx = 0f, dy = 0f;
        if (left)  dx -= 1f;
        if (right) dx += 1f;
        if (up)    dy += 1f;
        if (down)  dy -= 1f;

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
        if (facingRight) batch.draw(tex, drawX, drawY, PLAYER_W,  PLAYER_H);
        else batch.draw(tex, drawX + PLAYER_W, drawY, -PLAYER_W, PLAYER_H);
    }

    private static Texture[][] loadFrames() {
        Texture[][] f = new Texture[Direction.values().length][FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            int n = i + 1;
            f[Direction.FORWARD.ordinal()][i]  = load(PATH_FORWARD + n + PNG);
            f[Direction.BACKWARD.ordinal()][i] = load(PATH_BACKWARD + n + PNG);
            f[Direction.LEFT.ordinal()][i] = load(PATH_LEFT + n + PNG);
            f[Direction.RIGHT.ordinal()][i] = load(PATH_RIGHT + n + PNG);
        }
        return f;
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
