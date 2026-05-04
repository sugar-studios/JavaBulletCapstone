package io.github.sugar_studios.javaCapstoneProject;

/**
 * A collection of enemy spawn entries executed together.
 */
public final class Wave {

    public final WaveEntry[] entries;

    public Wave(WaveEntry... entries) {
        if (entries == null || entries.length == 0)
            throw new IllegalArgumentException("A Wave must contain at least one WaveEntry.");
        this.entries = entries;
    }
}
