package io.github.gone.fish;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import io.github.gone.minigames.ThrowMinigame;
import io.github.gone.progression.ProgressionManager;

/**
 * Determines which fish the player catches based on the throw minigame success level.
 */
public class FishLootTable {
    private final FishFactory fishFactory;
    private final ProgressionManager progressionManager;
    
    // Chance of catching trash instead of fish (decreases with level)
    private static final float[] TRASH_CHANCE_BY_LEVEL = {
        0.4f,   // Level 1: 40% chance of trash
        0.2f,   // Level 2: 20% chance of trash
        0.1f,   // Level 3: 10% chance of trash
        0.05f,  // Level 4: 5% chance of trash
        0.02f   // Level 5: 2% chance of trash
    };
    
    public FishLootTable() {
        this.fishFactory = new FishFactory();
        this.progressionManager = ProgressionManager.getInstance();
    }
    
    /**
     * Determines which fish to generate based on the throw success level.
     * - GREAT success has higher chance of rare and legendary fish
     * - GOOD success has moderate chance of rare fish
     * - MISS has primarily common fish with very low chance of rare
     * 
     * @param successLevel The success level from the throw minigame
     * @return The generated Fish instance
     */
    public Fish determineFish(ThrowMinigame.SuccessLevel successLevel) {
        int playerLevel = progressionManager.getCurrentLevel();
        
        // Chance of catching trash decreases with level
        float trashChance = playerLevel <= TRASH_CHANCE_BY_LEVEL.length 
            ? TRASH_CHANCE_BY_LEVEL[playerLevel - 1] 
            : 0.01f;  // Fallback for higher levels
        
        // If it's a miss, significantly increase chance of trash
        if (successLevel == ThrowMinigame.SuccessLevel.MISS) {
            trashChance *= 2; // Double the trash chance on a miss
        }
        
        // Check if player catches trash
        if (MathUtils.random() < trashChance) {
            Fish trash = fishFactory.generateFish(FishFactory.TRASH_ITEM);
            // Log fish info
            logFishInfo(trash, successLevel);
            return trash;
        }
        
        // Base probabilities for fish types
        float commonChance, rareChance, legendaryChance;
        
        switch (successLevel) {
            case GREAT:
                // Great success provides best chances for rare and legendary fish
                commonChance = 0.30f;
                rareChance = 0.45f;
                legendaryChance = 0.25f;
                break;
                
            case GOOD:
                // Good success provides moderate chances for rare fish
                commonChance = 0.60f;
                rareChance = 0.35f;
                legendaryChance = 0.05f;
                break;
                
            default: // MISS
                // Poor throw primarily yields common fish
                commonChance = 0.85f;
                rareChance = 0.14f;
                legendaryChance = 0.01f;
                break;
        }
        
        // Generate a random value between 0 and 1
        float roll = MathUtils.random();
        
        // Determine fish type based on probability distribution
        int fishType;
        if (roll < commonChance) {
            fishType = FishFactory.COMMON_FISH;
        } else if (roll < commonChance + rareChance) {
            fishType = FishFactory.RARE_FISH;
        } else {
            fishType = FishFactory.LEGENDARY_FISH;
        }
        
        // Use the factory to create the fish
        Fish fish = fishFactory.generateFish(fishType);
        
        // Log fish info
        logFishInfo(fish, successLevel);
        
        return fish;
    }
    
    /**
     * Logs information about the caught fish to help with debugging crashes
     */
    private void logFishInfo(Fish fish, ThrowMinigame.SuccessLevel successLevel) {
        Gdx.app.log("FishingDebug", "===============================================");
        Gdx.app.log("FishingDebug", "FISH CAUGHT - Potential crash debug information");
        Gdx.app.log("FishingDebug", "Class: " + fish.getClass().getSimpleName());
        Gdx.app.log("FishingDebug", "Name: " + fish.getName());
        Gdx.app.log("FishingDebug", "Rarity: " + fish.getRarity());
        Gdx.app.log("FishingDebug", "Weight: " + fish.getWeight());
        Gdx.app.log("FishingDebug", "Success Level: " + successLevel);
        Gdx.app.log("FishingDebug", "Player Level: " + progressionManager.getCurrentLevel());
        Gdx.app.log("FishingDebug", "===============================================");
    }
} 