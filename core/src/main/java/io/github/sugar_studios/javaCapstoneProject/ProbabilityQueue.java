package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Weighted random queue. Earlier slots are more probable (geometric decay).
// Dynamic mode shuffles on init and moves each pick to the back (recency suppression).
// Static mode fixes weights forever with no reordering.
public class ProbabilityQueue<T> {

    private static final float DEFAULT_DECAY = 0.70f;

    private final List<T> items;
    private final float[] weights;
    private final boolean isStatic;

    // Original constructor — dynamic, default decay. Existing callers unaffected.
    public ProbabilityQueue(List<T> initialItems) {
        this(initialItems, DEFAULT_DECAY, false);
    }

    // Dynamic with custom decay ratio.
    public ProbabilityQueue(List<T> initialItems, float decayRatio) {
        this(initialItems, decayRatio, false);
    }

    // Full control. isStatic=true fixes order and skips shuffle.
    public ProbabilityQueue(List<T> initialItems, float decayRatio, boolean isStatic) {
        this.items = new ArrayList<>(initialItems);
        this.isStatic = isStatic;
        if (!isStatic) Collections.shuffle(this.items);
        this.weights = buildWeights(this.items.size(), decayRatio);
    }

    // Samples one item by weight. Dynamic: moves chosen item to back. Static: peek only.
    public T selectNext() {
        if (items.isEmpty()) throw new IllegalStateException("ProbabilityQueue is empty.");

        float roll = MathUtils.random();
        float cumulative = 0f;
        int chosen = items.size() - 1;

        for (int i = 0; i < items.size(); i++) {
            cumulative += weights[i];
            if (roll <= cumulative) { chosen = i; break; }
        }

        if (isStatic) return items.get(chosen);

        T item = items.remove(chosen);
        items.add(item);
        return item;
    }

    public int size() { return items.size(); }

    // Builds a normalised geometric weight array.
    private static float[] buildWeights(int size, float decayRatio) {
        float[] w = new float[size];
        float sum = 0f;
        for (int i = 0; i < size; i++) {
            w[i] = (float) Math.pow(decayRatio, i);
            sum += w[i];
        }
        for (int i = 0; i < size; i++) w[i] /= sum;
        return w;
    }
}
