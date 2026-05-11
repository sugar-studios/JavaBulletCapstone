package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.graphics.Texture;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

// Spawns token clusters on enemy death via a two-stage type system plus a tier roll.
//
// Type resolution:
//   Stage 1: static ProbabilityQueue (BLUE ~39.5%, RED ~27.6%, BLACK ~19.3%, SPECIAL ~13.5%).
//   Stage 2: SPECIAL triggers a 1-in-15 roll → BLUE/RED/BLACK/PURPLE.
//
// Tier resolution (skipped for PURPLE, which is always tier 1):
//   Outer roll 1-in-3:
//     1       → tier 1
//     2       → 1/3 tier 2, otherwise tier 1
//     3       → inner 1-in-6 roll:
//                 1-2 → tier 1
//                 3   → 50/50 tier 1 or 2
//                 4-5 → tier 2
//                 6   → tier 3
public class TokenSpawner {

    private static final float DECAY_RATIO = 0.70f;
    private static final float CLUSTER_RADIUS = 80f;
    private static final int DEFAULT_COUNT = 5;

    private final ProbabilityQueue<TokenType> typeQueue;

    // Outer key: type. Inner key: tier (1-3). PURPLE only has key 1.
    private final EnumMap<TokenType, Texture[]> textures;

    // Build the texture map via buildTextures() rather than passing it in directly.
    public TokenSpawner(
        Texture blueT1, Texture blueT2, Texture blueT3,
        Texture redT1, Texture redT2, Texture redT3,
        Texture blackT1, Texture blackT2, Texture blackT3,
        Texture purple
    ) {
        textures = new EnumMap<>(TokenType.class);
        textures.put(TokenType.BLUE, new Texture[]{ blueT1, blueT2, blueT3 });
        textures.put(TokenType.RED, new Texture[]{ redT1, redT2, redT3 });
        textures.put(TokenType.BLACK, new Texture[]{ blackT1, blackT2, blackT3 });
        textures.put(TokenType.PURPLE, new Texture[]{ purple });

        List<TokenType> slots = Arrays.asList(
            TokenType.BLUE,
            TokenType.RED,
            TokenType.BLACK,
            TokenType.SPECIAL
        );
        this.typeQueue = new ProbabilityQueue<>(slots, DECAY_RATIO, true);
    }

    // Spawns count tokens scattered randomly within CLUSTER_RADIUS of (centerX, centerY).
    public List<Token> spawnTokens(float centerX, float centerY, int count) {
        if (count <= 0) throw new IllegalArgumentException("count must be > 0.");
        List<Token> spawned = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            TokenType queued = typeQueue.selectNext();
            TokenType resolved = queued == TokenType.SPECIAL ? resolveSpecial() : queued;
            int tier = resolved == TokenType.PURPLE ? 1 : resolveTier();
            Texture tex = textures.get(resolved)[tier - 1];
            float angle = MathUtils.random(MathUtils.PI2);
            float radius = MathUtils.random(CLUSTER_RADIUS);
            float tx = centerX + MathUtils.cos(angle) * radius - Token.TOKEN_SIZE * 0.5f;
            float ty = centerY + MathUtils.sin(angle) * radius - Token.TOKEN_SIZE * 0.5f;
            spawned.add(new Token(tx, ty, tex, resolved, tier));
        }
        return spawned;
    }

    public List<Token> spawnTokens(float centerX, float centerY) {
        return spawnTokens(centerX, centerY, DEFAULT_COUNT);
    }

    public void dispose() {
        for (Texture[] tiers : textures.values()) {
            for (Texture t : tiers) if (t != null) t.dispose();
        }
    }

    // 1-in-15 roll: 1-10 BLUE, 11-13 RED/BLACK 50-50, 14 RED, 15 PURPLE.
    private TokenType resolveSpecial() {
        int roll = MathUtils.random(1, 15);
        if (roll <= 10) return TokenType.BLUE;
        if (roll <= 13) return MathUtils.randomBoolean() ? TokenType.RED : TokenType.BLACK;
        if (roll == 14) return TokenType.RED;
        return TokenType.PURPLE;
    }

    // Outer 1-in-3, with an inner 1-in-6 on a roll of 3. Returns 1, 2, or 3.
    private int resolveTier() {
        int outer = MathUtils.random(1, 3);
        if (outer == 1) return 1;
        if (outer == 2) return MathUtils.random(1, 3) == 1 ? 2 : 1;
        int inner = MathUtils.random(1, 6);
        if (inner <= 2) return 1;
        if (inner == 3) return MathUtils.randomBoolean() ? 1 : 2;
        if (inner <= 5) return 2;
        return 3;
    }
}
