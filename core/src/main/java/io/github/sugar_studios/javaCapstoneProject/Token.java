package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.graphics.Texture;

import static io.github.sugar_studios.javaCapstoneProject.SfxPlayer.playCollect;

// Collectible world object. Falls under gravity, collected on Player contact.
// Hitbox is TOKEN_HITBOX_SIZE (2x TOKEN_SIZE). Sprite draws at TOKEN_SIZE centred on hitbox.
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
    public static final float TOKEN_HITBOX_SIZE = TOKEN_SIZE * 2f;

    private static final int[] BLUE_SCORE_BY_TIER = { 100, 500, 1500 };
    private static final float[] RED_PROGRESS_BY_TIER = { 50f, 125f, 200f };
    private static final float[] BLACK_PROGRESS_BY_TIER = { 25f, 40f, 70f };

    private static final float GRAVITY = -420f;
    private static final float TERMINAL_VELOCITY = -320f;

    private final TokenType type;
    private final int tier;
    private float velocityY = 0f;
    private boolean active = true;

    public Token(float x, float y, Texture texture, TokenType type, int tier) {
        super(x, y, TOKEN_HITBOX_SIZE, TOKEN_HITBOX_SIZE, texture);
        this.type = type;
        this.tier = tier;
    }

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

    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.Batch batch) {
        if (texture == null) return;
        float offset = (TOKEN_HITBOX_SIZE - TOKEN_SIZE) * 0.5f;
        batch.draw(texture, rect.x + offset, rect.y + offset, TOKEN_SIZE, TOKEN_SIZE);
    }

    public void collect(Player player) {
        if (!active) return;
        switch (type) {
            case BLUE:
                playCollect();
                GameScreen.addScore(BLUE_SCORE_BY_TIER[tier - 1]);
                break;
            case RED:
                playCollect();
                player.addRedProgress(RED_PROGRESS_BY_TIER[tier - 1]);
                break;
            case BLACK:
                playCollect();
                player.addBlackProgress(BLACK_PROGRESS_BY_TIER[tier - 1]);
                break;
            case PURPLE:
                playCollect();
                player.heal(1f);
                GameScreen.addScore(2000);
                break;
            default:
                break;
        }
        active = false;
    }

    public void deactivate() { active = false; }

    public TokenType getType() { return type; }
    public int getTier() { return tier; }
    public boolean isActive() { return active; }

    private void applyGravity(float deltaTime) {
        velocityY = Math.max(velocityY + GRAVITY * deltaTime, TERMINAL_VELOCITY);
        rect.y += velocityY * deltaTime;
    }
}
