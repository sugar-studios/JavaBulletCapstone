package io.github.sugar_studios.javaCapstoneProject;

/**
 * One row in a wave script: a pre-built enemy paired with its timing metadata.
 *
 * spawnDelay  – seconds to wait after THIS enemy is spawned before the next
 *               entry's countdown begins.  For checkpoint entries this delay
 *               starts counting down AFTER the checkpoint enemy dies.
 *
 * isCheckpoint – when true the WaveManager will not advance past this entry
 *               until the enemy's isAlive() returns false.  The enemy itself
 *               must also be constructed with isCheckpoint=true so it stops
 *               at its final waypoint instead of despawning.
 */

public final class WaveEntry {

    public final Enemy enemy;
    public final float spawnDelay;
    public final boolean isCheckpoint;
    public final String animKey;
    public final String bulletPath;

    public WaveEntry(Enemy enemy, float spawnDelay, boolean isCheckpoint, String animKey, String bulletPath) {
        this.enemy = enemy;
        this.spawnDelay = Math.max(0f, spawnDelay);
        this.isCheckpoint = isCheckpoint;
        this.animKey = animKey;
        this.bulletPath = bulletPath;
    }
}
