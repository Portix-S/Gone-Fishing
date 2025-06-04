package io.github.gone.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import io.github.gone.entities.Player;
import io.github.gone.fish.Fish;
import io.github.gone.fish.FishLootTable;
import io.github.gone.minigames.ThrowMinigame;
import io.github.gone.states.GameState;
import io.github.gone.ui.FishCaughtScreen;
import io.github.gone.utils.ShapeRendererManager;

public class GameManager {

    private Player player;
    private GameState gameState;


    private final float buttonRadius = 40f;
    private final Vector2 buttonPosition; // New position for the button
    private boolean isFishing;
    private boolean isReeling;

    private final ShapeRendererManager shapeRenderer;
    private final ThrowMinigame throwMinigame;
    private Fish caughtFish;

    private static final Color BUTTON_COLOR = new Color(1f, 0.8f, 0.2f, 1f);

    
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
        REELING,
        CAUGHT_FISH
    }
    
    private FishingState currentState;


    public  GameManager(Player player, GameState gameState, Vector2 position){
       
        // Position the rod on the left side
        // Position the button at the bottom center
        this.buttonPosition = new Vector2(position.x, position.y - 100);

        this.isFishing = false;
        this.isReeling = false;
        this.shapeRenderer = new ShapeRendererManager();
        this.currentState = FishingState.IDLE;
        this.fishCaughtScreen = new FishCaughtScreen();

        // Initialize text rendering
        this.buttonFont = new BitmapFont();
        this.buttonFont.setColor(Color.BLACK);
        this.buttonLayout = new GlyphLayout();
        
        // Create throw minigame at the center of the screen
        this.throwMinigame = new ThrowMinigame(position.x, position.y + 150);
        this.throwMinigame.setListener(new ThrowMinigame.ThrowMinigameListener() {
            @Override
            public void onThrowComplete(Fish fish) {
                onThrowMinigameFinished(fish);
            }
        });
    }

    void Init(){

    }

    public void update(float delta) {
        
        //TODO chamar as coisas do fishing rod

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
            
            }
        else if (isReeling) {
            // Reel in the line
            // lineLength -= 150 * delta;
            //TODO temos que fazer o role do catch minigame agui

            if (currentState == FishingState.CAUGHT_FISH) {
                
                isReeling = false;
                
               
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
                    isFishing = false;
                    currentState = FishingState.IDLE;
                    caughtFish = null;
                }

            }


        }

        player.currentRod.update(delta, currentState);
    }

    void UpdateState(){



    }

    private void onThrowMinigameFinished(Fish fish){
        
        currentState = FishingState.CASTING;
        isFishing = true;
        isReeling = false;
        
        // Determine what fish will be caught using the loot table
        caughtFish = fish;

        // Log fishing start information
        Gdx.app.log("FishingRod", "===============================================");
        Gdx.app.log("FishingRod", "FISHING STARTED");
        // // Gdx.app.log("FishingRod", "Success Level: " + successLevel);
        // Gdx.app.log("FishingRod", "Max Reachable Length: " + getMaxReachableLength());
        // Gdx.app.log("FishingRod", "Base Max Length: " + maxLineLength);
        if (caughtFish != null) {
            Gdx.app.log("FishingRod", "Potential Fish: " + caughtFish.getName());
            Gdx.app.log("FishingRod", "Fish Rarity: " + caughtFish.getRarity());
            Gdx.app.log("FishingRod", "Fish Weight: " + caughtFish.getWeight());
        } else {
            Gdx.app.log("FishingRod", "No fish determined (null)");
        }
        Gdx.app.log("FishingRod", "===============================================");
    
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
        } else if (currentState == FishingState.CASTING && !isReeling) {
            startReeling();
        } else if (isPointInCastButton(x, y) && currentState == FishingState.IDLE) {
            startFishing();
        }
    }
    
    public void startReeling() {
        if (isFishing) {
            isReeling = true;
            currentState = FishingState.REELING;
            
            // Log reeling start
            Gdx.app.log("FishingRod", "===============================================");
            Gdx.app.log("FishingRod", "REELING STARTED");
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

    public void draw(SpriteBatch batch) {

        player.currentRod.draw(batch, isFishing, isReeling);

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
        
        
        // Draw button for casting or reeling
        if (!isFishing) {
            shapeRenderer.setColor(Color.YELLOW);
        } else if (isFishing && !isReeling) {
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
        } else if (isFishing && !isReeling) {
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
        } else if (isFishing && !isReeling) {
            // Show "REEL!" text when ready to reel
            buttonFont.setColor(Color.GREEN);
            buttonLayout.setText(buttonFont, "REEL!");
            buttonFont.draw(batch, "REEL!", 
                buttonPosition.x - buttonLayout.width / 2, 
                buttonPosition.y - buttonRadius - 10);
            buttonFont.setColor(Color.BLACK); // Reset color
        }
        
        // Draw throw minigame result message if needed
        if (throwMinigame.isShowingResult()) {
            throwMinigame.draw(batch);
        }
    }

}
