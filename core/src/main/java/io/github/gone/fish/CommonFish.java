package io.github.gone.fish;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.gone.utils.ShapeRendererManager;

/**
 * Represents a common fish with low rarity.
 */
public class CommonFish extends Fish {
    private final ShapeRendererManager shapeRenderer;
    
    public CommonFish() {
        super(1, "Goldfish", "A common goldfish found in ponds and lakes.", 1);
        this.shapeRenderer = new ShapeRendererManager();
    }
    
    @Override
    public void draw(SpriteBatch batch, float x, float y) {
        // End SpriteBatch to use ShapeRenderer
        batch.end();
        
        // Draw a simple fish shape
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GOLD);
        
        // Draw fish body (oval)
        shapeRenderer.getShapeRenderer().ellipse(x - 25, y - 15, 50, 30);
        
        // Draw tail
        shapeRenderer.getShapeRenderer().triangle(
            x - 25, y,         // Tail connection point
            x - 40, y + 15,    // Top of tail
            x - 40, y - 15     // Bottom of tail
        );
        
        // Draw eye
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.getShapeRenderer().circle(x + 15, y + 5, 3);
        
        shapeRenderer.end();
        
        // Resume SpriteBatch
        batch.begin();
    }
    
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
} 