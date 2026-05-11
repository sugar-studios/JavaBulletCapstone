package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;

// Static SFX manager. Call load() in GameScreen.show(), dispose() in GameScreen.dispose().
// shoot and kill randomly alternate between their two variants each play.
public final class SfxPlayer {

    private static final String SFX = "audio/sfx/";

    private static Sound shoot1, shoot2;
    private static Sound kill1, kill2;
    private static Sound death, death2;
    private static Sound swordSwing, swordHit;
    private static Sound cutscene;

    private static boolean loaded = false;

    public static void load() {
        shoot1 = sound("shoot1.wav");
        shoot2 = sound("shoot2.wav");
        kill1 = sound("kill1.wav");
        kill2 = sound("kill2.wav");
        death = sound("death1.wav");
        death2 = sound("death2.wav");
        swordSwing = sound("swordSwing.wav");
        swordHit = sound("swordHit.wav");
        cutscene = sound("cutscene.wav");
        loaded = true;
    }

    public static void dispose() {
        if (!loaded) return;
        shoot1.dispose();
        shoot2.dispose();
        kill1.dispose();
        kill2.dispose();
        death.dispose();
        death2.dispose();
        swordSwing.dispose();
        swordHit.dispose();
        cutscene.dispose();
        loaded = false;
    }

    public static void playShoot() {
        if (!loaded) return;
        (MathUtils.randomBoolean() ? shoot1 : shoot2).play();
    }

    public static void playKill() {
        if (!loaded) return;
        (MathUtils.randomBoolean() ? kill1 : kill2).play();
    }

    public static void playDeath() {
        if (!loaded) return;
        (MathUtils.randomBoolean() ? death : death2).play();
    }

    public static void playSwordSwing() { if (loaded) swordSwing.play(); }
    public static void playSwordHit() { if (loaded) swordHit.play(); }
    public static void playCutscene() { if (loaded) cutscene.play(); }

    private static Sound sound(String filename) {
        return Gdx.audio.newSound(Gdx.files.internal(SFX + filename));
    }
}
