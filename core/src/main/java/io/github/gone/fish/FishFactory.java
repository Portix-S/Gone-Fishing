package io.github.gone.fish;

import com.badlogic.gdx.math.MathUtils;
import io.github.gone.progression.ProgressionManager;
import java.util.Map;

/**
 * Factory class for creating fish instances based on their type.
 */
public class FishFactory {
    private final ProgressionManager progressionManager;
    
    // Constants for fish types
    public static final int TRASH_ITEM = 0;
    public static final int COMMON_FISH = 1;
    public static final int RARE_FISH = 2;
    public static final int LEGENDARY_FISH = 3;
    
    public FishFactory() {
        this.progressionManager = ProgressionManager.getInstance();
    }
    
    /**
     * Creates a fish of the specified type.
     * 
     * @param fishType Type of fish to create (0=Trash, 1=Common, 2=Rare, 3=Legendary)
     * @return The created Fish instance
     */
    public Fish generateFish(int fishType) {
        int playerLevel = progressionManager.getCurrentLevel();
        
        // If player level is too low for the requested fish type, adjust accordingly
        if (fishType == LEGENDARY_FISH && playerLevel < 3) {
            // Legendary fish requires level 3 or higher
            fishType = RARE_FISH; // Downgrade to rare fish
        } else if (fishType == RARE_FISH && playerLevel < 2) {
            // Rare fish requires level 2 or higher
            fishType = COMMON_FISH; // Downgrade to common fish
        }
        
        switch (fishType) {
            case TRASH_ITEM:
                // Randomly select a trash item
                int trashType = MathUtils.random(1);
                if (trashType == 0) {
                    return new TrashItem();
                } else {
                    return new TinCanFish();
                }
                
            case COMMON_FISH:
                // Randomly select a common fish
                int commonType = MathUtils.random(1);
                if (commonType == 0) {
                    return new CommonFish();
                } else {
                    return new TroutFish();
                }
                
            case RARE_FISH:
                // Randomly select a rare fish
                int rareType = MathUtils.random(1);
                if (rareType == 0) {
                    return new RareFish();
                } else {
                    return new ClownFish();
                }
                
            case LEGENDARY_FISH:
                // Randomly select a legendary fish
                int legendaryType = MathUtils.random(1);
                if (legendaryType == 0) {
                    return new LegendaryFish();
                } else {
                    return new SharkFish();
                }
                
            default:
                // Default to common fish if an invalid type is provided
                return new CommonFish();
        }
    }
    
    /**
     * Creates a fish instance based on its name.
     * This method is primarily for reconstructing Fish objects from persisted data.
     * It does not consider player level for fish type generation, always returning the base type.
     * @param fishName The name of the fish to create.
     * @return The created Fish instance, or null if the name is not recognized.
     */
    public Fish createFishByName(String fishName) {
        // This could be made more robust with a Map<String, Integer> for lookup
        // For now, a switch statement will suffice given the limited number of fish types.
        switch (fishName) {
            case "Goldfish":
                return new CommonFish();
            case "Trout":
                return new TroutFish();
            case "Koi":
                return new RareFish();
            case "Clownfish":
                return new ClownFish();
            case "Great White Shark":
                return new SharkFish();
            case "Legendary Fish": // Assuming this is the name for the generic LegendaryFish
                return new LegendaryFish();
            case "Trash Item": // Assuming this is the name for the generic TrashItem
                return new TrashItem();
            case "Tin Can Fish": // Assuming this is the name for TinCanFish
                return new TinCanFish();
            default:
                // Log a warning or throw an exception if an unknown fish name is encountered.
                // For now, return null.
                return null;
        }
    }
    
    /**
     * Checks if a particular fish type is available at the current player level.
     * 
     * @param fishType Type of fish to check (0=Trash, 1=Common, 2=Rare, 3=Legendary)
     * @return true if the fish type is available, false otherwise
     */
    public boolean isFishTypeAvailable(int fishType) {
        int playerLevel = progressionManager.getCurrentLevel();
        
        switch (fishType) {
            case TRASH_ITEM: // Trash - always available
            case COMMON_FISH: // Common fish - always available
                return true;
            case RARE_FISH: // Rare fish - requires level 2+
                return playerLevel >= 2;
            case LEGENDARY_FISH: // Legendary fish - requires level 3+
                return playerLevel >= 3;
            default:
                return false;
        }
    }

    /**
     * Returns a map of all possible fish names with a dummy value (e.g., 0).
     * This is used for populating the gallery with all known fish, including those not yet caught.
     * @return A Map where keys are fish names and values are placeholder integers.
     */
    public Map<String, Integer> getAllFishNames() {
        Map<String, Integer> allFish = new java.util.HashMap<>();
        allFish.put("Goldfish", 0);
        allFish.put("Trout", 0);
        allFish.put("Koi", 0);
        allFish.put("Clownfish", 0);
        allFish.put("Great White Shark", 0);
        allFish.put("Legendary Fish", 0);
        allFish.put("Trash Item", 0);
        allFish.put("Tin Can Fish", 0);
        return allFish;
    }
} 