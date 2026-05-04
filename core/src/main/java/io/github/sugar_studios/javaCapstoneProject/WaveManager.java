package io.github.sugar_studios.javaCapstoneProject;

import java.util.HashMap;
import java.util.Queue;

/**
 * Drives wave progression for a single level run.
 *
 * State machine
 * ─────────────
 *   IDLE
 *    │  startLevel(n)
 *   SPAWNING  ── iterates WaveEntry[] one at a time:
 *    │              Phase A – count down spawnDelay, then add enemy to EnemyPool
 *    │              Phase B – if checkpoint, block until enemy.isAlive() == false
 *    │              Phase C – advance entryIndex; set next delay
 *    │           when entryIndex reaches entries.length:
 *   WAVE_COOLDOWN  ── waits WAVE_TIMER seconds
 *    │  polls next Wave from the queue
 *    │  if queue is empty:
 *   COMPLETE
 *
 * Spawn-delay semantics
 * ─────────────────────
 *   entry[N].spawnDelay is the gap AFTER entry[N] spawns before entry[N+1]
 *   begins its own countdown.  For checkpoint entries this delay starts
 *   counting down AFTER the checkpoint enemy dies.
 *
 *   Example: delay[0]=1.5, delay[1]=1.5, delay[2]=0
 *     t=0.0  → enemy 0 spawns
 *     t=1.5  → enemy 1 spawns
 *     t=3.0  → enemy 2 spawns  (immediately, delay=0)
 *     t=3.0  → wave cooldown begins
 */
public class WaveManager {

    private static final float WAVE_TIMER = 4f;

    private enum State { IDLE, SPAWNING, WAVE_COOLDOWN, COMPLETE }

    private final HashMap<Integer, Queue<Wave>> levelMap;
    private Queue<Wave> currentLevelQueue;
    private Wave currentWave;

    private int entryIndex;
    private float spawnDelayTimer;
    private boolean currentEntrySpawned;

    private float waveCooldownTimer;
    private State state = State.IDLE;

    public WaveManager(HashMap<Integer, Queue<Wave>> levelMap) {
        if (levelMap == null)
            throw new IllegalArgumentException("levelMap must not be null.");
        this.levelMap = levelMap;
    }

    public void startLevel(int level) {
        Queue<Wave> q = levelMap.get(level);
        if (q == null) {
            System.err.println("[WaveManager] Level " + level + " not found.");
            state = State.COMPLETE;
            return;
        }
        currentLevelQueue = q;
        advanceToNextWave();
    }

    public void update(float delta) {
        switch (state) {
            case SPAWNING:
                updateSpawning(delta);
                break;
            case WAVE_COOLDOWN:
                updateCooldown(delta);
                break;
            default:
                break;
        }
    }

    public boolean isComplete() { return state == State.COMPLETE; }

    private void advanceToNextWave() {
        currentWave = currentLevelQueue.poll();
        if (currentWave == null) {
            state = State.COMPLETE; return;
        }
        entryIndex = 0;
        spawnDelayTimer = 0f;
        currentEntrySpawned = false;
        state = State.SPAWNING;
    }

    private void updateSpawning(float delta) {
        if (entryIndex >= currentWave.entries.length) {
            state = State.WAVE_COOLDOWN;
            waveCooldownTimer = WAVE_TIMER;
            return;
        }

        WaveEntry entry = currentWave.entries[entryIndex];

        if (!currentEntrySpawned) {
            spawnDelayTimer -= delta;
            if (spawnDelayTimer > 0f) return;

            entry.enemy.initAnim(entry.animKey, entry.bulletPath);
            EnemyPool.add(entry.enemy);
            currentEntrySpawned = true;
            return;
        }

        if (entry.isCheckpoint && entry.enemy.isAlive()) return;

        spawnDelayTimer = entry.spawnDelay;
        entryIndex++;
        currentEntrySpawned = false;
    }

    private void updateCooldown(float delta) {
        waveCooldownTimer -= delta;
        if (waveCooldownTimer <= 0f) advanceToNextWave();
    }
}
