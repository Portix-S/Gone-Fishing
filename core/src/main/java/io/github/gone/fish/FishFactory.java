package io.github.gone.fish;

/**
 * Factory class for creating fish instances based on their type.
 */
public class FishFactory {
    
    /**
     * Creates a fish of the specified type.
     * 
     * @param fishType Type of fish to create (1=Common, 2=Rare, 3=Legendary)
     * @return The created Fish instance
     */
    public Fish generateFish(int fishType) {
        switch (fishType) {
            case 1:
                return new CommonFish();
            case 2:
                return new RareFish();
            case 3:
                return new LegendaryFish();
            default:
                // Default to common fish if an invalid type is provided
                return new CommonFish();
        }
    }
} 