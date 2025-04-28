package io.github.gone.fish;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.gone.utils.ShapeRendererManager;

/**
 * Represents a rare fish with medium rarity.
 */
public class RareFish extends Fish {
    private final ShapeRendererManager shapeRenderer;
    
    public RareFish() {
        super(2, "Koi", "A rare and colorful koi fish with distinctive patterns.", 2);
        this.shapeRenderer = new ShapeRendererManager();
    }
    
    @Override
    public void draw(SpriteBatch batch, float x, float y) {
        // End SpriteBatch to use ShapeRenderer
        batch.end();
        
        // Draw a more sophisticated fish shape
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw fish body (oval)
        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.getShapeRenderer().ellipse(x - 30, y - 20, 60, 40);
        
        // Draw tail
        shapeRenderer.getShapeRenderer().triangle(
            x - 30, y,         // Tail connection point
            x - 55, y + 25,    // Top of tail
            x - 55, y - 25     // Bottom of tail
        );
        
        // Draw patterns
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.getShapeRenderer().ellipse(x - 15, y - 10, 25, 20);
        
        // Draw fins
        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.getShapeRenderer().triangle(
            x, y + 20,        // Top fin start
            x + 15, y + 35,   // Top fin peak
            x + 30, y + 20    // Top fin end
        );
        
        shapeRenderer.getShapeRenderer().triangle(
            x, y - 20,        // Bottom fin start
            x + 15, y - 35,   // Bottom fin peak
            x + 30, y - 20    // Bottom fin end
        );
        
        // Draw eye
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.getShapeRenderer().circle(x + 15, y + 5, 4);
        
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