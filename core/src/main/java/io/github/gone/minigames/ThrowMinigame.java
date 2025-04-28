package io.github.gone.minigames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.github.gone.utils.ShapeRendererManager;

/**
 * A minigame that requires the player to time their click
 * when a rotating line passes through a highlighted area.
 * Used to determine the throw distance and fish type.
 */
public class ThrowMinigame {
    // Constants
    private static final float CIRCLE_RADIUS = 60f;
    private static final float GREAT_ZONE_SIZE = 30f;
    private static final float GOOD_ZONE_SIZE = 20f; // Size for each "good" zone (before and after great zone)
    private static final float ROTATION_SPEED = 180f; // Degrees per second
    
    // Position
    private final float centerX;
    private final float centerY;
    
    // Success zones
    private float successZoneStartAngle; // Randomized on start
    
    // State
    private float currentAngle;
    private boolean isActive;
    private SuccessLevel successLevel;
    private float resultMessageTimer;
    private static final float RESULT_MESSAGE_DURATION = 1.5f;
    private boolean showResultOnly = false;
    
    // Define success levels
    public enum SuccessLevel {
        MISS(0, "Miss!", Color.RED),
        GOOD(1, "Good!", Color.YELLOW),
        GREAT(2, "Great!", Color.GREEN);
        
        private final int value;
        private final String message;
        private final Color color;
        
        SuccessLevel(int value, String message, Color color) {
            this.value = value;
            this.message = message;
            this.color = color;
        }
        
        public int getValue() {
            return value;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Color getColor() {
            return color;
        }
    }
    
    // Rendering
    private final ShapeRendererManager shapeRenderer;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout;
    
    // Events
    private ThrowMinigameListener listener;
    
    public interface ThrowMinigameListener {
        void onThrowComplete(SuccessLevel successLevel);
    }
    
    public ThrowMinigame(float centerX, float centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.currentAngle = 0;
        this.isActive = false;
        this.successLevel = SuccessLevel.MISS;
        this.resultMessageTimer = 0;
        this.successZoneStartAngle = 45f; // Default value, will be randomized
        
        this.shapeRenderer = new ShapeRendererManager();
        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);
        this.font.getData().setScale(2.0f);
        this.glyphLayout = new GlyphLayout();
    }
    
    public void setListener(ThrowMinigameListener listener) {
        this.listener = listener;
    }
    
    public void start() {
        isActive = true;
        currentAngle = 0;
        successLevel = SuccessLevel.MISS;
        resultMessageTimer = 0;
        showResultOnly = false;
        
        // Randomize the start angle for the success zone between 0 and 360 degrees
        successZoneStartAngle = MathUtils.random(0f, 360f);
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public boolean isShowingResult() {
        return !isActive && resultMessageTimer > 0;
    }
    
    public void update(float delta) {
        if (isActive) {
            // Update rotation angle
            currentAngle += ROTATION_SPEED * delta;
            if (currentAngle >= 360) {
                currentAngle = currentAngle % 360;
            }
        }
        
        // Update result message timer
        if (resultMessageTimer > 0) {
            resultMessageTimer -= delta;
            if (resultMessageTimer <= 0 && showResultOnly) {
                showResultOnly = false;
            }
        }
    }
    
    public void draw(SpriteBatch batch) {
        // If not active and not showing result, don't draw anything
        if (!isActive && !isShowingResult()) {
            return;
        }
        
        // End SpriteBatch to use ShapeRenderer
        batch.end();
        
        // Only draw the minigame circle and zones if active (not just showing result)
        if (isActive) {
            // Draw the skill check minigame
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            
            // Draw outer circle (background)
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.getShapeRenderer().circle(centerX, centerY, CIRCLE_RADIUS);
            
            // Left "Good" zone (before Great zone)
            shapeRenderer.setColor(Color.YELLOW);
            float goodZoneStartLeft = successZoneStartAngle - GOOD_ZONE_SIZE;
            shapeRenderer.getShapeRenderer().arc(centerX, centerY, CIRCLE_RADIUS - 5, 
                goodZoneStartLeft, GOOD_ZONE_SIZE, 20);
            
            // "Great" zone
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.getShapeRenderer().arc(centerX, centerY, CIRCLE_RADIUS - 5, 
                successZoneStartAngle, GREAT_ZONE_SIZE, 20);
            
            // Right "Good" zone (after Great zone)
            shapeRenderer.setColor(Color.YELLOW);
            float goodZoneStartRight = successZoneStartAngle + GREAT_ZONE_SIZE;
            shapeRenderer.getShapeRenderer().arc(centerX, centerY, CIRCLE_RADIUS - 5, 
                goodZoneStartRight, GOOD_ZONE_SIZE, 20);
                
            shapeRenderer.end();
            
            // Draw line (pointer)
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.RED);
            
            // Convert degrees to radians for calculations
            float radians = currentAngle * MathUtils.degreesToRadians;
            float lineEndX = centerX + MathUtils.cos(radians) * CIRCLE_RADIUS;
            float lineEndY = centerY + MathUtils.sin(radians) * CIRCLE_RADIUS;
            
            shapeRenderer.getShapeRenderer().line(centerX, centerY, lineEndX, lineEndY);
            shapeRenderer.end();
        }
        
        // Resume SpriteBatch for text rendering
        batch.begin();
        
        // If showing result message
        if (resultMessageTimer > 0) {
            String message = successLevel.getMessage();
            Color messageColor = successLevel.getColor();
            
            font.setColor(messageColor);
            glyphLayout.setText(font, message);
            font.draw(batch, message, centerX - glyphLayout.width / 2, centerY + CIRCLE_RADIUS + 40);
        }
        
        // Instruction text if active
        if (isActive) {
            font.setColor(Color.WHITE);
            String instruction = "Click to throw!";
            glyphLayout.setText(font, instruction);
            font.draw(batch, instruction, centerX - glyphLayout.width / 2, centerY - CIRCLE_RADIUS - 20);
        }
    }
    
