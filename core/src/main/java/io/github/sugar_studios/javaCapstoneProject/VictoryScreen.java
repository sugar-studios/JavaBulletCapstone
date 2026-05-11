package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/*
 * TEMP
 */
public class VictoryScreen extends ScreenAdapter {
    private final Main game;
    private final Batch batch;
    private BitmapFont fontEN;
    private final Viewport viewport = new ScreenViewport();
    private final GlyphLayout layout = new GlyphLayout();
    private final int score;


    public VictoryScreen(Main game, int score) {
        this.game = game;
        this.batch = game.getBatch();
        this.fontEN = game.getFontEN();
        this.score = score;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        layout.setText(fontEN, "YOU WIN!!!\n\nPoints: " + score);

        //font.draw(batch, "'ood 'ay mate", 100f, 100f);

        fontEN.draw(batch, layout, viewport.getWorldWidth() / 2 - layout.width / 2, viewport.getWorldHeight() / 2 - layout.height  + 100);



        batch.end();
    }
}
