package io.github.gone.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import io.github.gone.utils.ShapeRendererManager;
import io.github.gone.minigames.ThrowMinigame;
import io.github.gone.fish.Fish;
import io.github.gone.fish.FishLootTable;
import io.github.gone.game.GameManager;
import io.github.gone.game.GameManager.FishingState;
import io.github.gone.ui.FishCaughtScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class FishingRod {
    private final Vector2 position;
    private final float rodLength = 200f;
    private final float rodWidth = 8f;
    // private final float buttonRadius = 40f;
    // private final Vector2 buttonPosition; // New position for the button
    
    // Colors
    private static final Color ROD_HANDLE_COLOR = new Color(0.6f, 0.4f, 0.2f, 1);
    private static final Color ROD_SHAFT_COLOR = new Color(0.8f, 0.6f, 0.3f, 1);
    private static final Color REEL_COLOR = new Color(0.7f, 0.7f, 0.7f, 1);
    private static final Color REEL_DETAIL_COLOR = new Color(0.5f, 0.5f, 0.5f, 1);
    private static final Color LINE_COLOR = Color.WHITE;
    
    // private boolean isFishing;
    private float lineLength;
    private final float maxLineLength = 300f;
    private final float goodBonusLineLength = 50f; // Additional length for "Good" success
    private final float greatBonusLineLength = 100f; // Additional length for "Great" success
    // private boolean isReeling;
    // private ThrowMinigame.SuccessLevel currentSuccessLevel = ThrowMinigame.SuccessLevel.MISS;
    
    // Animation
    private float rodSwayAngle = 0f;
    private float lineSwayFactor = 0f;
    
    private final ShapeRendererManager shapeRenderer;
    // private final ThrowMinigame throwMinigame;
    // private final FishLootTable fishLootTable;
    // private Fish caughtFish;
    
    // // Fish caught screen
    // private final FishCaughtScreen fishCaughtScreen;
    
    // For button text
    private final BitmapFont buttonFont;
    private final GlyphLayout buttonLayout;
    
    // // States for fishing process
    // private enum FishingState {
    //     IDLE,
    //     THROW_MINIGAME,
    //     CASTING,
    //     REELING,
    //     CAUGHT_FISH
    // }
    
    // private FishingState currentState;
    
    public FishingRod(Vector2 position) {
        // Position the rod on the left side
        this.position = new Vector2(position.x * 0.3f, position.y + 50);
        
        
        
        this.lineLength = 0;
        this.shapeRenderer = new ShapeRendererManager();
        
        // Initialize text rendering
        this.buttonFont = new BitmapFont();
        this.buttonFont.setColor(Color.BLACK);
        this.buttonLayout = new GlyphLayout();
    
    }
    
    public void update(float delta, GameManager.FishingState currentState ) {
        // Animate the rod with a gentle sway when idle or fishing
        if (currentState == GameManager.FishingState.IDLE || currentState == GameManager.FishingState.CASTING) {
            rodSwayAngle += delta * 1.5f;
            if (rodSwayAngle > 2 * Math.PI) {
                rodSwayAngle -= 2 * Math.PI;
            }
        }
        
        // Calculate line sway factor for animation
        if (currentState == GameManager.FishingState.CASTING && lineLength > 50) {
            lineSwayFactor = (float) Math.sin(rodSwayAngle * 0.7f) * 15;
        } else {
            lineSwayFactor = 0;
        }
    
        
        if (currentState == GameManager.FishingState.CASTING) {
            // Extend the fishing line
            float maxLength = getMaxReachableLength();
            if (lineLength < maxLength) {
                // Adjust extension speed based on success level
                float extensionSpeed = 130 * delta;
                
                lineLength += extensionSpeed;
                if (lineLength > maxLength) {
                    lineLength = maxLength;
                }
            }
        } else if (currentState == GameManager.FishingState.REELING) {
            // Reel in the line
            lineLength -= 150 * delta;
            if (lineLength <= 0) {
                lineLength = 0;
                // isReeling = false;
                
            }
        }
    }
    
    public void draw(SpriteBatch batch, boolean isFishing, boolean isReeling) {
          
        // End SpriteBatch to use ShapeRenderer
        batch.end();
        
        // Begin a single ShapeRenderer session for filled shapes
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Calculate rod angle based on sway
        float rodAngle = (float) Math.sin(rodSwayAngle) * 5; // -5 to 5 degrees
        
        // Draw fishing rod (a better-looking rod)
        drawImprovedRod(rodAngle);
        
        // Draw fishing line if fishing
        if (isFishing || isReeling) {
            // Calculate rod tip position
            float tipX = position.x + (float) Math.sin(rodAngle * Math.PI / 180) * rodLength;
            float tipY = position.y + (float) Math.cos(rodAngle * Math.PI / 180) * rodLength;
            
            // End filled shapes and begin line shapes for the fishing line
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            
            // Draw line with parabolic curve and get bait position
            Vector2 baitPosition = drawFishingLine(tipX, tipY, isReeling);
            
            // Go back to filled shapes for the bait circle
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            
            // Draw a small circle at the end of the line (the bait) at the exact endpoint of the curv
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.getShapeRenderer().circle(baitPosition.x, baitPosition.y, 7);

        }
        
        // Draw button for casting or reeling
        if (!isFishing) {
            shapeRenderer.setColor(Color.YELLOW);
        } else if (isFishing && lineLength >= getMaxReachableLength() && !isReeling) {
            // Visual indicator for when to reel - make button pulse when ready to reel
            float pulse = (float) (0.7f + 0.3f * Math.sin(System.currentTimeMillis() / 200.0));
            shapeRenderer.setColor(new Color(0.2f, 0.8f, 0.2f * pulse, 1.0f)); // Pulsing green
        } else {
            shapeRenderer.setColor(Color.GRAY);
        }
     
        // End ShapeRenderer session
        shapeRenderer.end();
        
        // Begin SpriteBatch again
        batch.begin();
    
    }
    
    /**
     * Draws an improved fishing rod with handle and reel
     */
    private void drawImprovedRod(float angle) {
        // Save original position
        float originalX = position.x;
        float originalY = position.y;
        
        // Calculate rod endpoint based on angle
        float endX = position.x + (float) Math.sin(angle * Math.PI / 180) * rodLength;
        float endY = position.y + (float) Math.cos(angle * Math.PI / 180) * rodLength;
        
        // Draw rod handle (brown)
        shapeRenderer.setColor(ROD_HANDLE_COLOR);
        
        // Draw handle base
        shapeRenderer.getShapeRenderer().rect(
            position.x - rodWidth * 1.5f, 
            position.y - rodLength * 0.2f, 
            rodWidth * 3, 
            rodLength * 0.2f
        );
        
        // Draw the rod shaft - more realistic with a slight taper
        shapeRenderer.setColor(ROD_SHAFT_COLOR);
        
        // Instead of using polygon which requires different ShapeType, draw as a simple rectangle
        shapeRenderer.getShapeRenderer().rectLine(
            position.x, position.y,
            endX, endY,
            rodWidth
        );
        
        // Draw fishing reel
        shapeRenderer.setColor(REEL_COLOR);
        shapeRenderer.getShapeRenderer().circle(
            position.x + rodWidth * 2, 
            position.y + rodLength * 0.1f, 
            rodWidth * 2
        );
        
        // Draw reel details
        shapeRenderer.setColor(REEL_DETAIL_COLOR);
        shapeRenderer.getShapeRenderer().circle(
            position.x + rodWidth * 2, 
            position.y + rodLength * 0.1f, 
            rodWidth
        );
        
        // Draw rod guides (line holders)
        float guideSpacing = rodLength / 6;
        shapeRenderer.setColor(Color.DARK_GRAY);
        
        for (int i = 1; i <= 5; i++) {
            float ratio = i / 6f;
            float guideX = position.x + (endX - position.x) * ratio;
            float guideY = position.y + (endY - position.y) * ratio;
            float guideSize = rodWidth * (1 - ratio * 0.5f);
            
//            shapeRenderer.getShapeRenderer().circle(guideX, guideY, guideSize);
        }
    }
    
    /**
     * Draws the fishing line with a parabolic curve towards the middle of the screen (sea) when casting
     * and a realistic curve back to the rod when reeling in
     * Returns the coordinates of the bait position for drawing
     */
    private Vector2 drawFishingLine(float startX, float startY, boolean isReeling) {
        shapeRenderer.setColor(LINE_COLOR);
        
        // For a parabolic curve towards the sea (middle of screen)
        int segments = 20;
        float lastX = startX;
        float lastY = startY;
        
        // Calculate target distance based on success level
        float targetDistance = lineLength;
        
        // Adjust target position based on success level for more realistic casting
        float targetX = 240f; // Center of screen horizontally (WORLD_WIDTH/2)
        
        
        float targetY = startY - targetDistance * 0.5f; // Below the rod, towards the water
        
        // Store the bait position (last point of our curve)
        Vector2 baitPosition = new Vector2(lastX, lastY);
        
        // Use different curve based on whether we're casting or reeling
        if (isReeling) {
            // When reeling in, create a more realistic curve that comes up from the water
            // Calculate how far along the reeling process we are (0.0 = just started, 1.0 = almost done)
            float reelingProgress = 1.0f - (lineLength / getMaxReachableLength());
            
            // As we reel, the line becomes more vertical
            float waterX = targetX * (1 - reelingProgress) + startX * reelingProgress;
            float waterY = targetY + 20 + reelingProgress * 40; // Lift from the water as we reel in
            
            for (int i = 1; i <= segments; i++) {
                float t = i / (float) segments;
                
                // Create a curve that changes shape as we reel in
                // At start of reeling: similar to cast curve
                // As we progress: transitions to a more vertical curve
                
                // Control point varies with reeling progress
                float controlX = (startX + waterX) / 2 - 50 * (1 - reelingProgress);
                float controlY = startY - 100 * (1 - reelingProgress);
                
                // Quadratic Bezier formula
                float segmentX = (1-t)*(1-t)*startX + 2*(1-t)*t*controlX + t*t*waterX;
                float segmentY = (1-t)*(1-t)*startY + 2*(1-t)*t*controlY + t*t*waterY;
                
                // Add some gentle sway based on lineSwayFactor and reeling
                float swayAmount = lineSwayFactor * (float) Math.sin(t * Math.PI) * 0.3f;
                swayAmount *= (1 - reelingProgress); // Less sway as we reel in
                segmentX += swayAmount;
                
                shapeRenderer.getShapeRenderer().line(lastX, lastY, segmentX, segmentY);
                lastX = segmentX;
                lastY = segmentY;
                
                // If this is the last segment, store the end position for the bait
                if (i == segments) {
                    baitPosition.set(segmentX, segmentY);
                }
            }
        } else {
            // Regular casting curve
            for (int i = 1; i <= segments; i++) {
                float t = i / (float) segments;
                
                // Parametric equation for a parabola from start to target
                // Quadratic Bezier curve: B(t) = (1-t)^2*P0 + 2(1-t)t*P1 + t^2*P2
                // Where P0 is start, P2 is end, and P1 is the control point
                
                // Control point - create a nice arc
                float controlX = (startX + targetX) / 2;
                float controlY = startY - targetDistance * 0.3f; // Control point below start point for a nice arc
                
                // Quadratic Bezier formula
                float segmentX = (1-t)*(1-t)*startX + 2*(1-t)*t*controlX + t*t*targetX;
                float segmentY = (1-t)*(1-t)*startY + 2*(1-t)*t*controlY + t*t*targetY;
                
                // Add some gentle sway based on lineSwayFactor
                segmentX += lineSwayFactor * (float) Math.sin(t * Math.PI) * 0.3f;
                
                shapeRenderer.getShapeRenderer().line(lastX, lastY, segmentX, segmentY);
                lastX = segmentX;
                lastY = segmentY;
                
                // If this is the last segment, store the end position for the bait
                if (i == segments) {
                    baitPosition.set(segmentX, segmentY);
                }
            }
        }
        
        return baitPosition;
    }
    
    
    public float getLineLength() {
        return lineLength;
    }
    
    public float getMaxLineLength() {
        return maxLineLength;
    }
    
    public float getMaxReachableLength() {
        return maxLineLength;
    }
    
} 