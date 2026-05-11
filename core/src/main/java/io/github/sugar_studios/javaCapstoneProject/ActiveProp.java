package io.github.sugar_studios.javaCapstoneProject;

/** A single prop currently scrolling through the gameplay area. */
public class ActiveProp {

    public final PropType type;
    public final float displayWidth;
    public final float displayHeight;
    public final float x;
    public float y;
    public final boolean flipX;
    public final float scale;
    public final float tint;

    public ActiveProp(PropType type, float x, float y, boolean flipX, float scale, float tint, float displayWidth, float displayHeight) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.flipX = flipX;
        this.scale = scale;
        this.tint = tint;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
    }
}
