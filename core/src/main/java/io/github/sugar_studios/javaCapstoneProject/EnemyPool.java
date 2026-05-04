package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.graphics.g2d.Batch;
import java.util.ArrayList;

public final class EnemyPool {

    private static final ArrayList<Enemy> active = new ArrayList<>();

    private EnemyPool() {}

    public static void add(Enemy enemy) {
        if (enemy != null) active.add(enemy);
    }

    public static void update(float delta, Player player) {
        for (int i = active.size() - 1; i >= 0; i--) {
            Enemy e = active.get(i);
            e.update(delta, player);
            if (e.isMarkedForRemoval()) active.remove(i);
        }
    }

    public static void draw(Batch batch) {
        for (Enemy e : active) e.draw(batch);
    }

    public static ArrayList<Enemy> getActive() { return active; }

    public static void clear() { active.clear(); }
}
