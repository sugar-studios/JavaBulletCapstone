package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.graphics.Color;

/** Per-level configuration: asset folder, sky fill colour, and background music track. */
public final class BackgroundConfig {

    public final int folder;
    public final Color color;
    public final String musicPath;

    public BackgroundConfig(int folder, Color color, String musicPath) {
        this.folder = folder;
        this.color = color;
        this.musicPath = musicPath;
    }

    /** Builds a path to a named asset inside this level's background folder. */
    public String path(String asset) {
        return "backgrounds/" + folder + "/" + asset;
    }
}
