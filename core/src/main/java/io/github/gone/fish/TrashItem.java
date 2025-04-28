package io.github.gone.fish;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.gone.utils.ShapeRendererManager;

/**
 * Represents a piece of trash caught while fishing.
 * Has minimal XP value but counts towards fishing stats.
 */
public class TrashItem extends Fish {
    private final ShapeRendererManager shapeRenderer;
    
    public TrashItem() {
        super(0, "Old Boot", "A waterlogged boot. Not a fish, but at least you caught something!", 1);
        this.shapeRenderer = new ShapeRendererManager();
    }
    
    @Override
    public void draw(SpriteBatch batch, float x, float y) {
        // End SpriteBatch to use ShapeRenderer
        batch.end();
        
        // Draw a basic boot shape
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Boot base color (brown)
        shapeRenderer.setColor(new Color(0.4f, 0.25f, 0.1f, 1f));
        
        // Draw the boot sole
        shapeRenderer.getShapeRenderer().rect(x - 40, y - 15, 60, 12);
        
        // Draw the boot upper
        shapeRenderer.getShapeRenderer().rect(x - 40, y - 3, 25, 30);
        
        // Add some boot details
        shapeRenderer.setColor(new Color(0.3f, 0.2f, 0.1f, 1f));
        
        // Boot heel
        shapeRenderer.getShapeRenderer().rect(x - 40, y - 15, 15, 20);
        
        // Boot laces
        shapeRenderer.setColor(Color.BLACK);
        for (int i = 0; i < 3; i++) {
            shapeRenderer.getShapeRenderer().rect(x - 35, y + 0 + i * 8, 15, 2);
        }
        
        // Draw water dripping
        shapeRenderer.setColor(new Color(0.3f, 0.7f, 0.9f, 0.7f));
        shapeRenderer.getShapeRenderer().circle(x - 30, y - 20, 2);
        shapeRenderer.getShapeRenderer().circle(x - 20, y - 25, 3);
        shapeRenderer.getShapeRenderer().circle(x, y - 23, 2);
        
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