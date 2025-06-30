package io.github.gone.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.gone.entities.Player;
import io.github.gone.minigames.MinigameManager;
import io.github.gone.minigames.ThrowMinigame;
import io.github.gone.states.GameState;

public class GameManager implements ThrowMinigame.ThrowMinigameListener {
    private Player player;
    private GameState currentGameState;
    private MinigameManager minigameManager;
    private float centerX; // To be passed to MinigameManager
    private float centerY; // To be passed to MinigameManager

    public GameManager(float centerX, float centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
    }

    public void init() {
        // Initialize player, minigameManager, etc.
        this.player = new Player(centerX, centerY);
        this.minigameManager = new MinigameManager(centerX, centerY + 150);
        this.minigameManager.getThrowMinigame().setListener(this);
        // Set initial game state if needed
        // currentGameState = GameState.SOME_INITIAL_STATE;
    }

    public void update(float delta) {
        // Update based on current game state
        if (currentGameState != null) {
//            currentGameState.update(delta);
        }

        // Update minigame manager if a minigame is active
        if (minigameManager.isMinigameActive()) {
            minigameManager.update(delta);
        } else {
            player.getFishingRod().update(delta);
        }
    }

    public void updateState(GameState newState) {
        this.currentGameState = newState;
        // Potentially setup minigame based on new state
        minigameManager.setupMinigame(newState);
    }

    public void draw(SpriteBatch batch) {
        // Draw based on current game state
        if (currentGameState != null) {
//            currentGameState.draw(batch);
        }
        if (minigameManager.isMinigameActive() || player.getFishingRod().isInThrowMinigame()) {
            minigameManager.draw(batch);
        } else {
            player.getFishingRod().draw(batch);
        }
    }

    public void handleClick(float x, float y) {
        Gdx.app.log("GameManager", "handleClick: Clicked at (" + x + ", " + y + ")");
        Gdx.app.log("GameManager", "handleClick: minigameManager.isMinigameActive() = " + minigameManager.isMinigameActive());
        Gdx.app.log("GameManager", "handleClick: player.getFishingRod().isPointInCastButton = " + player.getFishingRod().isPointInCastButton(x, y));
        Gdx.app.log("GameManager", "handleClick: player.getFishingRod().isInThrowMinigame() = " + player.getFishingRod().isInThrowMinigame());
        
        if (minigameManager.isMinigameActive()) {
            Gdx.app.log("GameManager", "handleClick: Minigame active, delegating click to minigameManager.");
            minigameManager.onClick();
        } else if (player.getFishingRod().isPointInCastButton(x, y)) {
            Gdx.app.log("GameManager", "handleClick: Click on cast/reel button detected.");
            // GameManager now directly determines action based on FishingRod's state
            if (player.getFishingRod().isFishing() && !player.getFishingRod().isReeling() && player.getFishingRod().getLineLength() >= player.getFishingRod().getMaxReachableLength()) {
                // This condition handles reeling if the cast button is clicked again when reeling is due
                Gdx.app.log("GameManager", "handleClick: FishingRod ready to reel, starting reeling.");
                player.getFishingRod().startReeling();
            } else if (player.getFishingRod().getCurrentState() == io.github.gone.entities.FishingRod.FishingState.IDLE) { // Only cast if in IDLE state
                Gdx.app.log("GameManager", "handleClick: FishingRod is in IDLE state, initiating startFishing().");
                player.getFishingRod().startFishing(); // Set FishingRod to THROW_MINIGAME state
                minigameManager.startThrowMinigame(centerX, centerY + 150); // Start the actual minigame
            }
        } else if (player.getFishingRod().isShowingFishCaught()) {
            Gdx.app.log("GameManager", "handleClick: Fish caught screen active, delegating click.");
            player.getFishingRod().getFishCaughtScreen().handleClick(x, y);
        }
    }

    @Override
    public void onThrowComplete(ThrowMinigame.SuccessLevel successLevel) {
        // Minigame is complete, update FishingRod
        player.getFishingRod().onMinigameCompletion(successLevel);
        // Update Gallery (player.playerGallery) with the caught fish
        if (player.getFishingRod().getCaughtFish() != null) {
//            player.getPlayerGallery().addFish(player.getFishingRod().getCaughtFish());
        }
    }
    
    public boolean isMinigameActive() {
        return minigameManager != null && minigameManager.isMinigameActive();
    }

    public boolean isShowingFishCaught() {
        return player != null && player.getFishingRod().isShowingFishCaught();
    }

    public void dispose() {
        if (player != null) {
            player.dispose();
        }
        if (minigameManager != null) {
            minigameManager.dispose();
        }
        // Dispose currentGameState if it has disposable resources
        // if (currentGameState instanceof Disposable) {
        //     ((Disposable) currentGameState).dispose();
        // }
    }
}