    public void onClick() {
        if (!isActive) return;
        
        // Check which success zone the click landed in
        successLevel = checkSuccessZone(currentAngle);
        resultMessageTimer = RESULT_MESSAGE_DURATION;
        isActive = false;
        showResultOnly = true;
        
        // Log throw result information
        Gdx.app.log("ThrowMinigame", "===============================================");
        Gdx.app.log("ThrowMinigame", "THROW RESULT");
        Gdx.app.log("ThrowMinigame", "Success Level: " + successLevel);
        Gdx.app.log("ThrowMinigame", "Current Angle: " + currentAngle);
        Gdx.app.log("ThrowMinigame", "Target Angle: " + successZoneStartAngle);
        Gdx.app.log("ThrowMinigame", "Great Zone: " + successZoneStartAngle + " to " + (successZoneStartAngle + GREAT_ZONE_SIZE));
        Gdx.app.log("ThrowMinigame", "Left Good Zone: " + (successZoneStartAngle - GOOD_ZONE_SIZE) + " to " + successZoneStartAngle);
        Gdx.app.log("ThrowMinigame", "Right Good Zone: " + (successZoneStartAngle + GREAT_ZONE_SIZE) + " to " + (successZoneStartAngle + GREAT_ZONE_SIZE + GOOD_ZONE_SIZE));
        Gdx.app.log("ThrowMinigame", "===============================================");
        
        // Notify listener
        if (listener != null) {
            listener.onThrowComplete(successLevel);
        }
    }
    
    private SuccessLevel checkSuccessZone(float angle) {
        // Normalize angle to 0-360 range
        angle = normalizeAngle(angle);
        float targetAngle = normalizeAngle(successZoneStartAngle);
        
        // Check if angle is within "Great" zone
        if (isAngleInRange(angle, targetAngle, targetAngle + GREAT_ZONE_SIZE)) {
            return SuccessLevel.GREAT;
        }
        
        // Check if angle is within left "Good" zone
        if (isAngleInRange(angle, targetAngle - GOOD_ZONE_SIZE, targetAngle)) {
            return SuccessLevel.GOOD;
        }
        
        // Check if angle is within right "Good" zone
        if (isAngleInRange(angle, targetAngle + GREAT_ZONE_SIZE, targetAngle + GREAT_ZONE_SIZE + GOOD_ZONE_SIZE)) {
            return SuccessLevel.GOOD;
        }
        
        // If not in any success zone
        return SuccessLevel.MISS;
    }
    
    /**
     * Normalizes an angle to the 0-360 range
     */
    private float normalizeAngle(float angle) {
        angle = angle % 360;
        if (angle < 0) angle += 360;
        return angle;
    }
    
    /**
     * Checks if an angle is within a specific range, handling wraparound at 360 degrees
     */
    private boolean isAngleInRange(float angle, float start, float end) {
        // Normalize all angles to 0-360 range
        angle = normalizeAngle(angle);
        start = normalizeAngle(start);
        end = normalizeAngle(end);
        
        // Handle the case where the range wraps around 360 degrees
        if (start > end) {
            return angle >= start || angle <= end;
        } else {
            return angle >= start && angle <= end;
        }
    }
    
    public void dispose() {
        font.dispose();
        shapeRenderer.dispose();
    }
} 