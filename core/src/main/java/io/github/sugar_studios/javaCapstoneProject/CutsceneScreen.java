package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class CutsceneScreen extends ScreenAdapter {

    private static final float FADE_SEC = 0.5f;
    private static final int STILL = 0, TIMED = 1, ANIM = 2, BLACK = 3;
    private static final float WORLD_WIDTH = 1920f;
    private static final float WORLD_HEIGHT = 1080f;

    private FitViewport viewport;
    private Music music;

    private static class Step {
        int type;
        Texture[] frames;
        float param;
        boolean input;
        boolean fadeIn = true;
    }

    private Main game;
    private Array<Step> steps = new Array<>();
    private int idx;
    private Step cur;
    private int phase;
    private float timer;
    private int frameIdx;
    private float animTimer;
    private float hintTimer = 0f;
    private boolean isFirstSlide = false;

    private com.badlogic.gdx.graphics.g2d.GlyphLayout hintLayout;

    public CutsceneScreen(Main game, int id) {
        this.game = game;
        this.buildId = id;
    }

    private int buildId;

    @Override
    public void show() {
        buildCutscene(buildId);
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        music = Gdx.audio.newMusic(Gdx.files.internal("audio/music/【 AJDispirito】FOREVER.mp3"));
        music.setLooping(true);
        music.setVolume(0.5f);
        music.play();
        SfxPlayer.load();
        hintLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.getFontEN(), "PRESS Z TO CONTINUE");
        idx = 0;
        nextStep();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    private void nextStep() {
        if (idx >= steps.size) {
            if (music != null) music.stop();
            game.setScreen(new GameScreen(game));
            return;
        }
        isFirstSlide = (idx == 0);
        cur = steps.get(idx++);
        phase = cur.fadeIn ? 0 : 1;
        timer = 0;
        frameIdx = 0;
        animTimer = 0;
        hintTimer = 0;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        timer += delta;

        if (cur.type == BLACK) {
            if (timer >= FADE_SEC) nextStep();
            return;
        }

        float overlay = 0;
        boolean advance = false;
        boolean zPressed = false;

        if (phase == 0) {
            overlay = 1f - Math.min(timer / FADE_SEC, 1f);
            if (timer >= FADE_SEC) { phase = 1; timer = 0; }
        } else {
            switch (cur.type) {
                case STILL:
                    if (cur.input && Keybinds.isJustPressed(Keybinds.Action.SHOOT)) {
                        zPressed = true;
                        advance = true;
                    }
                    break;
                case TIMED:
                    if (timer >= cur.param) advance = true;
                    break;
                case ANIM:
                    animTimer += delta * 1000f;
                    if (animTimer >= cur.param) {
                        animTimer -= cur.param;
                        if (frameIdx < cur.frames.length - 1) {
                            frameIdx++;
                        } else if (!cur.input) {
                            advance = true;
                        }
                    }
                    if (cur.input && Keybinds.isJustPressed(Keybinds.Action.SHOOT)) {
                        zPressed = true;
                        advance = true;
                    }
                    break;
            }
        }

        drawFrame(cur.frames[frameIdx]);
        if (overlay > 0) drawOverlay(overlay);

        if (isFirstSlide && phase == 1 && cur.type == STILL) {
            hintTimer += delta;
            if (hintTimer >= 2f) drawHint();
        }

        if (advance) {
            if (zPressed) SfxPlayer.playCutscene();
            nextStep();
        }
    }

    private void drawHint() {
        viewport.apply();
        com.badlogic.gdx.graphics.g2d.BitmapFont font = game.getFontEN();
        float x = WORLD_WIDTH - 48f - hintLayout.width;
        float y = WORLD_HEIGHT - 48f;
        game.getBatch().begin();
        font.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        font.draw(game.getBatch(), hintLayout, x, y);
        game.getBatch().end();
    }

    private void drawFrame(Texture tex) {
        viewport.apply();
        game.getBatch().setProjectionMatrix(viewport.getCamera().combined);
        float scale = Math.min(WORLD_WIDTH / tex.getWidth(), WORLD_HEIGHT / tex.getHeight());
        float rw = tex.getWidth() * scale;
        float rh = tex.getHeight() * scale;
        float x = (WORLD_WIDTH - rw) / 2f;
        float y = (WORLD_HEIGHT - rh) / 2f;
        game.getBatch().begin();
        game.getBatch().draw(tex, x, y, rw, rh);
        game.getBatch().end();
    }

    private void drawOverlay(float alpha) {
        viewport.apply();
        ShapeRenderer sr = game.getShapeRenderer();
        sr.setProjectionMatrix(viewport.getCamera().combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0, 0, 0, alpha);
        sr.rect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        sr.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void still(String path) {
        Step s = new Step();
        s.type = STILL;
        s.input = true;
        s.frames = new Texture[]{ new Texture(Gdx.files.internal(path)) };
        steps.add(s);
    }

    private void timed(String path, float sec) {
        Step s = new Step();
        s.type = TIMED;
        s.param = sec;
        s.input = false;
        s.frames = new Texture[]{ new Texture(Gdx.files.internal(path)) };
        steps.add(s);
    }

    private void anim(float ms, boolean input, String[] paths) {
        Step s = new Step();
        s.type = ANIM;
        s.param = ms;
        s.input = input;
        s.frames = new Texture[paths.length];
        for (int i = 0; i < paths.length; i++)
            s.frames[i] = new Texture(Gdx.files.internal(paths[i]));
        steps.add(s);
    }

    private void black() {
        Step s = new Step();
        s.type = BLACK;
        s.frames = new Texture[0];
        steps.add(s);
    }

    private void noFade() {
        steps.peek().fadeIn = false;
    }

    private String[] range(String prefix, int start, int end) {
        String[] p = new String[end - start + 1];
        for (int i = 0; i < p.length; i++) p[i] = prefix + (start + i) + ".png";
        return p;
    }

    private void buildCutscene(int id) {
        switch (id) {
            case 0: buildIntro(); break;
        }
    }

    private void buildIntro() {
        still("cutscenes/IntroCutscene/scene1.png");
        noFade();
        anim(66, true, range("cutscenes/IntroCutscene/scene2-", 1, 12));
        black();
        still("cutscenes/IntroCutscene/scene3.png");
        black();
        still("cutscenes/IntroCutscene/scene4.png");
        black();
        still("cutscenes/IntroCutscene/scene5.png");
        black();
        still("cutscenes/IntroCutscene/scene6.png");
        black();
        still("cutscenes/IntroCutscene/scene7.png");
        black();
        timed("cutscenes/IntroCutscene/scene8.png", 1f);
        noFade();
        anim(66, false, range("cutscenes/IntroCutscene/scene9-", 1, 11));
        noFade();
        anim(33, false, range("cutscenes/IntroCutscene/scene10-", 1, 28));
        noFade();
        timed("cutscenes/IntroCutscene/scene10-28.png", .75f);
        noFade();
        anim(33, true, range("cutscenes/IntroCutscene/scene10-", 28, 39));
        noFade();
        black();
        still("cutscenes/IntroCutscene/scene11.png");
        black();
        still("cutscenes/IntroCutscene/scene12.png");
        black();
    }

    @Override
    public void dispose() {
        for (Step s : steps) {
            if (s.frames != null) {
                for (Texture t : s.frames) t.dispose();
            }
        }
        if (music != null) music.dispose();
        SfxPlayer.dispose();
    }
}
