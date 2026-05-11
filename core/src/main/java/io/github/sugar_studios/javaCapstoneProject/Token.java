package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.graphics.Texture;

// Collectible world object. Falls under gravity, collected on Player contact.
// Hitbox and draw size are always TOKEN_SIZE x TOKEN_SIZE.
// PURPLE tokens are always tier 1. All others can be tier 1-3.
// Prefer update(float, Player) each frame; update(float) is gravity-only.
public class Token extends GameObject {

    public static final String TEXTURE_BLUE_T1 = "tokens/token_blue_1.png";
    public static final String TEXTURE_BLUE_T2 = "tokens/token_blue_2.png";
    public static final String TEXTURE_BLUE_T3 = "tokens/token_blue_3.png";
    public static final String TEXTURE_RED_T1 = "tokens/token_red_1.png";
    public static final String TEXTURE_RED_T2 = "tokens/token_red_2.png";
    public static final String TEXTURE_RED_T3 = "tokens/token_red_3.png";
    public static final String TEXTURE_BLACK_T1 = "tokens/token_black_1.png";
    public static final String TEXTURE_BLACK_T2 = "tokens/token_black_2.png";
    public static final String TEXTURE_BLACK_T3 = "tokens/token_black_3.png";
    public static final String TEXTURE_PURPLE = "tokens/token_purple_1.png";

    public static final float TOKEN_SIZE = 50f;

    // Points granted per blue token tier.
    private static final int[] BLUE_SCORE_BY_TIER = { 100, 500, 1500 };
    // Meter progress added per token tier for red and black tokens.
    private static final float[] RED_PROGRESS_BY_TIER  = { 50f, 125f, 200f };
    private static final float[] BLACK_PROGRESS_BY_TIER = { 25f, 40f, 70f };

    private static final float GRAVITY = -420f;
    private static final float TERMINAL_VELOCITY = -320f;

    private final TokenType type;
    private final int tier;
    private float velocityY = 0f;
    private boolean active = true;

    public Token(float x, float y, Texture texture, TokenType type, int tier) {
        super(x, y, TOKEN_SIZE, TOKEN_SIZE, texture);
        this.type = type;
        this.tier = tier;
    }

    // Physics + contact check. Returns true the frame this token is collected.
    public boolean update(float deltaTime, Player player) {
        if (!active) return false;
        if (overlaps(player)) {
            collect(player);
            return true;
        }
        applyGravity(deltaTime);
        return false;
    }

    @Override
    public void update(float deltaTime) {
        if (!active) return;
        applyGravity(deltaTime);
    }

    // Fires this token's effect on the player (scaled by tier) then destroys itself.
    public void collect(Player player) {
        if (!active) return;
        switch (type) {
            case BLUE:
                GameScreen.addScore(BLUE_SCORE_BY_TIER[tier - 1]);
                break;
            case RED:
                player.addRedProgress(RED_PROGRESS_BY_TIER[tier - 1]);
                break;
            case BLACK:
                player.addBlackProgress(BLACK_PROGRESS_BY_TIER[tier - 1]);
                break;
            case PURPLE:
                player.heal(1f);
                GameScreen.addScore(2000);
                break;
            default:
                break;
        }
        active = false;
    }

    // Destroys without firing any effect (e.g. fell off-screen).
    public void deactivate() { active = false; }

    public TokenType getType() { return type; }
    public int getTier() { return tier; }
    public boolean isActive() { return active; }

    private void applyGravity(float deltaTime) {
        velocityY = Math.max(velocityY + GRAVITY * deltaTime, TERMINAL_VELOCITY);
        rect.y += velocityY * deltaTime;
    }
}
