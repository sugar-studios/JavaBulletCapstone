package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A weighted probability queue with recency suppression.
 * Position 0 is the most probable and the last position the least probable.
 * The list is shuffled at construction so the first draw is uniformly random.
 */
public class ProbabilityQueue<T> {
    private static final float WEIGHT_DECAY_RATIO = 0.70f;

    private final List<T> items;
    private final float[] weights;


    public ProbabilityQueue(List<T> initialItems) {
        this.items   = new ArrayList<>(initialItems);
        Collections.shuffle(this.items);
        this.weights = buildWeights(this.items.size());
    }


    /**
     * Selects one item via position-weighted sampling, then moves it to the
     * last slot so it is least likely next time.
     *
     * @return the selected item
     */
    public T selectNext() {
        if (items.isEmpty()) throw new IllegalStateException("ProbabilityQueue is empty.");

        float roll = MathUtils.random();
        float cumulative = 0f;
        int chosen = items.size() - 1; // fallback: last item

        for (int i = 0; i < items.size(); i++) {
            cumulative += weights[i];
            if (roll <= cumulative) { chosen = i; break; }
        }

        T item = items.remove(chosen); // shifts everything after forward by 1
        items.add(item); // append to the least-probable slot
        return item;
    }

    public int size() { return items.size(); }

    private static float[] buildWeights(int size) {
        float[] w = new float[size];
        float sum = 0f;
        for (int i = 0; i < size; i++) {
            w[i] = (float) Math.pow(WEIGHT_DECAY_RATIO, i); sum += w[i];
        }
        for (int i = 0; i < size; i++) {
            w[i] /= sum;
        }
        return w;
    }
}
