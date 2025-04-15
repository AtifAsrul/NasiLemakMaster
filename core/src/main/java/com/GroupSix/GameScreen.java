package com.GroupSix;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameScreen implements Screen {
    // Reference to main game class
    private final MyGame game;

    // Textures
    private Texture backgroundTexture;
    private Texture plateTexture;
    private Texture[] ingredientTextures;

    // Sprites
    private Sprite plateSprite;
    private Array<Sprite> ingredientSprites;
    private Array<Sprite> plateContents;

    // Game state
    private Array<String> currentOrder;
    private Array<String> possibleIngredients;
    private int score = 0;

    // Input
    private Sprite draggedSprite;
    private Vector2 dragOffset;
    private boolean isDragging = false;

    // Rendering
    private SpriteBatch spriteBatch;
    private FitViewport viewport;
    private BitmapFont font;

    // UI
    private Rectangle plateRect;
    private Rectangle uiSafeArea;

    public GameScreen(final MyGame game) {
        this.game = game;
        initializeGame();
    }

    private void initializeGame() {
        // Initialize ingredients list
        possibleIngredients = new Array<String>();
        possibleIngredients.addAll("rice", "egg", "sambal", "chicken", "vegetables");

        // Load textures
        backgroundTexture = new Texture(Gdx.files.internal("background.png"));
        plateTexture = new Texture(Gdx.files.internal("plate.png"));

        ingredientTextures = new Texture[possibleIngredients.size];
        for (int i = 0; i < possibleIngredients.size; i++) {
            ingredientTextures[i] = new Texture(Gdx.files.internal(possibleIngredients.get(i) + ".png"));
        }

        // Initialize rendering
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(800, 500);
        font = new BitmapFont();
        font.getData().setScale(2);

        // Setup plate
        plateSprite = new Sprite(plateTexture);
        plateSprite.setPosition(300, 50);
        plateSprite.setSize(200, 150);
        plateRect = new Rectangle(plateSprite.getX(), plateSprite.getY(),
            plateSprite.getWidth(), plateSprite.getHeight());

        // Safe area for UI (top 100 pixels)
        uiSafeArea = new Rectangle(0, 400, 800, 100);

        // Setup ingredients (below UI safe area)
        ingredientSprites = new Array<Sprite>();
        plateContents = new Array<Sprite>();

        float x = 50;
        for (Texture tex : ingredientTextures) {
            Sprite ingredient = new Sprite(tex);
            ingredient.setSize(80, 80);
            ingredient.setPosition(x, 300);  // Moved below UI area
            ingredientSprites.add(ingredient);
            x += 150;
        }

        // Initial order
        generateNewOrder();
    }

    @Override
    public void render(float delta) {
        handleInput();
        update();
        draw();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            Vector2 touchPos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);

            // Ignore touches in UI area
            if (uiSafeArea.contains(touchPos)) return;

            // Check if touching an ingredient
            for (Sprite ingredient : ingredientSprites) {
                if (ingredient.getBoundingRectangle().contains(touchPos)) {
                    draggedSprite = new Sprite(ingredient);
                    dragOffset = new Vector2(
                        touchPos.x - ingredient.getX(),
                        touchPos.y - ingredient.getY()
                    );
                    isDragging = true;
                    break;
                }
            }
        }

        if (isDragging) {
            if (Gdx.input.isTouched()) {
                Vector2 touchPos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
                viewport.unproject(touchPos);

                draggedSprite.setPosition(
                    touchPos.x - dragOffset.x,
                    touchPos.y - dragOffset.y
                );
            } else {
                // Touch released
                Vector2 touchPos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
                viewport.unproject(touchPos);

                if (plateRect.contains(touchPos)) {
                    // Check for duplicates before adding
                    String ingredientName = getIngredientName(draggedSprite);
                    if (!hasDuplicateOnPlate(ingredientName)) {
                        Sprite added = new Sprite(draggedSprite);
                        added.setPosition(
                            plateSprite.getX() + MathUtils.random(20, plateSprite.getWidth() - added.getWidth() - 20),
                            plateSprite.getY() + MathUtils.random(20, plateSprite.getHeight() - added.getHeight() - 20)
                        );
                        plateContents.add(added);
                    }
                }

                isDragging = false;
                draggedSprite = null;
            }
        }

        // Serve when space is pressed
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            checkOrder();
        }

        // Return to menu when ESC is pressed
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
        }
    }

    private void update() {
        // Keep ingredients in bounds
        for (Sprite ingredient : ingredientSprites) {
            ingredient.setX(MathUtils.clamp(ingredient.getX(), 0, 800 - ingredient.getWidth()));
            ingredient.setY(MathUtils.clamp(ingredient.getY(), 0, 300 - ingredient.getHeight()));
        }
    }

    private void draw() {
        Gdx.gl.glClearColor(0.9f, 0.9f, 0.8f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin();

        // Draw background
        spriteBatch.draw(backgroundTexture, 0, 0, 800, 500);

        // Draw plate
        plateSprite.draw(spriteBatch);

        // Draw ingredients on plate
        for (Sprite content : plateContents) {
            content.draw(spriteBatch);
        }

        // Draw ingredient options
        for (Sprite ingredient : ingredientSprites) {
            ingredient.draw(spriteBatch);
        }

        // Draw dragged ingredient
        if (isDragging && draggedSprite != null) {
            draggedSprite.draw(spriteBatch);
        }

        // Draw UI (in safe area)
        font.draw(spriteBatch, "Order: " + getOrderText(), 20, 480);
        font.draw(spriteBatch, "Score: " + score, 20, 440);
        font.draw(spriteBatch, "Press SPACE to serve", 500, 50);
        font.draw(spriteBatch, "ESC to Menu", 500, 80);

        spriteBatch.end();
    }

    private String getIngredientName(Sprite sprite) {
        for (int i = 0; i < ingredientTextures.length; i++) {
            if (sprite.getTexture() == ingredientTextures[i]) {
                return possibleIngredients.get(i);
            }
        }
        return "";
    }

    private boolean hasDuplicateOnPlate(String ingredientName) {
        for (Sprite item : plateContents) {
            if (getIngredientName(item).equals(ingredientName)) {
                return true;
            }
        }
        return false;
    }

    private String getOrderText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currentOrder.size; i++) {
            if (i > 0) sb.append(" + ");
            sb.append(currentOrder.get(i));
        }
        return sb.toString();
    }

    private void generateNewOrder() {
        currentOrder = new Array<String>();
        int count = MathUtils.random(2, 3);

        // Create order without duplicates
        Array<String> availableIngredients = new Array<String>(possibleIngredients);
        for (int i = 0; i < count; i++) {
            String ingredient = availableIngredients.random();
            currentOrder.add(ingredient);
            availableIngredients.removeValue(ingredient, false);
        }
    }

    private void checkOrder() {
        if (plateContents.size != currentOrder.size) {
            score = Math.max(0, score - 10);
            plateContents.clear();
            return;
        }

        Array<String> plateItems = new Array<String>();
        for (Sprite item : plateContents) {
            plateItems.add(getIngredientName(item));
        }

        boolean match = true;
        for (String orderItem : currentOrder) {
            if (!plateItems.contains(orderItem, false)) {
                match = false;
                break;
            }
            plateItems.removeValue(orderItem, false);
        }

        if (match) {
            score += 100;
            Gdx.app.log("Game", "Correct order! +100 points");
        } else {
            score = Math.max(0, score - 50);
            Gdx.app.log("Game", "Wrong order! -50 points");
        }

        plateContents.clear();
        generateNewOrder();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void show() {
        // Called when this screen becomes the current screen
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    game.setScreen(new MenuScreen(game));
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void hide() {
        // Called when current screen changes from this to a different screen
    }

    @Override
    public void pause() {
        // Called when game is paused
    }

    @Override
    public void resume() {
        // Called when game is resumed
    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        plateTexture.dispose();
        for (Texture tex : ingredientTextures) {
            tex.dispose();
        }
        spriteBatch.dispose();
        font.dispose();
    }
}
