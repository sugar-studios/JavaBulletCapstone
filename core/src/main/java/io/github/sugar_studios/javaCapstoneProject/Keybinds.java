package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import java.util.HashMap;

/**
 * Maps game actions to input keys and provides query helpers.
 */
public class Keybinds {

    public enum Action {
        FOCUS, UP, DOWN, LEFT, RIGHT, DASH, SWORD_SLASH, SHOOT, BOMB
    }

    private static final HashMap<Action, Integer> bindings = new HashMap<>();

    static {
        bindings.put(Action.FOCUS,Input.Keys.SHIFT_LEFT);
        bindings.put(Action.UP, Input.Keys.UP);
        bindings.put(Action.DOWN, Input.Keys.DOWN);
        bindings.put(Action.LEFT, Input.Keys.LEFT);
        bindings.put(Action.RIGHT, Input.Keys.RIGHT);
        bindings.put(Action.DASH, Input.Keys.SPACE);
        bindings.put(Action.SWORD_SLASH, Input.Keys.X);
        bindings.put(Action.SHOOT, Input.Keys.Z);
        bindings.put(Action.BOMB, Input.Keys.C);
    }

    public static boolean isHeld(Action action) {
        return Gdx.input.isKeyPressed(bindings.get(action));
    }

    public static boolean isJustPressed(Action action) {
        return Gdx.input.isKeyJustPressed(bindings.get(action));
    }

    public static void rebind(Action action, int newKey) {
        bindings.put(action, newKey);
    }
}
