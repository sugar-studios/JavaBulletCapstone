package io.github.sugar_studios.javaCapstoneProject;

/** Every kind of prop that can scroll through the gameplay area. */
public enum PropType {
    PROP_1, PROP_2, PROP_3, PROP_4, PROP_5,
    FOG,
    FOREGROUND;

    /** Spawns on a side edge and renders at reduced opacity over the background. */
    public boolean isFog() {
        return this == FOG;
    }

    /** Renders tiled horizontally across the full gameplay width. */
    public boolean isTiled() {
        return this == FOREGROUND;
    }

    /** True for any prop rendered in the foreground pass rather than the background pass. */
    public boolean isForeground() {
        return this == FOG || this == FOREGROUND; }

}
