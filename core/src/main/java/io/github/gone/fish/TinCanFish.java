package io.github.gone.fish;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.gone.utils.ShapeRendererManager;

/**
 * Represents a tin can caught while fishing.
 * Has minimal XP value but counts towards fishing stats.
 */
public class TinCanFish extends Fish {
    private final ShapeRendererManager shapeRenderer;
    
    public TinCanFish() {
        super(0, "Rusty Can", "An old tin can. Maybe there's still something edible inside?", 1);
        this.shapeRenderer = new ShapeRendererManager();
    }
    
    @Override
    public void draw(SpriteBatch batch, float x, float y) {
        // End SpriteBatch to use ShapeRenderer
        batch.end();
        
        // Draw a tin can
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Can body
        shapeRenderer.setColor(new Color(0.65f, 0.65f, 0.65f, 1f));
        
        // Cylindrical body
        shapeRenderer.getShapeRenderer().rect(x - 20, y - 20, 40, 40);
        
        // Top and bottom ellipses
        shapeRenderer.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        shapeRenderer.getShapeRenderer().ellipse(x - 20, y + 20, 40, 10);
        shapeRenderer.getShapeRenderer().ellipse(x - 20, y - 20, 40, 10);
        
        // Label
        shapeRenderer.setColor(new Color(0.7f, 0.2f, 0.2f, 1f));
        shapeRenderer.getShapeRenderer().rect(x - 18, y - 15, 36, 30);
        
        // Label text (just some lines to suggest text)
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.getShapeRenderer().rect(x - 15, y, 30, 2);
        shapeRenderer.getShapeRenderer().rect(x - 15, y + 5, 25, 2);
        shapeRenderer.getShapeRenderer().rect(x - 15, y - 5, 20, 2);
        
        // Rust spots
        shapeRenderer.setColor(new Color(0.6f, 0.3f, 0.1f, 1f));
        shapeRenderer.getShapeRenderer().circle(x - 10, y - 12, 3);
        shapeRenderer.getShapeRenderer().circle(x + 13, y + 15, 4);
        shapeRenderer.getShapeRenderer().circle(x + 8, y - 18, 2);
        
        // Can opening tab
        shapeRenderer.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
        shapeRenderer.getShapeRenderer().rect(x - 5, y + 20, 10, 5);
        shapeRenderer.getShapeRenderer().circle(x, y + 22, 3);
        
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