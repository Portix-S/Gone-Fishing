package io.github.gone.fish;

import com.badlogic.gdx.math.MathUtils;
import io.github.gone.minigames.ThrowMinigame;

/**
 * Determines which fish the player catches based on the throw minigame success level.
 */
public class FishLootTable {
    private final FishFactory fishFactory;
    
    public FishLootTable() {
        this.fishFactory = new FishFactory();
    }
    
    /**
     * Determines which fish to generate based on the skill check success level.
     * - GREAT success has higher chance of rare and legendary fish
     * - GOOD success has moderate chance of rare fish
     * - MISS has primarily common fish with very low chance of rare
     * 
     * @param successLevel The success level from the throw minigame
     * @return The generated Fish instance
     */
    public Fish determineFish(ThrowMinigame.SuccessLevel successLevel) {
        // Base probabilities
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
            fishType = 1; // Common fish
        } else if (roll < commonChance + rareChance) {
            fishType = 2; // Rare fish
        } else {
            fishType = 3; // Legendary fish
        }
        
        // Use the factory to create the fish
        return fishFactory.generateFish(fishType);
    }
} 