package io.github.gone.fish;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Base class for all fish types in the game.
 */
public abstract class Fish {
    protected int sprite;
    protected String name;
    protected String description;
    protected int rarity;
    
    public Fish(int sprite, String name, String description, int rarity) {
        this.sprite = sprite;
        this.name = name;
        this.description = description;
        this.rarity = rarity;
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
    
    /**
     * Renders the fish on screen
     */
    public abstract void draw(SpriteBatch batch, float x, float y);
} 