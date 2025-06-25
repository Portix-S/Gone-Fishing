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
import io.github.gone.entities.FishingRod;
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
    private final FishingRod fishingRod;
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
        
        // Position fishing rod at bottom left
        fishingRod = new FishingRod(new Vector2(WORLD_WIDTH / 2, WORLD_HEIGHT / 4));
        
        // Initialize custom input handler
        inputHandler = new CustomInputHandler(fishingRod, viewport, levelUpPopup, fishGalleryScreen);
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
        fishingRod.draw(batch);
        
        // Draw UI elements
        experienceBar.draw(batch);
        
        batch.end();
        
        // Only show log button when not in throw minigame
        if (!fishingRod.isInThrowMinigame() && !fishingRod.isShowingFishCaught()) {
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
                fishGalleryScreen.draw(batch);
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
                fishGalleryScreen.draw(batch);
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
        // Update fishing rod
        fishingRod.update(delta);
        
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
        if (!levelUpPopup.isActive() && !fishingRod.isShowingFishCaught() && 
            !fishingRod.isFishing() && !fishingRod.isInThrowMinigame()) {
            
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
        // Dispose of resources
        fishingRod.dispose();
        experienceBar.dispose();
        levelUpPopup.dispose();
        fishGalleryScreen.dispose();
        shapeRenderer.dispose();
        buttonFont.dispose();
    }
    
    // Helper method to check if point is inside log button
    private boolean isPointInLogButton(float x, float y) {
        return x >= LOG_BUTTON_X && x <= LOG_BUTTON_X + LOG_BUTTON_SIZE &&
               y >= LOG_BUTTON_Y && y <= LOG_BUTTON_Y + LOG_BUTTON_SIZE;
    }
    
    /**
     * Custom input handler that also handles popup clicks
     */
    private class CustomInputHandler extends InputHandler {
        private final LevelUpPopup levelUpPopup;
        private final FishGalleryScreen fishGalleryScreen;
        
        public CustomInputHandler(FishingRod fishingRod, Viewport viewport, 
                                 LevelUpPopup levelUpPopup, FishGalleryScreen fishGalleryScreen) {
            super(fishingRod, viewport);
            this.levelUpPopup = levelUpPopup;
            this.fishGalleryScreen = fishGalleryScreen;
        }
        
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            // Convert screen coordinates to world coordinates
            Vector3 touchPoint = new Vector3(screenX, screenY, 0);
            viewport.unproject(touchPoint);
            
            // Check if any popup is active and handle its click first
            if (levelUpPopup.isActive() && levelUpPopup.handleClick(touchPoint.x, touchPoint.y)) {
                return true;
            }
            if (fishGalleryScreen.isActive() && fishGalleryScreen.handleClick(touchPoint.x, touchPoint.y)) {
                return true;
            }
            
            // Check for log button click only if no popups are active and minigame is not active
            if (!fishingRod.isInThrowMinigame() && !fishingRod.isShowingFishCaught() && 
                isPointInLogButton(touchPoint.x, touchPoint.y)) {
                fishGalleryScreen.show(new FishGalleryScreen.Callback() {
                    @Override
                    public void onClose() {
                        // Nothing special needed when closing
                    }
                    
                    @Override
                    public void onReset() {
                        // Reset player progression and update UI
                        progressionManager.resetProgress();
                        experienceBar.refresh();
                    }
                });
                return true;
            }
            
            // If no popup handled the click, delegate to fishing rod
            return super.touchDown(screenX, screenY, pointer, button);
        }
    }
} 