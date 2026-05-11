package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Main extends Game {

    private ShapeRenderer shapeRenderer;
    private Batch batch;
    private BitmapFont fontEN;
    // private BitmapFont fontJP;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        FreeTypeFontGenerator generatorEN = new FreeTypeFontGenerator(
            Gdx.files.internal("fonts/Mona12.ttf"));
        // FreeTypeFontGenerator generatorJP = new FreeTypeFontGenerator(
        //         Gdx.files.internal("fonts/Mona12TextJP.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size  = 24;
        parameter.color = Color.WHITE;

        fontEN = generatorEN.generateFont(parameter);
        // fontJP = generatorJP.generateFont(parameter);

        generatorEN.dispose();
        // generatorJP.dispose();

        //setScreen(new GameScreen(this));
        //setScreen(new CutsceneScreen(this, 0));
        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        shapeRenderer.dispose();
        fontEN.dispose();
        // fontJP.dispose();
    }

    // ── Shared resource accessors
    public Batch getBatch() { return batch; }
    public ShapeRenderer getShapeRenderer() { return shapeRenderer; }
    public BitmapFont getFontEN() { return fontEN; }
    // public BitmapFont getFontJP()        { return fontJP; }
}

/*
 * REFLECTION
 *
 * 1) What was your project about, and what features did you implement?
 * My project is a bullet Hell demo. I implmented a probablity queue, a input  hasmap, enemies that follow a script
 * of movement and shooting. A wave system.
 * 2) Which concepts from CSC205 did you utilize in your project?
 * A lot of hashmaps, arrays, and queues. I also used this to make my own "custom" data structture, that being the Probablity Queue. These are nested inside each other to make the core of the wave system
 * 3) What challenges did you encounter, and how did you overcome them?
 *  I had a really hard time figuring out how to get eneimes to work, bullets were easy but thinking of how to make enmies work and fly around.
 *  animations were also really hard (not the art), getting the code to work involved a lot of fitanguiling.
 *  I werid struggle was that on 5/3 I reinstalled windows on my PC, which normally would be fine but I realized I had only ported 1 enemy asset into
 *  my game... which was a challenge because I lost hours of sprite working... I overcame this by pretendeding that didnt happen. I also struggled with audio for hours, couldnt get it to work.
 * I still cant get JP text to work >:(
 * 4) What are you most proud of in your project?
 *   None of it really, it was rushed and it shows. If I hadn't had waited until the week it was assigned I probably would've finished my vision.
 *   I had already planned out this beforehand but didn't start working on it till the last second. But if anything I really am proud of the cutscene art and the wave system
 *    I think it was an elogant solution.
 * 5) If you had more time, what would you add or improve?
 *  Everything.
 */
