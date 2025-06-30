package io.github.gone.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.gone.GoneFishingGame;
import io.github.gone.game.GameManager;
import io.github.gone.input.InputHandler;
import io.github.gone.progression.ProgressionManager;
import io.github.gone.ui.ExperienceBar;
import io.github.gone.ui.LevelUpPopup;
import io.github.gone.ui.FishGalleryScreen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.gone.utils.ShapeRendererManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class GameScreen implements Screen {
    private static final float WORLD_WIDTH = 480;
    private static final float WORLD_HEIGHT = 800;
    
    // Constants for log button
    private static final float LOG_BUTTON_SIZE = 40;
    private static final float LOG_BUTTON_X = WORLD_WIDTH - LOG_BUTTON_SIZE - 10;  // 10 pixels from right edge
    private static final float LOG_BUTTON_Y = WORLD_HEIGHT - LOG_BUTTON_SIZE - 10; // 10 pixels from top
    private static final Color LOG_BUTTON_COLOR = new Color(0.2f, 0.6f, 0.8f, 1); // Cyan-ish blue
    
    // Background colors
    private static final Color SUN_COLOR = new Color(1f, 0.9f, 0.5f, 1);
    private static final Color CLOUD_COLOR = new Color(0.95f, 0.95f, 0.95f, 1);
    private static final Color LAND_COLOR = new Color(0.5f, 0.35f, 0.2f, 1);
    private static final Color DOCK_COLOR = new Color(0.4f, 0.3f, 0.2f, 1);
    private static final Color WATER_COLOR = new Color(0.2f, 0.4f, 0.8f, 0.8f);
    private static final Color WAVES_COLOR = new Color(0.8f, 0.9f, 1f, 0.7f);
    private static final Color SKY_COLOR = new Color(0.4f, 0.7f, 0.9f, 1f); // Base sky color
    
    private final GoneFishingGame game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final GameManager gameManager;
    private final InputHandler inputHandler;
    
    // UI Elements
    private final ExperienceBar experienceBar;
    private final LevelUpPopup levelUpPopup;
    private final FishGalleryScreen fishGalleryScreen;
    private final ShapeRendererManager shapeRenderer;
    private final BitmapFont buttonFont;
    private final GlyphLayout glyphLayout;
    
    // Progression
    private final ProgressionManager progressionManager;
    
    public GameScreen(GoneFishingGame game) {
        this.game = game;
        this.batch = game.getBatch();
        
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        
        // Make sure we enable blending for transparent objects
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        // Initialize progression manager first (other components might depend on it)
        progressionManager = ProgressionManager.getInstance();
        
        // Create shape renderer - needed for many other components
        shapeRenderer = new ShapeRendererManager();
        
        // Initialize text rendering components
        buttonFont = new BitmapFont();
        buttonFont.setColor(Color.WHITE);
        glyphLayout = new GlyphLayout();
        
        // Create UI elements
        experienceBar = new ExperienceBar(20, WORLD_HEIGHT - 50, WORLD_WIDTH - 40);
        levelUpPopup = new LevelUpPopup();
        fishGalleryScreen = new FishGalleryScreen();
        
        // Initialize GameManager
        gameManager = new GameManager(WORLD_WIDTH / 2, WORLD_HEIGHT / 4);
        gameManager.init();
        
        // Initialize custom input handler
        inputHandler = new CustomInputHandler(gameManager, viewport, levelUpPopup, fishGalleryScreen);
        Gdx.input.setInputProcessor(inputHandler);
    }
    
    @Override
    public void show() {
        // Called when this screen becomes the current screen
    }
    
    @Override
    public void render(float delta) {
        // Clear the screen
        Gdx.gl.glClearColor(0.15f, 0.4f, 0.7f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Update game logic
        update(delta);
        
        // Render game elements
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        
        // Draw background
        drawBackground();
        
        batch.begin();
        
        // Draw fishing rod
        gameManager.draw(batch);
        
        // Draw UI elements
        experienceBar.draw(batch);
        
        batch.end();
        
        // Only show log button when not in throw minigame
        if (!gameManager.isMinigameActive() && !gameManager.isShowingFishCaught()) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            // Draw log button
            shapeRenderer.setColor(LOG_BUTTON_COLOR);
            shapeRenderer.getShapeRenderer().rect(LOG_BUTTON_X, LOG_BUTTON_Y, LOG_BUTTON_SIZE, LOG_BUTTON_SIZE);
            shapeRenderer.end();
            
            // Begin batch again to draw "Gallery" text on the button
            batch.begin();
            glyphLayout.setText(buttonFont, "Gallery");
            buttonFont.draw(batch, "Gallery", 
                LOG_BUTTON_X + (LOG_BUTTON_SIZE - glyphLayout.width) / 2,
                LOG_BUTTON_Y + (LOG_BUTTON_SIZE + glyphLayout.height) / 2);
            
            // Draw level up popup if active
            if (levelUpPopup.isActive()) {
                levelUpPopup.draw(batch);
            }
            
            // Draw player log screen if active
            if (fishGalleryScreen.isActive()) {
                fishGalleryScreen.draw();
            }
            
            batch.end();
        } else {
            // Begin batch again for popup UI elements
            batch.begin();
            
            // Draw level up popup if active
            if (levelUpPopup.isActive()) {
                levelUpPopup.draw(batch);
            }
            
            // Draw player log screen if active
            if (fishGalleryScreen.isActive()) {
                fishGalleryScreen.draw();
            }
            
            batch.end();
        }
    }
    
    /**
     * Draws the background with land, water, sky, and decorative elements
     */
    private void drawBackground() {
        // We'll use ShapeRenderer for the background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Sky gradient (lighter at top)
        for (int y = 0; y < WORLD_HEIGHT; y += 10) {
            float ratio = y / WORLD_HEIGHT;
            // Create a temporary color for the gradient
            Color gradientColor = new Color(
                SKY_COLOR.r + ratio * 0.1f, 
                SKY_COLOR.g + ratio * 0.1f, 
                SKY_COLOR.b, 
                1
            );
            shapeRenderer.setColor(gradientColor);
            shapeRenderer.getShapeRenderer().rect(0, y, WORLD_WIDTH, 10);
        }
        
        // Sun
        shapeRenderer.setColor(SUN_COLOR);
        shapeRenderer.getShapeRenderer().circle(WORLD_WIDTH - 80, WORLD_HEIGHT - 80, 40);
        
        // Clouds
        drawCloud(80, WORLD_HEIGHT - 100, 1.2f);
        drawCloud(200, WORLD_HEIGHT - 150, 0.8f);
        drawCloud(350, WORLD_HEIGHT - 80, 1.0f);
        
        // Land (brown dirt)
        shapeRenderer.setColor(LAND_COLOR);
        shapeRenderer.getShapeRenderer().rect(0, 0, WORLD_WIDTH, WORLD_HEIGHT / 5);
        
        // Land edge (dock-like)
        shapeRenderer.setColor(DOCK_COLOR);
        shapeRenderer.getShapeRenderer().rect(0, WORLD_HEIGHT / 5, WORLD_WIDTH, 10);
        
        // Water
        shapeRenderer.setColor(WATER_COLOR);
        shapeRenderer.getShapeRenderer().rect(0, WORLD_HEIGHT / 5 + 10, WORLD_WIDTH, WORLD_HEIGHT * 0.6f - 10);
        
        shapeRenderer.end();
        
        // Draw waves - use a separate begin/end block for the lines
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(WAVES_COLOR);
        
        // Draw several wave lines
        float waveY = WORLD_HEIGHT / 5 + 10;
        for (int i = 0; i < 10; i++) {
            drawWaveLine(waveY + i * 20, 0.5f);
        }
        
        shapeRenderer.end();
    }
    
    /**
     * Helper method to draw a cloud
     */
    private void drawCloud(float x, float y, float scale) {
        shapeRenderer.setColor(CLOUD_COLOR);
        shapeRenderer.getShapeRenderer().circle(x, y, 20 * scale);
        shapeRenderer.getShapeRenderer().circle(x + 15 * scale, y + 10 * scale, 15 * scale);
        shapeRenderer.getShapeRenderer().circle(x + 25 * scale, y - 5 * scale, 17 * scale);
        shapeRenderer.getShapeRenderer().circle(x - 20 * scale, y, 15 * scale);
    }
    
    /**
     * Helper method to draw a wave line
     */
    private void drawWaveLine(float y, float amplitude) {
        float waveFrequency = 10f;
        float lastX = 0;
        float lastY = y + (float) Math.sin(0) * amplitude * 5;
        
        for (float x = 10; x <= WORLD_WIDTH; x += 10) {
            float newY = y + (float) Math.sin(x / waveFrequency) * amplitude * 5;
            shapeRenderer.getShapeRenderer().line(lastX, lastY, x, newY);
            lastX = x;
            lastY = newY;
        }
    }
    
    private void update(float delta) {
        // Update GameManager
        gameManager.update(delta);
        
        // Update UI elements
        experienceBar.update(delta);
        levelUpPopup.update(delta);
        
        // Check for level up
        checkForLevelUp();
    }
    
    /**
     * Checks if player has leveled up and shows popup if necessary
     */
    private void checkForLevelUp() {
        // Only check for level up if no popups are currently shown and we're not in the middle of fishing
        if (!levelUpPopup.isActive() && !gameManager.isShowingFishCaught() && 
            !gameManager.isMinigameActive()) {
            
            // Check if level up flag is set
            if (progressionManager.checkAndClearLevelUpFlag()) {
                // Show level up popup
                levelUpPopup.show(progressionManager.getCurrentLevel(), null);
            }
        }
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
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
    public void hide() {
        // Called when this screen is no longer the current screen
    }
    
    @Override
    public void dispose() {
        batch.dispose();
        gameManager.dispose();
        experienceBar.dispose();
        levelUpPopup.dispose();
        fishGalleryScreen.dispose();
        shapeRenderer.dispose();
        buttonFont.dispose();
    }
    
    public SpriteBatch getBatch() {
        return batch;
    }
    
    private boolean isPointInLogButton(float x, float y) {
        if (gameManager.isMinigameActive() || gameManager.isShowingFishCaught()) {
            return false; // Log button should not be clickable during minigame or fish caught screen
        }
        float distance = Vector2.dst(LOG_BUTTON_X + LOG_BUTTON_SIZE / 2, LOG_BUTTON_Y + LOG_BUTTON_SIZE / 2, x, y);
        return distance <= LOG_BUTTON_SIZE / 2;
    }
    
    /**
     * Custom input handler that also handles popup clicks
     */
    private class CustomInputHandler extends InputHandler {
        private final GameManager gameManager;
        private final LevelUpPopup levelUpPopup;
        private final FishGalleryScreen fishGalleryScreen;
        private final Viewport viewport;
        private final Vector3 touchPoint;
        
        public CustomInputHandler(GameManager gameManager, Viewport viewport, 
                                 LevelUpPopup levelUpPopup, FishGalleryScreen fishGalleryScreen) {
            this.gameManager = gameManager;
            this.viewport = viewport;
            this.touchPoint = new Vector3();
            this.levelUpPopup = levelUpPopup;
            this.fishGalleryScreen = fishGalleryScreen;
        }
        
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            // Prioritize popups and gallery that cover the whole screen
            if (levelUpPopup.isActive()) {
                return levelUpPopup.handleClick(viewport.unproject(new Vector3(screenX, screenY, 0)).x, 
                                                 viewport.unproject(new Vector3(screenX, screenY, 0)).y);
            } else if (fishGalleryScreen.isActive()) {
                // The FishGalleryScreen's stage now handles input directly
                return fishGalleryScreen.touchDown(screenX, screenY, pointer, button);
            }

            Vector3 worldCoordinates = viewport.unproject(new Vector3(screenX, screenY, 0));
            float worldX = worldCoordinates.x;
            float worldY = worldCoordinates.y;

            if (isPointInLogButton(worldX, worldY)) {
                fishGalleryScreen.toggleVisibility();
                return true; // Click handled
            }
            
            // If no specific UI element handled the click, pass to GameManager for game logic
            gameManager.handleClick(worldX, worldY);
            
            return true;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (fishGalleryScreen.isActive()) {
                return fishGalleryScreen.touchUp(screenX, screenY, pointer, button);
            }
            return super.touchUp(screenX, screenY, pointer, button);
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (fishGalleryScreen.isActive()) {
                return fishGalleryScreen.touchDragged(screenX, screenY, pointer);
            }
            return super.touchDragged(screenX, screenY, pointer);
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            if (fishGalleryScreen.isActive()) {
                return fishGalleryScreen.mouseMoved(screenX, screenY);
            }
            return super.mouseMoved(screenX, screenY);
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            if (fishGalleryScreen.isActive()) {
                return fishGalleryScreen.scrolled(amountX, amountY);
            }
            return super.scrolled(amountX, amountY);
        }

        @Override
        public boolean keyDown(int keycode) {
            if (fishGalleryScreen.isActive()) {
                return fishGalleryScreen.keyDown(keycode);
            }
            return super.keyDown(keycode);
        }

        @Override
        public boolean keyUp(int keycode) {
            if (fishGalleryScreen.isActive()) {
                return fishGalleryScreen.keyUp(keycode);
            }
            return super.keyUp(keycode);
        }

        @Override
        public boolean keyTyped(char character) {
            if (fishGalleryScreen.isActive()) {
                return fishGalleryScreen.keyTyped(character);
            }
            return super.keyTyped(character);
        }
    }
} 