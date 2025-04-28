package io.github.gone.fish;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.gone.utils.ShapeRendererManager;

/**
 * Represents a legendary fish with high rarity.
 */
public class LegendaryFish extends Fish {
    private final ShapeRendererManager shapeRenderer;
    private float animTimer = 0f;
    
    public LegendaryFish() {
        super(3, "Marlin", "A legendary deep-sea marlin with a distinctive spear-like bill.", 3);
        this.shapeRenderer = new ShapeRendererManager();
    }
    
    @Override
    public void draw(SpriteBatch batch, float x, float y) {
        // End SpriteBatch to use ShapeRenderer
        batch.end();
        
        // Update animation timer
        animTimer += 0.05f;
        float glowSize = (float) Math.sin(animTimer) * 5 + 15;
        
        // Draw a glow effect
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.4f, 0.7f, 1f, 0.3f));
        shapeRenderer.getShapeRenderer().circle(x, y, 70 + glowSize);
        
        // Draw an impressive fish shape
        shapeRenderer.setColor(Color.ROYAL);
        
        // Draw fish body (elongated)
        shapeRenderer.getShapeRenderer().ellipse(x - 40, y - 20, 80, 40);
        
        // Draw spear/bill
        shapeRenderer.getShapeRenderer().rectLine(
            x + 40, y, 
            x + 80, y, 
            3
        );
        
        // Draw tail (more elaborate)
        shapeRenderer.getShapeRenderer().triangle(
            x - 40, y,        // Tail connection point
            x - 70, y + 35,   // Top of tail
            x - 70, y - 35    // Bottom of tail
        );
        
        // Draw dorsal fin
        shapeRenderer.getShapeRenderer().triangle(
            x - 10, y + 20,     // Start of fin
            x + 20, y + 45,     // Peak of fin
            x + 30, y + 20      // End of fin
        );
        
        // Draw lower fin
        shapeRenderer.getShapeRenderer().triangle(
            x, y - 20,          // Start of fin
            x + 10, y - 40,     // Peak of fin
            x + 30, y - 20      // End of fin
        );
        
        // Draw decorative details
        shapeRenderer.setColor(Color.GOLD);
        shapeRenderer.getShapeRenderer().circle(x + 10, y, 5);
        shapeRenderer.getShapeRenderer().circle(x - 20, y, 3);
        
        // Draw eye
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.getShapeRenderer().circle(x + 25, y + 5, 5);
        
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