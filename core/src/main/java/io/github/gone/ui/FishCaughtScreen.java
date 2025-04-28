package io.github.gone.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.gone.fish.Fish;
import io.github.gone.progression.ProgressionManager;
import io.github.gone.utils.ShapeRendererManager;

/**
 * Screen shown when a fish is caught, displaying fish information and stats.
 */
public class FishCaughtScreen {
    // UI Constants
    private static final float SCREEN_WIDTH = Gdx.graphics.getWidth();
    private static final float SCREEN_HEIGHT = Gdx.graphics.getHeight();
    private static final float PANEL_WIDTH = SCREEN_WIDTH * 0.8f;
    private static final float PANEL_HEIGHT = SCREEN_HEIGHT * 0.7f;
    private static final float PANEL_X = (SCREEN_WIDTH - PANEL_WIDTH) / 2;
    private static final float PANEL_Y = (SCREEN_HEIGHT - PANEL_HEIGHT) / 2;
    
    // Colors
    private static final Color OVERLAY_COLOR = new Color(0, 0, 0, 0.7f);
    private static final Color PANEL_COLOR = new Color(0.1f, 0.1f, 0.1f, 0.9f);
    private static final Color TITLE_COLOR = new Color(1, 0.8f, 0.2f, 1);
    private static final Color TEXT_COLOR = new Color(1, 1, 1, 1);
    
    // Rarity colors
    private static final Color COMMON_COLOR = new Color(0.8f, 0.8f, 0.8f, 1);
    private static final Color RARE_COLOR = new Color(0.2f, 0.4f, 1, 1);
    private static final Color LEGENDARY_COLOR = new Color(1, 0.6f, 0, 1);
    
    // Content
    private Fish caughtFish;
    private boolean isActive;
    private float fishAnimationTimer;
    
    // Rendering
    private final ShapeRendererManager shapeRenderer;
    private final BitmapFont titleFont;
    private final BitmapFont textFont;
    private final BitmapFont instructionFont;
    private final GlyphLayout layout;
    
    // Experience
    private final ProgressionManager progressionManager;
    private int expGained;
    
    // Callback for when the screen is closed
    public interface Callback {
        void onClose();
    }
    
    private Callback callback;
    
    public FishCaughtScreen() {
        this.shapeRenderer = new ShapeRendererManager();
        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(2.5f);
        this.textFont = new BitmapFont();
        this.textFont.getData().setScale(1.5f);
        this.instructionFont = new BitmapFont();
        this.instructionFont.getData().setScale(1.2f);
        this.layout = new GlyphLayout();
        this.progressionManager = ProgressionManager.getInstance();
        this.isActive = false;
    }
    
    /**
     * Shows the fish caught screen with the given fish
     */
    public void show(Fish fish, Callback callback) {
        this.caughtFish = fish;
        this.callback = callback;
        this.isActive = true;
        this.fishAnimationTimer = 0;
        
        // Log fish added to inventory
        Gdx.app.log("FishCaughtScreen", "===============================================");
        Gdx.app.log("FishCaughtScreen", "FISH ADDED TO INVENTORY");
        Gdx.app.log("FishCaughtScreen", "Name: " + fish.getName());
        Gdx.app.log("FishCaughtScreen", "Class: " + fish.getClass().getSimpleName());
        Gdx.app.log("FishCaughtScreen", "Rarity: " + fish.getRarity() + " (" + getRarityName(fish.getRarity()) + ")");
        Gdx.app.log("FishCaughtScreen", "Weight: " + fish.getWeight() + " KG");
        Gdx.app.log("FishCaughtScreen", "Description: " + fish.getDescription());
        Gdx.app.log("FishCaughtScreen", "Total Weight: " + progressionManager.getTotalWeight() + " KG");
        Gdx.app.log("FishCaughtScreen", "Total Fish: " + progressionManager.getTotalFishCaught());
        Gdx.app.log("FishCaughtScreen", "===============================================");
        
        // Add experience for the caught fish
        this.expGained = 10 * fish.getRarity(); // Base XP calculation
        progressionManager.addExperienceForFish(fish, fish.getWeight());
    }
    
    /**
     * Updates the animation state
     */
    public void update(float delta) {
        if (!isActive) return;
        
        fishAnimationTimer += delta;
    }
    
    /**
     * Returns the rarity name based on the rarity level
     */
    private String getRarityName(int rarity) {
        switch (rarity) {
            case 3: return "LEGENDARY";
            case 2: return "RARE";
            default: return "COMMON";
        }
    }
    
