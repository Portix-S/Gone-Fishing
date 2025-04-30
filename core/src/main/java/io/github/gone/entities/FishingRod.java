package io.github.gone.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import io.github.gone.utils.ShapeRendererManager;
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
    private ThrowMinigame.SuccessLevel currentSuccessLevel = ThrowMinigame.SuccessLevel.MISS;
    
    // Animation
    private float rodSwayAngle = 0f;
    private float lineSwayFactor = 0f;
    
    private final ShapeRendererManager shapeRenderer;
    private final ThrowMinigame throwMinigame;
    private final FishLootTable fishLootTable;
    private Fish caughtFish;
    
    // Fish caught screen
    private final FishCaughtScreen fishCaughtScreen;
    
    // For button text
    private final BitmapFont buttonFont;
    private final GlyphLayout buttonLayout;
    
    // States for fishing process
    private enum FishingState {
        IDLE,
        THROW_MINIGAME,
        CASTING,
        REELING,
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
        this.fishLootTable = new FishLootTable();
        this.fishCaughtScreen = new FishCaughtScreen();
        
        // Initialize text rendering
        this.buttonFont = new BitmapFont();
        this.buttonFont.setColor(Color.BLACK);
        this.buttonLayout = new GlyphLayout();
        
        // Create throw minigame at the center of the screen
        this.throwMinigame = new ThrowMinigame(position.x, position.y + 150);
        this.throwMinigame.setListener(new ThrowMinigame.ThrowMinigameListener() {
            @Override
            public void onThrowComplete(ThrowMinigame.SuccessLevel successLevel) {
                onThrowMinigameFinished(successLevel);
            }
        });
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
        currentSuccessLevel = successLevel;
        
        // Determine what fish will be caught using the loot table
        caughtFish = fishLootTable.determineFish(successLevel);
        
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
                rodSwayAngle -= 2 * Math.PI;
            }
        }
        
        // Calculate line sway factor for animation
        if (isFishing && lineLength > 50) {
            lineSwayFactor = (float) Math.sin(rodSwayAngle * 0.7f) * 15;
        } else {
            lineSwayFactor = 0;
        }
        
        // Update throw minigame if active
        throwMinigame.update(delta);
        
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
                float extensionSpeed;
                switch (currentSuccessLevel) {
                    case GREAT:
                        extensionSpeed = 130 * delta; // Fastest for "Great"
                        break;
                    case GOOD:
                        extensionSpeed = 115 * delta; // Medium for "Good"
                        break;
                    default:
                        extensionSpeed = 100 * delta; // Base speed for "Miss"
                        break;
                }
                
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
                Gdx.app.log("FishingRod", "Success Level: " + currentSuccessLevel);
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
                            currentSuccessLevel = ThrowMinigame.SuccessLevel.MISS;
                            caughtFish = null;
                        }
                    });
                } else {
                    // Reset if no fish was caught (should not happen normally)
                    isFishing = false;
                    currentSuccessLevel = ThrowMinigame.SuccessLevel.MISS;
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
        if (currentState == FishingState.THROW_MINIGAME) {
            throwMinigame.draw(batch);
            return;
        }
        
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
            if (currentSuccessLevel == ThrowMinigame.SuccessLevel.GREAT) {
                shapeRenderer.setColor(Color.GREEN);
                shapeRenderer.getShapeRenderer().circle(baitPosition.x, baitPosition.y, 7);
            } else if (currentSuccessLevel == ThrowMinigame.SuccessLevel.GOOD) {
                shapeRenderer.setColor(Color.YELLOW);
                shapeRenderer.getShapeRenderer().circle(baitPosition.x, baitPosition.y, 6);
            } else {
                shapeRenderer.setColor(Color.RED);
                shapeRenderer.getShapeRenderer().circle(baitPosition.x, baitPosition.y, 5);
            }
        }
        
        // Draw button for casting
        if (!isFishing) {
            shapeRenderer.setColor(Color.YELLOW);
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
        }
        
        // End ShapeRenderer session
        shapeRenderer.end();
        
        // Begin SpriteBatch again
        batch.begin();
        
        // Draw "CAST" text on button when not fishing
        if (!isFishing) {
            buttonLayout.setText(buttonFont, "CAST");
            buttonFont.draw(batch, "CAST", 
                buttonPosition.x - buttonLayout.width / 2, 
                buttonPosition.y - buttonRadius - 10);
        }
        
        // Draw throw minigame result message if needed
        if (throwMinigame.isShowingResult()) {
            throwMinigame.draw(batch);
        }
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
     * Draws the fishing line with a parabolic curve towards the middle of the screen (sea)
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
        float targetX = 240f; // Center of screen horizontally (WORLD_WIDTH/2)
        
        // Better casts (GOOD, GREAT) go farther horizontally
        switch (currentSuccessLevel) {
            case GREAT:
                targetX = 280f; // Farther right for great casts
                break;
            case GOOD:
                targetX = 260f; // Moderate distance for good casts
                break;
            default:
                targetX = 220f; // Shorter for misses
                break;
        }
        
        float targetY = startY - targetDistance * 0.5f; // Below the rod, towards the water
        
        // Store the bait position (last point of our curve)
        Vector2 baitPosition = new Vector2(lastX, lastY);
        
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
        
        return baitPosition;
    }
    
    public boolean isPointInCastButton(float x, float y) {
        float distance = Vector2.dst(buttonPosition.x, buttonPosition.y, x, y);
        return distance <= buttonRadius;
    }
    
    public void startFishing() {
        if (!isFishing && currentState == FishingState.IDLE) {
            currentState = FishingState.THROW_MINIGAME;
            throwMinigame.start();
        }
    }
    
    public void handleClick(float x, float y) {
        // If fish caught screen is active, let it handle the click
        if (fishCaughtScreen.isActive()) {
            fishCaughtScreen.handleClick(x, y);
            return;
        }
        
        if (currentState == FishingState.THROW_MINIGAME) {
            throwMinigame.onClick();
        } else if (currentState == FishingState.CASTING && lineLength >= getMaxReachableLength() && !isReeling) {
            startReeling();
        } else if (isPointInCastButton(x, y) && currentState == FishingState.IDLE) {
            startFishing();
        }
    }
    
    public void startReeling() {
        if (isFishing && lineLength >= getMaxReachableLength()) {
            isReeling = true;
            currentState = FishingState.REELING;
            
            // Log reeling start
            Gdx.app.log("FishingRod", "===============================================");
            Gdx.app.log("FishingRod", "REELING STARTED");
            Gdx.app.log("FishingRod", "Line Length: " + lineLength);
            Gdx.app.log("FishingRod", "Max Reachable Length: " + getMaxReachableLength());
            Gdx.app.log("FishingRod", "Success Level: " + currentSuccessLevel);
            if (caughtFish != null) {
                Gdx.app.log("FishingRod", "Potential Fish: " + caughtFish.getName());
            }
            Gdx.app.log("FishingRod", "===============================================");
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
    
    public boolean isShowingFishCaught() {
        return fishCaughtScreen.isActive();
    }
    
    public float getLineLength() {
        return lineLength;
    }
    
    public float getMaxLineLength() {
        return maxLineLength;
    }
    
    public float getMaxReachableLength() {
        // Return different maximum length based on success level
        switch (currentSuccessLevel) {
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
    
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (throwMinigame != null) {
            throwMinigame.dispose();
        }
        if (fishCaughtScreen != null) {
            fishCaughtScreen.dispose();
        }
        if (buttonFont != null) {
            buttonFont.dispose();
        }
        if (caughtFish != null) {
            if (caughtFish instanceof io.github.gone.fish.CommonFish) {
                ((io.github.gone.fish.CommonFish) caughtFish).dispose();
            } else if (caughtFish instanceof io.github.gone.fish.RareFish) {
                ((io.github.gone.fish.RareFish) caughtFish).dispose();
            } else if (caughtFish instanceof io.github.gone.fish.LegendaryFish) {
                ((io.github.gone.fish.LegendaryFish) caughtFish).dispose();
            }
        }
    }
} 