package io.github.gone.fish;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/**
 * Base class for all fish types in the game.
 */
public abstract class Fish {
    protected int sprite;
    protected String name;
    protected String description;
    protected int rarity;
    protected float weight; // Weight in kg
    
    public Fish(int sprite, String name, String description, int rarity) {
        this.sprite = sprite;
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        
        // Generate a random weight based on rarity
        generateRandomWeight();
    }
    
    /**
     * Generates a random weight for the fish based on its rarity
     */
    private void generateRandomWeight() {
        // Higher rarity means larger potential weight
        float baseWeight = 0.5f * rarity;
        float variance = 0.5f * rarity;
        
        // Generate a weight with some randomness
        weight = baseWeight + MathUtils.random() * variance;
        
        // Round to 2 decimal places
        weight = Math.round(weight * 100) / 100f;
    }
    
    public int getSprite() {
        return sprite;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getRarity() {
        return rarity;
    }
    
    public float getWeight() {
        return weight;
    }
    
    /**
     * Renders the fish on screen
     */
    public abstract void draw(SpriteBatch batch, float x, float y);
} 