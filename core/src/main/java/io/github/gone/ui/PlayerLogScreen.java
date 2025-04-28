package io.github.gone.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.gone.progression.ProgressionManager;
import io.github.gone.utils.ShapeRendererManager;

/**
 * Screen that displays player statistics and provides a reset option.
 */
public class PlayerLogScreen {
    // UI Constants
    private static final float SCREEN_WIDTH = Gdx.graphics.getWidth();
    private static final float SCREEN_HEIGHT = Gdx.graphics.getHeight();
    private static final float PANEL_WIDTH = SCREEN_WIDTH * 0.8f;
    private static final float PANEL_HEIGHT = SCREEN_HEIGHT * 0.7f;
    private static final float PANEL_X = (SCREEN_WIDTH - PANEL_WIDTH) / 2;
    private static final float PANEL_Y = (SCREEN_HEIGHT - PANEL_HEIGHT) / 2;
    
    // Button constants
    private static final float BUTTON_WIDTH = 200f;
    private static final float BUTTON_HEIGHT = 60f;
    private static final float BUTTON_PADDING = 15f;
    private static final float RESET_BUTTON_Y = PANEL_Y + 80f;
    private static final float CLOSE_BUTTON_Y = PANEL_Y + 20f;
    
    // Colors
    private static final Color OVERLAY_COLOR = new Color(0, 0, 0, 0.7f);
    private static final Color PANEL_COLOR = new Color(0.1f, 0.1f, 0.1f, 0.9f);
    private static final Color TITLE_COLOR = new Color(0.2f, 0.4f, 0.8f, 1f);
    private static final Color TEXT_COLOR = new Color(1f, 1f, 1f, 1f);
    private static final Color BUTTON_COLOR = new Color(0.2f, 0.6f, 0.8f, 1f);
    private static final Color RESET_BUTTON_COLOR = new Color(0.8f, 0.2f, 0.2f, 1f);
    
    // Content
    private boolean isActive;
    
    // Rendering
    private final ShapeRendererManager shapeRenderer;
    private final BitmapFont titleFont;
    private final BitmapFont textFont;
    private final BitmapFont buttonFont;
    private final GlyphLayout layout;
    
    // Progression
    private final ProgressionManager progressionManager;
    
    // Callback for when reset is pressed
    public interface Callback {
        void onClose();
        void onReset();
    }
    
    private Callback callback;
    
    public PlayerLogScreen() {
        this.shapeRenderer = new ShapeRendererManager();
        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(2f);
        this.textFont = new BitmapFont();
        this.textFont.getData().setScale(1.5f);
        this.buttonFont = new BitmapFont();
        this.buttonFont.getData().setScale(1.2f);
        this.layout = new GlyphLayout();
        this.progressionManager = ProgressionManager.getInstance();
        this.isActive = false;
    }
    
    /**
     * Shows the player log screen
     */
    public void show(Callback callback) {
        this.callback = callback;
        this.isActive = true;
    }
    
    /**
     * Hide the player log screen
     */
    public void hide() {
        this.isActive = false;
    }
    
