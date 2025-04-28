package io.github.gone.fish;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.gone.utils.ShapeRendererManager;

/**
 * Represents a common trout fish.
 */
public class TroutFish extends Fish {
    private final ShapeRendererManager shapeRenderer;
    
    public TroutFish() {
        super(1, "Trout", "A freshwater trout with distinctive spots.", 1);
        this.shapeRenderer = new ShapeRendererManager();
    }
    
    @Override
    public void draw(SpriteBatch batch, float x, float y) {
        // End SpriteBatch to use ShapeRenderer
        batch.end();
        
        // Draw a simple fish shape
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Fish body (light green-brown)
        shapeRenderer.setColor(new Color(0.5f, 0.55f, 0.4f, 1f));
        shapeRenderer.getShapeRenderer().ellipse(x - 25, y - 15, 50, 30);
        
        // Tail
        shapeRenderer.getShapeRenderer().triangle(
            x - 25, y,         // Tail connection point
            x - 40, y + 15,    // Top of tail
            x - 40, y - 15     // Bottom of tail
        );
        
        // Dorsal fin
        shapeRenderer.getShapeRenderer().triangle(
            x - 10, y + 15,    // Fin start
            x, y + 25,         // Fin peak
            x + 10, y + 15     // Fin end
        );
        
        // Spots (characteristic of trout)
        shapeRenderer.setColor(new Color(0.3f, 0.3f, 0.2f, 1f));
        shapeRenderer.getShapeRenderer().circle(x - 15, y + 5, 2);
        shapeRenderer.getShapeRenderer().circle(x - 5, y + 8, 2);
        shapeRenderer.getShapeRenderer().circle(x + 5, y + 3, 2);
        shapeRenderer.getShapeRenderer().circle(x - 10, y - 5, 2);
        shapeRenderer.getShapeRenderer().circle(x, y - 8, 2);
        shapeRenderer.getShapeRenderer().circle(x + 10, y - 3, 2);
        
        // Eye
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