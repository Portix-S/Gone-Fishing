package io.github.gone.fish;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.gone.utils.ShapeRendererManager;

/**
 * Represents a colorful clownfish with distinctive stripes.
 */
public class ClownFish extends Fish {
    private final ShapeRendererManager shapeRenderer;
    
    public ClownFish() {
        super(2, "Clownfish", "A bright orange clownfish with white stripes. Popular in reef aquariums.", 2);
        this.shapeRenderer = new ShapeRendererManager();
    }
    
    @Override
    public void draw(SpriteBatch batch, float x, float y) {
        // End SpriteBatch to use ShapeRenderer
        batch.end();
        
        // Draw a clownfish with distinctive stripes
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Main body (bright orange)
        shapeRenderer.setColor(new Color(1f, 0.5f, 0f, 1f));
        shapeRenderer.getShapeRenderer().ellipse(x - 25, y - 15, 50, 30);
        
        // Tail
        shapeRenderer.getShapeRenderer().triangle(
            x - 25, y,         // Tail connection point
            x - 45, y + 15,    // Top of tail
            x - 45, y - 15     // Bottom of tail
        );
        
        // Fins
        shapeRenderer.getShapeRenderer().triangle(
            x - 10, y + 15,    // Dorsal fin start
            x + 5, y + 30,     // Dorsal fin peak
            x + 15, y + 15     // Dorsal fin end
        );
        
        shapeRenderer.getShapeRenderer().triangle(
            x - 5, y - 15,     // Bottom fin start
            x + 5, y - 25,     // Bottom fin peak
            x + 15, y - 15     // Bottom fin end
        );
        
        // White stripes (characteristic of clownfish)
        shapeRenderer.setColor(Color.WHITE);
        
        // Head stripe
        shapeRenderer.getShapeRenderer().ellipse(x + 10, y - 10, 10, 20);
        
        // Middle stripe
        shapeRenderer.getShapeRenderer().ellipse(x - 5, y - 10, 8, 20);
        
        // Tail stripe
        shapeRenderer.getShapeRenderer().ellipse(x - 20, y - 10, 7, 20);
        
        // Black outlines around stripes (simplified)
        shapeRenderer.setColor(Color.BLACK);
        
        // Outline around head stripe
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.getShapeRenderer().ellipse(x + 10, y - 10, 10, 20);
        
        // Outline around middle stripe
        shapeRenderer.getShapeRenderer().ellipse(x - 5, y - 10, 8, 20);
        
        // Outline around tail stripe
        shapeRenderer.getShapeRenderer().ellipse(x - 20, y - 10, 7, 20);
        shapeRenderer.end();
        
        // Continue with filled shapes
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Eye
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.getShapeRenderer().circle(x + 20, y + 5, 3);
        
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