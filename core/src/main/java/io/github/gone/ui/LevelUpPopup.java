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
 * Displays a popup when the player levels up, informing them they can now catch new fish.
 */
public class LevelUpPopup {
    // UI Constants
    private static final float POPUP_WIDTH = 350f;
    private static final float POPUP_HEIGHT = 200f;
    private static final float POPUP_PADDING = 20f;
    
    // Animation constants
    private static final float SHOW_DURATION = 0.5f;
    private static final float DISPLAY_DURATION = 3.0f;
    private static final float HIDE_DURATION = 0.5f;
    
    // State
    private boolean isActive;
    private float animationTimer;
    private float alpha;
    private int newLevel;
    
    // Position
    private float x;
    private float y;
    
    // Rendering
    private final ShapeRendererManager shapeRenderer;
    private final BitmapFont titleFont;
    private final BitmapFont textFont;
    private final GlyphLayout layout;
    
    // Callback
    public interface Callback {
        void onClose();
    }
    
    private Callback callback;
    
    public LevelUpPopup() {
        this.shapeRenderer = new ShapeRendererManager();
        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(1.8f);
        this.textFont = new BitmapFont();
        this.textFont.getData().setScale(1.2f);
        this.layout = new GlyphLayout();
        
        this.isActive = false;
        this.animationTimer = 0f;
        this.alpha = 0f;
        
        // Center the popup
        this.x = (Gdx.graphics.getWidth() - POPUP_WIDTH) / 2;
        this.y = (Gdx.graphics.getHeight() - POPUP_HEIGHT) / 2;
    }
    
    /**
     * Shows the level up popup
     */
    public void show(int newLevel, Callback callback) {
        this.newLevel = newLevel;
        this.callback = callback;
        this.isActive = true;
        this.animationTimer = 0f;
        this.alpha = 0f;
    }
    
    /**
     * Updates the popup animation
     */
    public void update(float delta) {
        if (!isActive) return;
        
        animationTimer += delta;
        
        // Handle fade in
        if (animationTimer <= SHOW_DURATION) {
            alpha = animationTimer / SHOW_DURATION;
        }
        // Display fully
        else if (animationTimer <= SHOW_DURATION + DISPLAY_DURATION) {
            alpha = 1f;
        }
        // Handle fade out
        else if (animationTimer <= SHOW_DURATION + DISPLAY_DURATION + HIDE_DURATION) {
            alpha = 1f - ((animationTimer - SHOW_DURATION - DISPLAY_DURATION) / HIDE_DURATION);
        }
        // Animation complete
        else {
            isActive = false;
            alpha = 0f;
            if (callback != null) {
                callback.onClose();
            }
        }
    }
    
    /**
     * Draws the level up popup
     */
    public void draw(SpriteBatch batch) {
        if (!isActive || alpha <= 0) return;
        
        // End SpriteBatch to use ShapeRenderer
        batch.end();
        
        // Draw popup background with alpha
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw darkened overlay
        shapeRenderer.setColor(new Color(0, 0, 0, 0.5f * alpha));
        shapeRenderer.getShapeRenderer().rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        // Draw popup background
        shapeRenderer.setColor(new Color(0.1f, 0.1f, 0.3f, 0.9f * alpha));
        shapeRenderer.getShapeRenderer().rect(x, y, POPUP_WIDTH, POPUP_HEIGHT);
        
        // Draw title background
        shapeRenderer.setColor(new Color(0.6f, 0.2f, 0.8f, alpha));
        shapeRenderer.getShapeRenderer().rect(x, y + POPUP_HEIGHT - 50, POPUP_WIDTH, 50);
        
        shapeRenderer.end();
        
        // Resume SpriteBatch for text
        batch.begin();
        
        // Draw title
        titleFont.setColor(new Color(1, 1, 1, alpha));
        String titleText = "Level Up!";
        layout.setText(titleFont, titleText);
        titleFont.draw(batch, titleText, 
            x + (POPUP_WIDTH - layout.width) / 2, 
            y + POPUP_HEIGHT - 15);
        
        // Draw level information
        textFont.setColor(new Color(1, 1, 0, alpha));
        String levelText = "You are now Level " + newLevel + "!";
        layout.setText(textFont, levelText);
        textFont.draw(batch, levelText,
            x + (POPUP_WIDTH - layout.width) / 2,
            y + POPUP_HEIGHT - 70);
        
        // Draw message about new fish
        textFont.setColor(new Color(1, 1, 1, alpha));
        String message = "You can now find new types of fish!";
        layout.setText(textFont, message);
        textFont.draw(batch, message,
            x + (POPUP_WIDTH - layout.width) / 2,
            y + POPUP_HEIGHT/2 - 20);
        
        // Draw fishing tips
        String tipsText = "Keep fishing to discover all kinds of\nunderwater treasures and creatures!";
        layout.setText(textFont, tipsText);
        textFont.draw(batch, tipsText,
            x + (POPUP_WIDTH - layout.width) / 2,
            y + 70);
    }
    
    /**
     * Handle click on the popup
     */
    public boolean handleClick(float clickX, float clickY) {
        if (!isActive || alpha < 0.5f) return false;
        
        // Skip the rest of the animation on click
        isActive = false;
        if (callback != null) {
            callback.onClose();
        }
        return true;
    }
    
    /**
     * Check if the popup is currently active
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
    }
} 