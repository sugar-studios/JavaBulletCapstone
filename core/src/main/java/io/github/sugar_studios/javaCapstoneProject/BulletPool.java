package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.graphics.g2d.Batch;
import java.util.Arrays;

/**
 * Static, fixed-capacity pool that is the single source of truth for every
 * active Bullet in the game.
 *
 * Rules:
 *   • If all slots are occupied, add() returns false and the bullet is silently dropped.
 *   • Bullets are evicted each frame when markedForRemoval or out-of-bounds.
 *   • Slots are set to null on eviction — no object pooling / recycling needed here
 *     because Bullet creation is cheap and the cap prevents runaway allocation.
 */
public final class BulletPool {

    public static final int CAPACITY = 256;

    private static final Bullet[] slots = new Bullet[CAPACITY];

    private BulletPool() {}   // non-instantiable

    /**
     * Attempts to register a bullet. Returns true on success, false if pool is full.
     * Call this instead of constructing bullets and storing them yourself.
     */
    public static boolean add(Bullet bullet) {
        for (int i = 0; i < CAPACITY; i++) {
            if (slots[i] == null) {
                slots[i] = bullet;
                return true;
            }
        }
        return false; // no room
    }

    /**
     * Updates every active bullet; evicts ones that are out-of-bounds or marked
     * for removal BEFORE moving them so one-frame ghost positions never linger.
     */
    public static void update(float delta, float worldWidth, float worldHeight) {
        for (int i = 0; i < CAPACITY; i++) {
            Bullet b = slots[i];
            if (b == null) continue;

            if (b.isMarkedForRemoval() || b.isOffScreen(worldWidth, worldHeight)) {
                slots[i] = null;
                continue;
            }

            b.update(delta);
        }
    }

    /** Draws every active bullet within the current batch session. */
    public static void draw(Batch batch) {
        for (Bullet b : slots) {
            if (b != null) b.draw(batch);
        }
    }

    /**
     * Exposes the raw slot array so GameScreen can iterate for collision checks
     * without an extra allocation. Treat as read-only outside this class.
     */
    public static Bullet[] getSlots() { return slots; }

    /** Removes all bullets instantly */
    public static void clear() { Arrays.fill(slots, null); }
}
