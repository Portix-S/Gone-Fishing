package io.github.gone.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.gone.progression.ProgressionManager;
import io.github.gone.utils.ShapeRendererManager;

/**
 * UI component that displays the player's current level and experience progress.
 */
public class ExperienceBar {
    // Constants
    private static final float BAR_HEIGHT = 20f;
    private static final float BAR_PADDING = 5f;
    private static final float LEVEL_CIRCLE_SIZE = 40f;
    
    // Colors
    private static final Color BACKGROUND_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.8f);
    private static final Color PROGRESS_COLOR = new Color(0.1f, 0.6f, 1f, 1f);
    private static final Color TEXT_COLOR = new Color(1f, 1f, 1f, 1f);
    private static final Color LEVEL_CIRCLE_COLOR = new Color(0.8f, 0.4f, 0f, 1f);
    
    // Position and dimensions
    private float x;
    private float y;
    private float width;
    
    // Rendering
    private final ShapeRendererManager shapeRenderer;
    private final BitmapFont font;
    private final GlyphLayout layout;
    
    // Progression
    private final ProgressionManager progressionManager;
    
    // Animation
    private float currentFillPercent;
    private float targetFillPercent;
    private boolean animating;
    
    public ExperienceBar(float x, float y, float width) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.shapeRenderer = new ShapeRendererManager();
        this.font = new BitmapFont();
        this.font.setColor(TEXT_COLOR);
        this.layout = new GlyphLayout();
        this.progressionManager = ProgressionManager.getInstance();
        
        // Initialize fill percentage
        updateTargetFillPercent();
        this.currentFillPercent = this.targetFillPercent;
        this.animating = false;
    }
    
    /**
     * Updates the experience bar animation and state
     */
    public void update(float delta) {
        // Check if we need to animate
        if (animating) {
            // Animate the fill towards the target
            if (Math.abs(currentFillPercent - targetFillPercent) < 0.01f) {
                currentFillPercent = targetFillPercent;
                animating = false;
            } else {
                // Smoothly move towards target value
                currentFillPercent += (targetFillPercent - currentFillPercent) * delta * 3;
            }
        } else {
            // Update the target percentage (in case of progression changes)
            float oldTarget = targetFillPercent;
            updateTargetFillPercent();
            
            // If target changed, start animation
            if (Math.abs(oldTarget - targetFillPercent) > 0.01f) {
                animating = true;
            }
        }
    }
    
    /**
     * Calculates the percentage fill for the XP bar based on current progression
     */
    private void updateTargetFillPercent() {
        int currentExp = progressionManager.getCurrentExp();
        int currentLevel = progressionManager.getCurrentLevel();
        int nextLevelRequirement = progressionManager.getNextLevelRequirement();
        
        if (nextLevelRequirement == 0) {
            // Max level reached
            targetFillPercent = 1.0f;
            return;
        }
        
        // Calculate previous level's XP requirement
        int prevLevelRequirement = 0;
        if (currentLevel > 1) {
            // This is a simplified approach - ideally we'd get this from progression manager
            prevLevelRequirement = progressionManager.getLevelRequirement(currentLevel);
        }
        
        // Calculate progress within current level
        int expInCurrentLevel = currentExp - prevLevelRequirement;
        int expRequiredForNextLevel = nextLevelRequirement - prevLevelRequirement;
        
        targetFillPercent = (float) expInCurrentLevel / expRequiredForNextLevel;
        targetFillPercent = Math.min(1.0f, Math.max(0.0f, targetFillPercent)); // Clamp between 0 and 1
    }
    
    /**
     * Triggers an animation to show XP gain
     */
    public void showXpGain() {
        updateTargetFillPercent();
        animating = true;
    }
    
    /**
     * Refreshes the bar to immediately reflect current progression values
     */
    public void refresh() {
        updateTargetFillPercent();
        currentFillPercent = targetFillPercent;
        animating = false;
    }
    
    /**
     * Draws the experience bar
     */
    public void draw(SpriteBatch batch) {
        // Store batch color
        Color batchColor = batch.getColor();
        
        // End batch to use ShapeRenderer
        batch.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw level circle background
        shapeRenderer.setColor(LEVEL_CIRCLE_COLOR);
        shapeRenderer.getShapeRenderer().circle(x + LEVEL_CIRCLE_SIZE/2, y + BAR_HEIGHT/2, LEVEL_CIRCLE_SIZE/2);
        
        // Draw bar background
        float barX = x + LEVEL_CIRCLE_SIZE + BAR_PADDING;
        float barWidth = width - LEVEL_CIRCLE_SIZE - BAR_PADDING;
        
        shapeRenderer.setColor(BACKGROUND_COLOR);
        shapeRenderer.getShapeRenderer().rect(barX, y, barWidth, BAR_HEIGHT);
        
        // Draw progress fill
        shapeRenderer.setColor(PROGRESS_COLOR);
        shapeRenderer.getShapeRenderer().rect(barX, y, barWidth * currentFillPercent, BAR_HEIGHT);
        
        shapeRenderer.end();
        
        // Resume batch for text
        batch.begin();
        batch.setColor(batchColor);
        
        // Draw level number in circle
        font.getData().setScale(1.2f);
        String levelText = Integer.toString(progressionManager.getCurrentLevel());
        layout.setText(font, levelText);
        font.draw(batch, levelText, 
            x + LEVEL_CIRCLE_SIZE/2 - layout.width/2, 
            y + BAR_HEIGHT/2 + layout.height/2);
        
        // Draw XP text
        font.getData().setScale(0.9f);
        int currentExp = progressionManager.getCurrentExp();
        int nextLevelReq = progressionManager.getNextLevelRequirement();
        
        String xpText = "";
        if (nextLevelReq > 0) {
            xpText = currentExp + "/" + nextLevelReq + " XP";
        } else {
            xpText = "MAX LEVEL";
        }
        
        layout.setText(font, xpText);
        font.draw(batch, xpText, 
            barX + barWidth/2 - layout.width/2, 
            y + BAR_HEIGHT/2 + layout.height/2);
            
        // Reset font scale
        font.getData().setScale(1.0f);
    }
    
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (font != null) {
            font.dispose();
        }
    }
} 