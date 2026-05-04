package io.github.sugar_studios.javaCapstoneProject;

/**
 * Represents a single prop that is currently active in the gameplay area.
 *
 * Props scroll downward each frame and are removed once they exit the bottom
 * of the visible area.
 */
public class ActiveProp {
    public final PropType type;
    public final float x;
    public float y;
    public final boolean flipX;
    public final float scale;
    public final float tint;


    public ActiveProp(PropType type, float x, float y, boolean flipX, float scale, float tint) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.flipX = flipX;
        this.scale = scale;
        this.tint  = tint;
    }
}
