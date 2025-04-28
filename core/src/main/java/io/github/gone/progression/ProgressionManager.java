package io.github.gone.progression;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import io.github.gone.fish.Fish;

/**
 * Manages player progression, including experience points, level tracking
 * and persisting progression data between game sessions.
 */
public class ProgressionManager {
    // Singleton instance
    private static ProgressionManager instance;
    
    // Experience constants
    private static final int BASE_XP_PER_FISH = 10;
    private static final int[] LEVEL_XP_REQUIREMENTS = {
        0,      // Level 0 (unused)
        0,      // Level 1 starts at 0 XP
        100,    // Level 2 requires 100 XP
        250,    // Level 3 requires 250 XP
        500,    // Level 4 requires 500 XP
        1000    // Level 5 requires 1000 XP
    };
    private static final int MAX_LEVEL = LEVEL_XP_REQUIREMENTS.length - 1;
    
    // Player progression data
    private int currentExp;
    private int currentLevel;
    private int totalFishCaught;
    private float totalWeight;
    private boolean hasLeveledUp;
    
    // Storage
    private final Preferences preferences;
    
    private ProgressionManager() {
        preferences = Gdx.app.getPreferences("fishing_progression");
        loadProgress();
        hasLeveledUp = false;
    }
    
    /**
     * Get the singleton instance of ProgressionManager
     */
    public static synchronized ProgressionManager getInstance() {
        if (instance == null) {
            instance = new ProgressionManager();
        }
        return instance;
    }
    
    /**
     * Load progression data from preferences
     */
    private void loadProgress() {
        currentExp = preferences.getInteger("current_exp", 0);
        currentLevel = preferences.getInteger("current_level", 1);
        totalFishCaught = preferences.getInteger("total_fish_caught", 0);
        totalWeight = preferences.getFloat("total_weight", 0f);
    }
    
    /**
     * Save progression data to preferences
     */
    private void saveProgress() {
        preferences.putInteger("current_exp", currentExp);
        preferences.putInteger("current_level", currentLevel);
        preferences.putInteger("total_fish_caught", totalFishCaught);
        preferences.putFloat("total_weight", totalWeight);
        preferences.flush();
    }
    
    /**
     * Add experience points when player catches a fish
     */
    public void addExperienceForFish(Fish fish, float weight) {
        // Base XP + bonus for rarity
        int expGained = BASE_XP_PER_FISH * fish.getRarity();
        
        // Store current level for comparison
        int previousLevel = currentLevel;
        
        currentExp += expGained;
        totalFishCaught++;
        totalWeight += weight;
        
        // Log experience gained
        Gdx.app.log("ProgressionManager", "===============================================");
        Gdx.app.log("ProgressionManager", "EXPERIENCE AWARDED");
        Gdx.app.log("ProgressionManager", "Fish: " + fish.getName());
        Gdx.app.log("ProgressionManager", "Rarity: " + fish.getRarity());
        Gdx.app.log("ProgressionManager", "Weight: " + weight);
        Gdx.app.log("ProgressionManager", "XP Gained: " + expGained);
        Gdx.app.log("ProgressionManager", "Current XP: " + currentExp);
        Gdx.app.log("ProgressionManager", "Total Fish: " + totalFishCaught);
        Gdx.app.log("ProgressionManager", "Total Weight: " + totalWeight);
        
        // Check for level up
        checkProgress();
        
        // Set level up flag if level changed
        if (currentLevel > previousLevel) {
            hasLeveledUp = true;
            
            // Log level up
            Gdx.app.log("ProgressionManager", "LEVEL UP!");
            Gdx.app.log("ProgressionManager", "New Level: " + currentLevel);
            Gdx.app.log("ProgressionManager", "Next Level Requirement: " + getNextLevelRequirement());
        }
        Gdx.app.log("ProgressionManager", "===============================================");
        
        // Save progress
        saveProgress();
    }
    
    /**
     * Check if player has leveled up and update level if necessary
     */
    private void checkProgress() {
        for (int level = currentLevel + 1; level <= MAX_LEVEL; level++) {
            if (currentExp >= LEVEL_XP_REQUIREMENTS[level]) {
                currentLevel = level;
            } else {
                break;
            }
        }
    }
    
    /**
     * Get the current player level
     */
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    /**
     * Get the current player experience
     */
    public int getCurrentExp() {
        return currentExp;
    }
    
    /**
     * Get the experience required for the next level
     */
    public int getNextLevelRequirement() {
        if (currentLevel >= MAX_LEVEL) {
            return 0; // Max level reached
        }
        return LEVEL_XP_REQUIREMENTS[currentLevel + 1];
    }
    
    /**
     * Get the experience required for a specific level
     */
    public int getLevelRequirement(int level) {
        if (level < 1 || level >= LEVEL_XP_REQUIREMENTS.length) {
            return 0;
        }
        return LEVEL_XP_REQUIREMENTS[level];
    }
    
    /**
     * Get the total number of fish caught
     */
    public int getTotalFishCaught() {
        return totalFishCaught;
    }
    
    /**
     * Get the total weight of all fish caught
     */
    public float getTotalWeight() {
        return totalWeight;
    }
    
    /**
     * Check if player has leveled up since last call,
     * and reset the flag if they have
     */
    public boolean checkAndClearLevelUpFlag() {
        boolean result = hasLeveledUp;
        hasLeveledUp = false;
        return result;
    }
    
    /**
     * Reset all progression (for testing purposes)
     */
    public void resetProgress() {
        currentExp = 0;
        currentLevel = 1;
        totalFishCaught = 0;
        totalWeight = 0;
        hasLeveledUp = false;
        saveProgress();
    }
} 