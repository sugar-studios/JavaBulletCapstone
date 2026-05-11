package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MainMenuScreen extends ScreenAdapter {

    private static final float WORLD_WIDTH = 1920f;
    private static final float WORLD_HEIGHT = 1080f;
    private static final float ANIM_MS = 200f;
    private static final float SEL_SCALE = 1.06f;
    private static final float UNSEL_ALPHA = 0.60f;
    private static final float BUTTON_SCALE = 5f;
    private static final float LINE_HEIGHT = 48f;

    private enum State { MAIN, CONTROLS }

    private final Main game;
    private FitViewport viewport;

    private Texture[] bgMain;
    private Texture[] bgControls;
    private Texture texStart, texControls, texBack;

    private float animTimer;
    private int bgFrame;

    private State state = State.MAIN;
    private int selIdx = 0;
    private int prevSelIdx = -1;

    private Rectangle[] mainRects;
    private Rectangle[] controlsRects;

    private final Vector2 mouseWorld = new Vector2();

    private static final String[][] CONTROL_ROWS = {
        {"MOVE", "ARROW KEYS"},
        {"SHOOT", "Z"},
        {"SWORD SLASH", "X"},
        {"BOMB", "C"},
        {"DASH", "SPACE"},
        {"FOCUS", "LEFT SHIFT"}
    };

    public MainMenuScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);

        bgMain = new Texture[7];
        for (int i = 0; i < 7; i++)
            bgMain[i] = new Texture(Gdx.files.internal("ui/mainMenu/mainMenu/menu" + (i + 1) + ".png"));

        bgControls = new Texture[3];
        for (int i = 0; i < 3; i++)
            bgControls[i] = new Texture(Gdx.files.internal("ui/mainMenu/controlsMenu/controlsMenu" + (i + 1) + ".png"));

        texStart = new Texture(Gdx.files.internal("ui/mainMenu/start.png"));
        texControls = new Texture(Gdx.files.internal("ui/mainMenu/controls.png"));
        texBack = new Texture(Gdx.files.internal("ui/mainMenu/back.png"));

        SfxPlayer.load();
        buildButtonRects();

        selIdx = 0;
        prevSelIdx = -1;
        animTimer = 0f;
        bgFrame = 0;
    }

    private void buildButtonRects() {
        float startW = texStart.getWidth() * BUTTON_SCALE;
        float startH = texStart.getHeight() * BUTTON_SCALE;
        float ctrlW = texControls.getWidth() * BUTTON_SCALE;
        float ctrlH = texControls.getHeight() * BUTTON_SCALE;

        mainRects = new Rectangle[]{
            new Rectangle((WORLD_WIDTH - startW) * .8f, WORLD_HEIGHT * 0.55f, startW, startH),
            new Rectangle((WORLD_WIDTH - ctrlW) * .8f, WORLD_HEIGHT * 0.20f, ctrlW, ctrlH)
        };

        float backW = texBack.getWidth() * BUTTON_SCALE;
        float backH = texBack.getHeight() * BUTTON_SCALE;

        controlsRects = new Rectangle[]{
            new Rectangle((WORLD_WIDTH - backW) / 2f, WORLD_HEIGHT * 0.10f, backW, backH)
        };
    }

    @Override
    public void resize(int w, int h) {
        viewport.update(w, h, true);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        animTimer += delta * 1000f;
        if (animTimer >= ANIM_MS) {
            animTimer -= ANIM_MS;
            Texture[] bg = (state == State.MAIN) ? bgMain : bgControls;
            bgFrame = (bgFrame + 1) % bg.length;
        }

        handleInput();

        viewport.apply();
        Batch batch = game.getBatch();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        Texture[] bg = (state == State.MAIN) ? bgMain : bgControls;
        batch.setColor(Color.WHITE);
        batch.draw(bg[bgFrame], 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        if (state == State.MAIN) {
            drawButton(batch, texStart, mainRects[0], selIdx == 0);
            drawButton(batch, texControls, mainRects[1], selIdx == 1);
        } else {
            drawControlsText(batch);
            drawButton(batch, texBack, controlsRects[0], true);
        }

        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void drawControlsText(Batch batch) {
        BitmapFont font = game.getFontEN();
        GlyphLayout layout = new GlyphLayout();

        float totalHeight = CONTROL_ROWS.length * LINE_HEIGHT;
        float startY = (WORLD_HEIGHT / 2f) + (totalHeight / 2f);
        float colGap = 300f;

        for (int i = 0; i < CONTROL_ROWS.length; i++) {
            String action = CONTROL_ROWS[i][0];
            String key = CONTROL_ROWS[i][1];

            layout.setText(font, action + "     " + key);
            float x = (WORLD_WIDTH - layout.width) / 2f;
            float y = startY - (i * LINE_HEIGHT);

            font.setColor(Color.WHITE);
            font.draw(batch, action, x, y);

            layout.setText(font, action);
            font.setColor(Color.YELLOW);
            font.draw(batch, key, x + layout.width + colGap, y);
        }

        font.setColor(Color.WHITE);
    }

    private void drawButton(Batch batch, Texture tex, Rectangle r, boolean selected) {
        if (selected) {
            batch.setColor(1f, 1f, 1f, 1f);
            float extra = (SEL_SCALE - 1f) / 2f;
            batch.draw(tex,
                r.x - r.width * extra,
                r.y - r.height * extra,
                r.width * SEL_SCALE,
                r.height * SEL_SCALE);
        } else {
            batch.setColor(1f, 1f, 1f, UNSEL_ALPHA);
            batch.draw(tex, r.x, r.y, r.width, r.height);
        }
    }

    private void handleInput() {
        Rectangle[] rects = (state == State.MAIN) ? mainRects : controlsRects;
        int maxIdx = rects.length - 1;

        if (Keybinds.isJustPressed(Keybinds.Action.UP))
            selIdx = Math.max(0, selIdx - 1);
        if (Keybinds.isJustPressed(Keybinds.Action.DOWN))
            selIdx = Math.min(maxIdx, selIdx + 1);

        updateMouseWorld();
        for (int i = 0; i <= maxIdx; i++) {
            if (rects[i].contains(mouseWorld)) {
                selIdx = i;
                break;
            }
        }

        if (selIdx != prevSelIdx) {
            SfxPlayer.playCutscene();
            prevSelIdx = selIdx;
        }

        boolean zPressed = Keybinds.isJustPressed(Keybinds.Action.SHOOT);
        boolean mouseClicked = Gdx.input.justTouched() && rects[selIdx].contains(mouseWorld);

        if (zPressed || mouseClicked) {
            SfxPlayer.playCutscene();
            confirm();
        }
    }

    private void updateMouseWorld() {
        viewport.unproject(mouseWorld.set(Gdx.input.getX(), Gdx.input.getY()));
    }

    private void confirm() {
        if (state == State.MAIN) {
            if (selIdx == 0) {
                game.setScreen(new CutsceneScreen(game, 0));
            } else {
                state = State.CONTROLS;
                selIdx = 0;
                prevSelIdx = -1;
                bgFrame = 0;
                animTimer = 0f;
            }
        } else {
            state = State.MAIN;
            selIdx = 0;
            prevSelIdx = -1;
            bgFrame = 0;
            animTimer = 0f;
        }
    }

    @Override
    public void dispose() {
        for (Texture t : bgMain) t.dispose();
        for (Texture t : bgControls) t.dispose();
        texStart.dispose();
        texControls.dispose();
        texBack.dispose();
        SfxPlayer.dispose();
    }
}
