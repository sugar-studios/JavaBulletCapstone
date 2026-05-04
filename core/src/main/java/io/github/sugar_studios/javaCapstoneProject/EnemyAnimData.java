package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.graphics.Texture;

/**
 * Holds animation frames and display data for an enemy.
 */
public record EnemyAnimData(Texture[] idleFrames, float idleFrameDur, Texture[] attackFrames, float attackFrameDur, float displayW, float displayH) {
    public boolean hasAttackAnim() {
        return attackFrames != null && attackFrames.length > 0;
    }

    public void dispose() {
        for (Texture t : idleFrames) if (t != null) t.dispose();
        if (attackFrames != null)
            for (Texture t : attackFrames) if (t != null) t.dispose();
    }
}
