package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.graphics.Texture;

/**
 * Holds animation frames and display data for an enemy.
 */
public record EnemyAnimData(Texture[] frames, float frameDur, float displayW, float displayH) {

    public void dispose() {
        for (Texture t : frames) if (t != null) t.dispose();
    }
}
