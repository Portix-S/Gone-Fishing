package io.github.gone.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import io.github.gone.utils.ShapeRendererManager;
import io.github.gone.minigames.MinigameManager;
import io.github.gone.minigames.ThrowMinigame;
import io.github.gone.fish.Fish;
import io.github.gone.fish.FishLootTable;
import io.github.gone.ui.FishCaughtScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class FishingRod {
    private final Vector2 position;
    private final float rodLength = 200f;
    private final float rodWidth = 8f;
    private final float buttonRadius = 40f;
    private final Vector2 buttonPosition; // New position for the button
    
    // Colors
    private static final Color ROD_HANDLE_COLOR = new Color(0.6f, 0.4f, 0.2f, 1);
    private static final Color ROD_SHAFT_COLOR = new Color(0.8f, 0.6f, 0.3f, 1);
    private static final Color REEL_COLOR = new Color(0.7f, 0.7f, 0.7f, 1);
    private static final Color REEL_DETAIL_COLOR = new Color(0.5f, 0.5f, 0.5f, 1);
    private static final Color BUTTON_COLOR = new Color(1f, 0.8f, 0.2f, 1f);
    private static final Color LINE_COLOR = Color.WHITE;
    
    private boolean isFishing;
    private float lineLength;
    private final float maxLineLength = 300f;
    private final float goodBonusLineLength = 50f; // Additional length for "Good" success
    private final float greatBonusLineLength = 100f; // Additional length for "Great" success
    private boolean isReeling;
    
    // Animation
    private float rodSwayAngle = 0f;
    private float lineSwayFactor = 0f;
    
    private final ShapeRendererManager shapeRenderer;
    private Fish caughtFish;
    private ThrowMinigame.SuccessLevel currentMinigameSuccessLevel; // New field to store the success level
    
    // Fish caught screen
    private final FishCaughtScreen fishCaughtScreen;
    
    // For button text
    private final BitmapFont buttonFont;
    private final GlyphLayout buttonLayout;
    
    // States for fishing process
    public enum FishingState {
        IDLE,
        THROW_MINIGAME,
        CASTING,
        CATCH_MINIGAME,
        CAUGHT_FISH
    }
    
    private FishingState currentState;
    
    public FishingRod(Vector2 position) {
        // Position the rod on the left side
        this.position = new Vector2(position.x * 0.3f, position.y + 50);
        
        // Position the button at the bottom center
        this.buttonPosition = new Vector2(position.x, position.y - 100);
        
        this.isFishing = false;
        this.lineLength = 0;
        this.isReeling = false;
        this.shapeRenderer = new ShapeRendererManager();
        this.currentState = FishingState.IDLE;
        this.fishCaughtScreen = new FishCaughtScreen();
        
        // Initialize text rendering
        this.buttonFont = new BitmapFont();
        this.buttonFont.setColor(Color.BLACK);
        this.buttonLayout = new GlyphLayout();
        
        // Create throw minigame at the center of the screen
        // this.minigameManager = new MinigameManager(position.x, position.y + 150); // REMOVE
        // this.minigameManager.getThrowMinigame().setListener(new ThrowMinigame.ThrowMinigameListener() { // REMOVE
        //     @Override // REMOVE
        //     public void onThrowComplete(ThrowMinigame.SuccessLevel successLevel) { // REMOVE
        //         onThrowMinigameFinished(successLevel); // REMOVE
        //     } // REMOVE
        // }); // REMOVE
    }
    
    /**
     * Called when the throw minigame finishes
     */
    private void onThrowMinigameFinished(ThrowMinigame.SuccessLevel successLevel) {
        // Start fishing with appropriate bonus based on success level
        currentState = FishingState.CASTING;
        isFishing = true;
        isReeling = false;
        lineLength = 0;
        this.currentMinigameSuccessLevel = successLevel; // Store the success level
        
        // Determine what fish will be caught using the loot table
        // REMOVE: caughtFish = fishLootTable.determineFish(successLevel);
        
        // Log fishing start information
        Gdx.app.log("FishingRod", "===============================================");
        Gdx.app.log("FishingRod", "FISHING STARTED");
        Gdx.app.log("FishingRod", "Success Level: " + successLevel);
        Gdx.app.log("FishingRod", "Max Reachable Length: " + getMaxReachableLength());
        Gdx.app.log("FishingRod", "Base Max Length: " + maxLineLength);
        if (caughtFish != null) {
            Gdx.app.log("FishingRod", "Potential Fish: " + caughtFish.getName());
            Gdx.app.log("FishingRod", "Fish Rarity: " + caughtFish.getRarity());
            Gdx.app.log("FishingRod", "Fish Weight: " + caughtFish.getWeight());
        } else {
            Gdx.app.log("FishingRod", "No fish determined (null)");
        }
        Gdx.app.log("FishingRod", "===============================================");
    }
    
    public void update(float delta) {
        // Animate the rod with a gentle sway when idle or fishing
        if (currentState == FishingState.IDLE || currentState == FishingState.CASTING) {
            rodSwayAngle += delta * 1.5f;
            if (rodSwayAngle > 2 * Math.PI) {
                rodSwayAngle -= (float) (2 * Math.PI);
            }
        }
        
        // Calculate line sway factor for animation
        if (isFishing && lineLength > 50) {
            lineSwayFactor = (float) Math.sin(rodSwayAngle * 0.7f) * 15;
        } else {
            lineSwayFactor = 0;
        }
        
        // Update throw minigame if active
        // minigameManager.update(delta); // REMOVE
        
        // Update fish caught screen if active
        fishCaughtScreen.update(delta);
        
        // If fish caught screen is active, don't update anything else
        if (fishCaughtScreen.isActive()) {
            return;
        }
        
        if (currentState == FishingState.THROW_MINIGAME) {
            // Wait for throw minigame to complete
            return;
        }
        
        if (isFishing && !isReeling) {
            // Extend the fishing line
            float maxLength = getMaxReachableLength();
            if (lineLength < maxLength) {
                // Adjust extension speed based on success level
                float extensionSpeed = switch (this.currentMinigameSuccessLevel) { // Use the stored success level
                    case GREAT -> 130 * delta; // Fastest for "Great"
                    case GOOD -> 115 * delta; // Medium for "Good"
                    default -> 100 * delta; // Base speed for "Miss"
                };

                lineLength += extensionSpeed;
                if (lineLength > maxLength) {
                    lineLength = maxLength;
                }
            }
        } else if (isReeling) {
            // Reel in the line
            lineLength -= 150 * delta;
            if (lineLength <= 0) {
                lineLength = 0;
                isReeling = false;
                
                // Log reeling completion
                Gdx.app.log("FishingRod", "===============================================");
                Gdx.app.log("FishingRod", "REELING COMPLETED");
                Gdx.app.log("FishingRod", "Success Level: " + this.currentMinigameSuccessLevel); // Use the stored success level
                if (caughtFish != null) {
                    Gdx.app.log("FishingRod", "Fish: " + caughtFish.getName());
                    Gdx.app.log("FishingRod", "Fish Type: " + caughtFish.getClass().getSimpleName());
                    Gdx.app.log("FishingRod", "Fish Rarity: " + caughtFish.getRarity());
                    Gdx.app.log("FishingRod", "Fish Weight: " + caughtFish.getWeight());
                } else {
                    Gdx.app.log("FishingRod", "No fish caught (null)");
                }
                Gdx.app.log("FishingRod", "===============================================");
                
                // If we have a fish (always true if determineFish return is not null)
                if (caughtFish != null) {
                    currentState = FishingState.CAUGHT_FISH;
                    
                    // Show the fish caught screen with a callback for when it's closed
                    fishCaughtScreen.show(caughtFish, new FishCaughtScreen.Callback() {
                        @Override
                        public void onClose() {
                            // Reset to idle state after fish is caught and screen is closed
                            currentState = FishingState.IDLE;
                            isFishing = false;
                            caughtFish = null;
                        }
                    });
                } else {
                    // Reset if no fish was caught (should not happen normally)
                    isFishing = false;
                    currentState = FishingState.IDLE;
                    caughtFish = null;
                }
            }
        }
    }
    
    public void draw(SpriteBatch batch) {
        // If fish caught screen is active, draw it and nothing else
        if (fishCaughtScreen.isActive()) {
            fishCaughtScreen.draw(batch);
            return;
        }
        
        // If throw minigame is active, draw it and return
        // if (currentState == FishingState.THROW_MINIGAME) { // REMOVE
        //     minigameManager.draw(batch); // REMOVE
        //     return; // REMOVE
        // } // REMOVE
        
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
            Vector2 baitPosition = drawFishingLine(tipX, tipY);
            
            // Go back to filled shapes for the bait circle
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            
            // Draw a small circle at the end of the line (the bait) at the exact endpoint of the curve
            // Change color and size based on success level
            // ThrowMinigame.SuccessLevel successLevel = minigameManager.getThrowMinigame() != null ? minigameManager.getThrowMinigame().getSuccessLevel() : ThrowMinigame.SuccessLevel.MISS; // REMOVE
            if (this.currentMinigameSuccessLevel == ThrowMinigame.SuccessLevel.GREAT) {
                shapeRenderer.setColor(Color.GREEN);
                shapeRenderer.getShapeRenderer().circle(baitPosition.x, baitPosition.y, 7);
            } else if (this.currentMinigameSuccessLevel == ThrowMinigame.SuccessLevel.GOOD) {
                shapeRenderer.setColor(Color.YELLOW);
                shapeRenderer.getShapeRenderer().circle(baitPosition.x, baitPosition.y, 6);
            } else {
                shapeRenderer.setColor(Color.RED);
                shapeRenderer.getShapeRenderer().circle(baitPosition.x, baitPosition.y, 5);
            }
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
        shapeRenderer.getShapeRenderer().circle(buttonPosition.x, buttonPosition.y, buttonRadius);
        
        // Draw button decoration
        if (!isFishing) {
            shapeRenderer.setColor(BUTTON_COLOR);
            shapeRenderer.getShapeRenderer().circle(buttonPosition.x, buttonPosition.y, buttonRadius * 0.8f);
            
            // Draw fishing icon on button
            shapeRenderer.setColor(Color.BLACK);
            // Draw a simple fishing hook icon
            shapeRenderer.getShapeRenderer().arc(buttonPosition.x, buttonPosition.y, buttonRadius * 0.4f, 180, 180);
            shapeRenderer.getShapeRenderer().rect(buttonPosition.x - 2, buttonPosition.y, 4, buttonRadius * 0.4f);
        } else if (isFishing && lineLength >= getMaxReachableLength() && !isReeling) {
            // Ready to reel - draw a visual indicator
            shapeRenderer.setColor(new Color(0.1f, 0.9f, 0.1f, 1.0f));
            shapeRenderer.getShapeRenderer().circle(buttonPosition.x, buttonPosition.y, buttonRadius * 0.8f);
            
            // Draw reel icon
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.getShapeRenderer().circle(buttonPosition.x, buttonPosition.y, buttonRadius * 0.5f, 12);
            shapeRenderer.setColor(new Color(0.1f, 0.9f, 0.1f, 1.0f));
            shapeRenderer.getShapeRenderer().circle(buttonPosition.x, buttonPosition.y, buttonRadius * 0.3f);
        }
        
        // End ShapeRenderer session
        shapeRenderer.end();
        
        // Begin SpriteBatch again
        batch.begin();
        
        // Draw button text
        if (!isFishing) {
            buttonLayout.setText(buttonFont, "CAST");
            buttonFont.draw(batch, "CAST", 
                buttonPosition.x - buttonLayout.width / 2, 
                buttonPosition.y - buttonRadius - 10);
        } else if (isFishing && lineLength >= getMaxReachableLength() && !isReeling) {
            // Show "REEL!" text when ready to reel
            buttonFont.setColor(Color.GREEN);
            buttonLayout.setText(buttonFont, "REEL!");
            buttonFont.draw(batch, "REEL!", 
                buttonPosition.x - buttonLayout.width / 2, 
                buttonPosition.y - buttonRadius - 10);
            buttonFont.setColor(Color.BLACK); // Reset color
        }
        
        // Draw throw minigame result message if needed
        // if (minigameManager.isMinigameActive() && minigameManager.getThrowMinigame().isShowingResult()) { // REMOVE
        //     minigameManager.getThrowMinigame().draw(batch); // REMOVE
        // } // REMOVE
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
            
            shapeRenderer.getShapeRenderer().circle(guideX, guideY, guideSize);
        }
    }
    
    /**
     * Draws the fishing line with a parabolic curve towards the middle of the screen (sea) when casting
     * and a realistic curve back to the rod when reeling in
     * Returns the coordinates of the bait position for drawing
     */
    private Vector2 drawFishingLine(float startX, float startY) {
        shapeRenderer.setColor(LINE_COLOR);
        
        // For a parabolic curve towards the sea (middle of screen)
        int segments = 20;
        float lastX = startX;
        float lastY = startY;
        
        // Calculate target distance based on success level
        float targetDistance = lineLength;
        
        // Adjust target position based on success level for more realistic casting
        float targetX = switch (this.currentMinigameSuccessLevel) { // Use the stored success level
            case GREAT -> 280f; // Farther right for great casts
            case GOOD -> 260f; // Moderate distance for good casts
            default -> 220f; // Shorter for misses
        }; // Center of screen horizontally (WORLD_WIDTH/2)
        
        // Better casts (GOOD, GREAT) go farther horizontally

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
    
    public boolean isPointInCastButton(float x, float y) {
        float distance = Vector2.dst(buttonPosition.x, buttonPosition.y, x, y);
        return distance <= buttonRadius;
    }
    
    public void startFishing() {
        if (currentState == FishingState.IDLE) {
            currentState = FishingState.THROW_MINIGAME; // Simply set the state for GameManager to pick up
            // No longer initiating minigame directly from here.
        }
    }
    
    public void startReeling() {
        if (isFishing && !isReeling && currentState == FishingState.CASTING) {
            isReeling = true;
        }
    }
    
    public boolean isFishing() {
        return isFishing;
    }
    
    public boolean isReeling() {
        return isReeling;
    }
    
    public boolean isInThrowMinigame() {
        return currentState == FishingState.THROW_MINIGAME;
    }

    public boolean isInCatchMinigame()
    {
        return currentState == FishingState.CATCH_MINIGAME;
    }

    public boolean isShowingFishCaught() {
        return currentState == FishingState.CAUGHT_FISH && fishCaughtScreen.isActive();
    }
    
    public float getLineLength() {
        return lineLength;
    }
    
    public float getMaxLineLength() {
        return maxLineLength;
    }
    
    public float getMaxReachableLength() {
        // This still uses currentSuccessLevel, which is gone.
        // We need to pass the success level to the FishLootTable or store it temporarily.
        // For now, let's keep it simple and assume a default if not set.
        // TODO: Revisit this to get the success level from the MinigameManager or the FishLootTable.
        // ThrowMinigame.SuccessLevel successLevel = minigameManager.getThrowMinigame() != null ? minigameManager.getThrowMinigame().getSuccessLevel() : ThrowMinigame.SuccessLevel.MISS; // REMOVE
        
        // The success level will be provided by GameManager. For now, use MISS as a placeholder.
        // ThrowMinigame.SuccessLevel successLevel = ThrowMinigame.SuccessLevel.MISS; // TEMPORARY ADJUSTMENT - REMOVE
        ThrowMinigame.SuccessLevel successLevel = this.currentMinigameSuccessLevel != null ? this.currentMinigameSuccessLevel : ThrowMinigame.SuccessLevel.MISS; // Use stored success level
        
        switch (successLevel) {
            case GREAT:
                return maxLineLength + greatBonusLineLength;
            case GOOD:
                return maxLineLength + goodBonusLineLength;
            default:
                return maxLineLength;
        }
    }
    
    public Fish getCaughtFish() {
        return caughtFish;
    }
    
    public void setCaughtFish(Fish fish) {
        this.caughtFish = fish;
    }
    
    public void dispose() {
        shapeRenderer.dispose();
        buttonFont.dispose();
        // minigameManager.dispose(); // REMOVE
        fishCaughtScreen.dispose();
    }
    
    public void onMinigameCompletion(ThrowMinigame.SuccessLevel successLevel) {
        this.onThrowMinigameFinished(successLevel);
    }
    
    public FishingState getCurrentState() {
        return currentState;
    }
    
    public FishCaughtScreen getFishCaughtScreen() {
        return fishCaughtScreen;
    }
} 