    /**
     * Draws the fish caught screen
     */
    public void draw(SpriteBatch batch) {
        if (!isActive) return;
        
        // Need to end batch to use ShapeRenderer
        batch.end();
        
        // Draw darkened overlay
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(OVERLAY_COLOR);
        shapeRenderer.getShapeRenderer().rect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        
        // Draw panel background
        shapeRenderer.setColor(PANEL_COLOR);
        shapeRenderer.getShapeRenderer().rect(PANEL_X, PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT);
        
        // Draw decorative header banner
        shapeRenderer.setColor(TITLE_COLOR);
        shapeRenderer.getShapeRenderer().rect(PANEL_X, PANEL_Y + PANEL_HEIGHT - 60, PANEL_WIDTH, 60);
        
        shapeRenderer.end();
        
        // Resume batch for text
        batch.begin();
        
        // Draw title
        titleFont.setColor(Color.WHITE);
        String title = "Congratulations";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title, PANEL_X + (PANEL_WIDTH - layout.width) / 2, PANEL_Y + PANEL_HEIGHT - 20);
        
        // Draw "Fish caught" text
        textFont.setColor(TEXT_COLOR);
        String caughtText = "Fish caught!";
        layout.setText(textFont, caughtText);
        textFont.draw(batch, caughtText, PANEL_X + (PANEL_WIDTH - layout.width) / 2, PANEL_Y + PANEL_HEIGHT - 80);
        
        // Draw fish
        float fishX = PANEL_X + PANEL_WIDTH / 2;
        float fishY = PANEL_Y + PANEL_HEIGHT / 2 + 20;
        
        // Apply a slight bobbing animation
        float bobOffset = (float) Math.sin(fishAnimationTimer * 3) * 5;
        caughtFish.draw(batch, fishX, fishY + bobOffset);
        
        // Draw fish weight with pink circular background
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.9f, 0.3f, 0.9f, 1)); // Pink color
        shapeRenderer.getShapeRenderer().circle(fishX + 80, fishY + 30, 25);
        shapeRenderer.end();
        batch.begin();
        
        // Weight text
        textFont.setColor(Color.WHITE);
        String weightText = String.format("%.2f", caughtFish.getWeight()) + "\nKG";
        layout.setText(textFont, weightText);
        textFont.draw(batch, weightText, fishX + 80 - layout.width / 2, fishY + 35);
        
        // Get rarity color based on fish rarity
        Color rarityColor;
        switch (caughtFish.getRarity()) {
            case 3:
                rarityColor = LEGENDARY_COLOR;
                break;
            case 2:
                rarityColor = RARE_COLOR;
                break;
            default:
                rarityColor = COMMON_COLOR;
        }
        
        // Fish name with appropriate rarity color
        textFont.setColor(rarityColor);
        layout.setText(textFont, caughtFish.getName());
        textFont.draw(batch, caughtText = caughtFish.getName(), PANEL_X + (PANEL_WIDTH - layout.width) / 2, PANEL_Y + PANEL_HEIGHT / 2 - 50);
        
        // Fish rarity text
        String rarityText = getRarityName(caughtFish.getRarity());
        layout.setText(textFont, rarityText);
        textFont.draw(batch, rarityText, PANEL_X + (PANEL_WIDTH - layout.width) / 2, PANEL_Y + PANEL_HEIGHT / 2 - 80);
        
        // Experience gained
        textFont.setColor(Color.YELLOW);
        String expText = "+" + expGained + " XP";
        layout.setText(textFont, expText);
        textFont.draw(batch, expText, PANEL_X + (PANEL_WIDTH - layout.width) / 2, PANEL_Y + PANEL_HEIGHT / 2 - 110);
        
        // Total weight text
        textFont.setColor(Color.WHITE);
        String weightTitle = "Total Weight:";
        layout.setText(textFont, weightTitle);
        textFont.draw(batch, weightTitle, PANEL_X + 40, PANEL_Y + 70);
        
        String totalWeightText = String.format("%.2f KG", progressionManager.getTotalWeight());
        textFont.draw(batch, totalWeightText, PANEL_X + PANEL_WIDTH - 150, PANEL_Y + 70);
        
        // Instruction text
        instructionFont.setColor(Color.WHITE);
        String instruction = "Tap empty area to close";
        layout.setText(instructionFont, instruction);
        instructionFont.draw(batch, instruction, PANEL_X + (PANEL_WIDTH - layout.width) / 2, PANEL_Y + 30);
    }
    
    /**
     * Handle touch input
     */
    public boolean handleClick(float x, float y) {
        if (!isActive) return false;
        
        // Any click will close the screen
        isActive = false;
        
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
        if (instructionFont != null) {
            instructionFont.dispose();
        }
    }
} 