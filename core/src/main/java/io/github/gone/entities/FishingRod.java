package io.github.gone.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import io.github.gone.utils.ShapeRendererManager;
import io.github.gone.minigames.SkillCheckMinigame;

public class FishingRod {
    private final Vector2 position;
    private final float rodLength = 150f;
    private final float rodWidth = 10f;
    private final float buttonRadius = 40f;
    
    private boolean isFishing;
    private float lineLength;
    private final float maxLineLength = 300f;
    private final float goodBonusLineLength = 50f; // Additional length for "Good" success
    private final float greatBonusLineLength = 100f; // Additional length for "Great" success
    private boolean isReeling;
    private SkillCheckMinigame.SuccessLevel currentSuccessLevel = SkillCheckMinigame.SuccessLevel.MISS;
    
    private final ShapeRendererManager shapeRenderer;
    private final SkillCheckMinigame skillCheck;
    
    // States for fishing process
    private enum FishingState {
        IDLE,
        SKILL_CHECK,
        CASTING,
        REELING
    }
    
    private FishingState currentState;
    
    public FishingRod(Vector2 position) {
        this.position = position;
        this.isFishing = false;
        this.lineLength = 0;
        this.isReeling = false;
        this.shapeRenderer = new ShapeRendererManager();
        this.currentState = FishingState.IDLE;
        
        // Create skill check minigame at the center of the screen
        this.skillCheck = new SkillCheckMinigame(position.x, position.y + 150);
        this.skillCheck.setListener(new SkillCheckMinigame.SkillCheckListener() {
            @Override
            public void onSkillCheckComplete(SkillCheckMinigame.SuccessLevel successLevel) {
                onSkillCheckFinished(successLevel);
            }
        });
    }
    
    /**
     * Called when the skill check minigame finishes
     */
    private void onSkillCheckFinished(SkillCheckMinigame.SuccessLevel successLevel) {
        // Start fishing with appropriate bonus based on success level
        currentState = FishingState.CASTING;
        isFishing = true;
        isReeling = false;
        lineLength = 0;
        currentSuccessLevel = successLevel;
    }
    
    public void update(float delta) {
        // Update skill check minigame if active
        skillCheck.update(delta);
        
        if (currentState == FishingState.SKILL_CHECK) {
            // Wait for skill check to complete
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
                isFishing = false;
                currentSuccessLevel = SkillCheckMinigame.SuccessLevel.MISS;
                currentState = FishingState.IDLE;
            }
        }
    }
    
    public void draw(SpriteBatch batch) {
        // If skill check is active, draw it and return
        if (currentState == FishingState.SKILL_CHECK) {
            skillCheck.draw(batch);
            return;
        }
        
        // End SpriteBatch to use ShapeRenderer
        batch.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw fishing rod (a rectangle)
        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.getShapeRenderer().rect(position.x - rodWidth / 2, position.y, rodWidth, rodLength);
        
        // Draw fishing line if fishing
        if (isFishing || isReeling) {
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.getShapeRenderer().line(position.x, position.y + rodLength, 
                    position.x, position.y + rodLength + lineLength);
            
            // Draw a small circle at the end of the line (the bait)
            // Change color and size based on success level
            if (currentSuccessLevel == SkillCheckMinigame.SuccessLevel.GREAT) {
                shapeRenderer.setColor(Color.GREEN);
                shapeRenderer.getShapeRenderer().circle(position.x, position.y + rodLength + lineLength, 7);
            } else if (currentSuccessLevel == SkillCheckMinigame.SuccessLevel.GOOD) {
                shapeRenderer.setColor(Color.YELLOW);
                shapeRenderer.getShapeRenderer().circle(position.x, position.y + rodLength + lineLength, 6);
            } else {
                shapeRenderer.setColor(Color.RED);
                shapeRenderer.getShapeRenderer().circle(position.x, position.y + rodLength + lineLength, 5);
            }
        }
        
        // Draw button for casting
        if (!isFishing) {
            shapeRenderer.setColor(Color.YELLOW);
        } else {
            shapeRenderer.setColor(Color.GRAY);
        }
        shapeRenderer.getShapeRenderer().circle(position.x, position.y - buttonRadius - 10, buttonRadius);
        
        shapeRenderer.end();
        
        // Begin SpriteBatch again
        batch.begin();
        
        // Draw skill check result message if needed
        if (skillCheck.isShowingResult()) {
            skillCheck.draw(batch);
        }
    }
    
    public boolean isPointInCastButton(float x, float y) {
        float distance = Vector2.dst(position.x, position.y - buttonRadius - 10, x, y);
        return distance <= buttonRadius;
    }
    
    public void startFishing() {
        if (!isFishing && currentState == FishingState.IDLE) {
            currentState = FishingState.SKILL_CHECK;
            skillCheck.start();
        }
    }
    
    public void handleClick(float x, float y) {
        if (currentState == FishingState.SKILL_CHECK) {
            skillCheck.onClick();
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
        }
    }
    
    public boolean isFishing() {
        return isFishing;
    }
    
    public boolean isReeling() {
        return isReeling;
    }
    
    public boolean isInSkillCheck() {
        return currentState == FishingState.SKILL_CHECK;
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
    
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (skillCheck != null) {
            skillCheck.dispose();
        }
    }
} 