    /**
     * Draw the player log screen
     */
    public void draw(SpriteBatch batch) {
        if (!isActive) return;
        
        // End batch to use ShapeRenderer
        batch.end();
        
        // Draw darkened overlay
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(OVERLAY_COLOR);
        shapeRenderer.getShapeRenderer().rect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        
        // Draw panel background
        shapeRenderer.setColor(PANEL_COLOR);
        shapeRenderer.getShapeRenderer().rect(PANEL_X, PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT);
        
        // Draw header banner
        shapeRenderer.setColor(TITLE_COLOR);
        shapeRenderer.getShapeRenderer().rect(PANEL_X, PANEL_Y + PANEL_HEIGHT - 60, PANEL_WIDTH, 60);
        
        // Draw reset button
        shapeRenderer.setColor(RESET_BUTTON_COLOR);
        float resetButtonX = PANEL_X + (PANEL_WIDTH - BUTTON_WIDTH) / 2;
        shapeRenderer.getShapeRenderer().rect(resetButtonX, RESET_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        
        // Draw close button
        shapeRenderer.setColor(BUTTON_COLOR);
        float closeButtonX = PANEL_X + (PANEL_WIDTH - BUTTON_WIDTH) / 2;
        shapeRenderer.getShapeRenderer().rect(closeButtonX, CLOSE_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        
        shapeRenderer.end();
        
        // Resume batch for text
        batch.begin();
        
        // Draw title
        titleFont.setColor(Color.WHITE);
        String title = "Player Stats";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title, 
            PANEL_X + (PANEL_WIDTH - layout.width) / 2, 
            PANEL_Y + PANEL_HEIGHT - 20);
        
        // Draw player level
        textFont.setColor(TEXT_COLOR);
        String levelText = "Level: " + progressionManager.getCurrentLevel();
        layout.setText(textFont, levelText);
        textFont.draw(batch, levelText, 
            PANEL_X + PANEL_WIDTH / 4, 
            PANEL_Y + PANEL_HEIGHT - 100);
        
        // Draw XP
        String xpText = "XP: " + progressionManager.getCurrentExp() + 
                        "/" + progressionManager.getNextLevelRequirement();
        layout.setText(textFont, xpText);
        textFont.draw(batch, xpText, 
            PANEL_X + PANEL_WIDTH / 4, 
            PANEL_Y + PANEL_HEIGHT - 140);
        
        // Draw total fish caught
        String fishText = "Total Fish Caught: " + progressionManager.getTotalFishCaught();
        layout.setText(textFont, fishText);
        textFont.draw(batch, fishText, 
            PANEL_X + PANEL_WIDTH / 4, 
            PANEL_Y + PANEL_HEIGHT - 180);
        
        // Draw total weight
        String weightText = "Total Weight: " + String.format("%.2f", progressionManager.getTotalWeight()) + " KG";
        layout.setText(textFont, weightText);
        textFont.draw(batch, weightText, 
            PANEL_X + PANEL_WIDTH / 4, 
            PANEL_Y + PANEL_HEIGHT - 220);
        
        // Draw reset button text
        buttonFont.setColor(Color.WHITE);
        String resetText = "Reset Progress";
        layout.setText(buttonFont, resetText);
        buttonFont.draw(batch, resetText, 
            resetButtonX + (BUTTON_WIDTH - layout.width) / 2, 
            RESET_BUTTON_Y + BUTTON_HEIGHT - BUTTON_PADDING);
        
        // Draw close button text
        String closeText = "Close";
        layout.setText(buttonFont, closeText);
        buttonFont.draw(batch, closeText, 
            closeButtonX + (BUTTON_WIDTH - layout.width) / 2, 
            CLOSE_BUTTON_Y + BUTTON_HEIGHT - BUTTON_PADDING);
    }
    
    /**
     * Handle touch input on the log screen
     */
    public boolean handleClick(float x, float y) {
        if (!isActive) return false;
        
        float resetButtonX = PANEL_X + (PANEL_WIDTH - BUTTON_WIDTH) / 2;
        float closeButtonX = PANEL_X + (PANEL_WIDTH - BUTTON_WIDTH) / 2;
        
        // Check if reset button was clicked
        if (x >= resetButtonX && x <= resetButtonX + BUTTON_WIDTH &&
            y >= RESET_BUTTON_Y && y <= RESET_BUTTON_Y + BUTTON_HEIGHT) {
            
            // Log reset action
            Gdx.app.log("PlayerLog", "Player requested progress reset");
            
            if (callback != null) {
                callback.onReset();
            }
            return true;
        }
        
        // Check if close button was clicked
        if (x >= closeButtonX && x <= closeButtonX + BUTTON_WIDTH &&
            y >= CLOSE_BUTTON_Y && y <= CLOSE_BUTTON_Y + BUTTON_HEIGHT) {
            
            hide();
            if (callback != null) {
                callback.onClose();
            }
            return true;
        }
        
        // Click outside buttons just closes the screen
        hide();
        if (callback != null) {
            callback.onClose();
        }
        return true;
    }
    
    /**
     * Check if the screen is currently active
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Clean up resources
     */
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (titleFont != null) {
            titleFont.dispose();
        }
        if (textFont != null) {
            textFont.dispose();
        }
        if (buttonFont != null) {
            buttonFont.dispose();
        }
    }
} 