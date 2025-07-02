package io.github.gone.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.gone.entities.FishingRod;
import io.github.gone.entities.Player;
import io.github.gone.minigames.CatchMinigame;
import io.github.gone.minigames.MinigameManager;
import io.github.gone.minigames.ThrowMinigame;
import io.github.gone.fish.FishLootTable;
import io.github.gone.fish.Fish;

public class GameManager implements ThrowMinigame.ThrowMinigameListener, CatchMinigame.CatchMinigameListener {
    private Player player;
    private MinigameManager minigameManager;
    private FishLootTable fishLootTable;
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
        this.minigameManager.getCatchMinigame().setListener(this);
        this.fishLootTable = new FishLootTable();
        // Set initial game state if needed
        // currentGameState = GameState.SOME_INITIAL_STATE;
    }

    public void update(float delta) {

        // Update minigame manager if a minigame is active
        if (minigameManager.isMinigameActive()) {
            minigameManager.update(delta);
        } else {
            player.getFishingRod().update(delta);
        }
    }

    public void draw(SpriteBatch batch) {
        if (minigameManager.isMinigameActive() || player.getFishingRod().isInThrowMinigame()) {
            minigameManager.draw(batch);
        } else {
            player.getFishingRod().draw(batch);
        }
    }

    public void handleClick(float x, float y) {
        FishingRod rod = player.getFishingRod();

        if (minigameManager.isMinigameActive()) {
            minigameManager.onClick();
        } else if (rod.isPointInCastButton(x, y)) {
            // GameManager now directly determines action based on FishingRod's state
            if (rod.isFishing() && !rod.isReeling() && rod.getLineLength() >= rod.getMaxReachableLength()) {
                // This condition handles reeling if the cast button is clicked again when reeling is due
                CatchMinigame.FishDifficulty difficulty = switch (rod.getCaughtFish().getRarity()) {
                    case 1 -> CatchMinigame.FishDifficulty.MEDIUM;
                    case 2 -> CatchMinigame.FishDifficulty.HARD;
                    case 3 -> CatchMinigame.FishDifficulty.LEGENDARY;
                    default -> CatchMinigame.FishDifficulty.EASY;
                };
                minigameManager.startCatchMinigame(centerX, centerY + 150, difficulty);
            } else if (rod.getCurrentState() == io.github.gone.entities.FishingRod.FishingState.IDLE) { // Only cast if in IDLE state
                rod.startFishing(); // Set FishingRod to THROW_MINIGAME state
                minigameManager.startThrowMinigame(centerX, centerY + 150); // Start the actual minigame
            }
        } else if (rod.isShowingFishCaught()) {
            rod.getFishCaughtScreen().handleClick(x, y);
        }
    }

    @Override
    public void onThrowComplete(ThrowMinigame.SuccessLevel successLevel) {
        // Minigame is complete, update FishingRod
        player.getFishingRod().onMinigameCompletion(successLevel);
        
        // Determine the fish using the FishLootTable and pass it to the FishingRod
        Fish determinedFish = fishLootTable.determineFish(successLevel);
        player.getFishingRod().setCaughtFish(determinedFish);
    }
    @Override
    public void onFishingCaught(CatchMinigame.FishingResult result, CatchMinigame.FishDifficulty difficulty) {
        if (result != CatchMinigame.FishingResult.SUCCESS)
        {
            player.getFishingRod().setCaughtFish(null);
        }
        player.getFishingRod().startReeling();
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
    }
}
