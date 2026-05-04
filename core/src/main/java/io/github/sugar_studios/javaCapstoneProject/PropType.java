package io.github.sugar_studios.javaCapstoneProject;

/**
 * Every type of prop that can scroll through the gameplay area.
 */
public enum PropType {

    //assetFile dispW   dispH   isFog  isOfuda
    TORII    ("background/prop_toro.png", 140f, 200f, false, false),
    ROCK_1   ("background/prop_rock1.png", 120f, 80f, false, false),
    ROCK_2   ("background/prop_rock2.png", 100f, 70f, false, false),
    MOSS_1   ("background/prop_moss1.png", 90f, 40f, false, false),
    MOSS_2   ("background/prop_moss2.png", 110f, 50f, false, false),
    FERN_1   ("background/prop_fern1.png", 80f, 90f, false, false),
    FERN_2   ("background/prop_fern2.png", 100f, 100f, false, false),
    MUSHROOM ("background/prop_mushroom.png", 60f, 70f, false, false),
    FOG      ("background/prop_fog.png", 320f, 180f, true, false),
    OFUDA    ("background/prop_ofuda.png", 256f, 64f, false, true );

    // ── Fields ────────────────────────────────────────────────────────────────

    public final String assetFile;
    public final float displayWidth;
    public final float displayHeight;

    /**
     * Fog is rendered in the foreground pass at reduced opacity and always
     * spawns on a side edge, flipped to face inward when on the right.
     */
    public final boolean isFog;

    /**
     * The ofuda rope is rendered in the foreground pass and tiled horizontally
     * to span the full gameplay width.
     */
    public final boolean isOfuda;


    PropType(String assetFile, float displayWidth, float displayHeight, boolean isFog, boolean isOfuda) {
        this.assetFile = assetFile;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.isFog = isFog;
        this.isOfuda = isOfuda;
    }

    public boolean isForeground() {
        return isFog || isOfuda;
    }
}
