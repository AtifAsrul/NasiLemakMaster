package com.GroupSix;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.viewport.*;

public class MenuScreen implements Screen {
    private final MyGame game;
    private Stage stage;
    private Texture backgroundTexture;
    private SpriteBatch batch;
    private BitmapFont font;
    private Sound buttonClickSound;
    private Music menuMusic;
    private Texture logoTexture;
    private float logoWidth, logoHeight;

    public MenuScreen(final MyGame game) {
        this.game = game;
        loadAssets();
        setupStage();
        createMenu();
    }

    private void loadAssets() {
        // Load textures
        backgroundTexture = new Texture(Gdx.files.internal("menu_background.png"));
        logoTexture = new Texture(Gdx.files.internal("game_logo.png"));

        // Calculate dimensions to fit screen properly
        float maxDisplayWidth = 600; // Reduced from 800 to allow padding
        float maxDisplayHeight = 200; // Maximum height you want

        // Get original dimensions
        float originalWidth = logoTexture.getWidth();
        float originalHeight = logoTexture.getHeight();
        float aspectRatio = originalWidth / originalHeight;

        // Calculate scaled dimensions maintaining aspect ratio
        if (originalWidth > maxDisplayWidth) {
            logoWidth = maxDisplayWidth;
            logoHeight = maxDisplayWidth / aspectRatio;

            // If still too tall after width adjustment
            if (logoHeight > maxDisplayHeight) {
                logoHeight = maxDisplayHeight;
                logoWidth = maxDisplayHeight * aspectRatio;
            }
        } else if (originalHeight > maxDisplayHeight) {
            logoHeight = maxDisplayHeight;
            logoWidth = maxDisplayHeight * aspectRatio;
        } else {
            // Use original size if smaller than max dimensions
            logoWidth = originalWidth;
            logoHeight = originalHeight;
        }

        Gdx.app.log("MenuScreen", "Logo dimensions: " + logoWidth + "x" + logoHeight);

        // Load sounds
        buttonClickSound = Gdx.audio.newSound(Gdx.files.internal("button_click.wav"));
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("menu_music.mp3"));
        menuMusic.setLooping(true);
        menuMusic.setVolume(0.5f);
        menuMusic.play();

        // Load font
        try {
            font = new BitmapFont(Gdx.files.internal("custom_font.fnt"), false);
            font.getData().setScale(1.2f); // Slightly larger font
        } catch (Exception e) {
            font = new BitmapFont();
            Gdx.app.error("MenuScreen", "Using default font");
        }

        batch = new SpriteBatch();
    }

    private void setupStage() {
        stage = new Stage(new FitViewport(800, 500));
        Gdx.input.setInputProcessor(stage);
    }

    private void createMenu() {
        // Create button style with your custom images
        TextureRegionDrawable buttonUp = new TextureRegionDrawable(
            new TextureRegion(new Texture(Gdx.files.internal("button_normal.png")))
        );
        TextureRegionDrawable buttonDown = new TextureRegionDrawable(
            new TextureRegion(new Texture(Gdx.files.internal("button_pressed.png")))
        );

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle(
            buttonUp, buttonDown, null, font
        );
        buttonStyle.fontColor = Color.WHITE; // Ensure text is visible

        // Create buttons
        TextButton playButton = new TextButton("PLAY", buttonStyle);
        TextButton exitButton = new TextButton("EXIT", buttonStyle);

        // Set consistent button sizes
        float buttonWidth = 200f;
        float buttonHeight = 60f;

        playButton.setSize(buttonWidth, buttonHeight);
        exitButton.setSize(buttonWidth, buttonHeight);

        // Center buttons horizontally with better vertical spacing
        float buttonX = (800 - buttonWidth) / 2;
        playButton.setPosition(buttonX, 200);
        exitButton.setPosition(buttonX, 120);

        // Add button listeners with improved feedback
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                buttonClickSound.play(0.7f);
                menuMusic.stop();
                game.setScreen(new GameScreen(game));
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                buttonClickSound.play(0.7f);
                menuMusic.stop();
                Gdx.app.exit();
            }
        });

        stage.addActor(playButton);
        stage.addActor(exitButton);
    }

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Single batch draw for better performance
        batch.begin();
        // Draw background
        batch.draw(backgroundTexture, 0, 0, 800, 500);
        float logoX = (800 - logoWidth) / 2; // Center horizontally
        float logoY = 250; // Position from top with padding
        batch.draw(logoTexture, logoX, logoY, logoWidth, logoHeight);
        batch.end();

        // Draw UI
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
        logoTexture.dispose();
        batch.dispose();
        font.dispose();
        buttonClickSound.dispose();
        menuMusic.dispose();

        // Dispose button textures if needed
        // (You might want to cache these if used elsewhere)
    }

    @Override
    public void show() {
        // Resume music if coming back from pause
        if (!menuMusic.isPlaying()) {
            menuMusic.play();
        }
    }

    @Override
    public void pause() {
        menuMusic.pause();
    }

    @Override
    public void resume() {
        menuMusic.play();
    }

    @Override
    public void hide() {
        // Called when screen changes
    }
}
