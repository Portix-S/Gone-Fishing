package io.github.gone.fish;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.github.gone.utils.ShapeRendererManager;

/**
 * Represents a fierce shark fish, a legendary catch.
 */
public class SharkFish extends Fish {
    private final ShapeRendererManager shapeRenderer;
    private float animTimer = 0f;
    
    public SharkFish() {
        super(3, "Great White Shark", "An apex predator of the ocean, this great white shark is a legendary catch!", 3);
        this.shapeRenderer = new ShapeRendererManager();
    }
    
    @Override
    public void draw(SpriteBatch batch, float x, float y) {
        // End SpriteBatch to use ShapeRenderer
        batch.end();
        
        // Update animation timer
        animTimer += 0.05f;
        float jawOffset = (float) Math.sin(animTimer) * 3;
        
        // Draw intimidating shark
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw a glow effect for legendary status
        shapeRenderer.setColor(new Color(0.2f, 0.4f, 0.7f, 0.3f));
        shapeRenderer.getShapeRenderer().circle(x, y, 80);
        
        // Main body (gray)
        shapeRenderer.setColor(new Color(0.4f, 0.45f, 0.5f, 1f));
        
        // Elongated body
        shapeRenderer.getShapeRenderer().ellipse(x - 50, y - 20, 100, 40);
        
        // Dorsal fin (iconic shark fin)
        shapeRenderer.getShapeRenderer().triangle(
            x, y + 20,        // Fin base start
            x - 10, y + 50,   // Fin peak
            x + 20, y + 20    // Fin base end
        );
        
        // Tail fin
        shapeRenderer.getShapeRenderer().triangle(
            x - 50, y,        // Tail connection
            x - 80, y + 30,   // Upper fin
            x - 70, y        // Middle point
        );
        
        shapeRenderer.getShapeRenderer().triangle(
            x - 50, y,        // Tail connection
            x - 80, y - 30,   // Lower fin
            x - 70, y        // Middle point
        );
        
        // Pectoral fin
        shapeRenderer.getShapeRenderer().triangle(
            x, y - 20,        // Fin base start
            x + 30, y - 40,   // Fin tip
            x + 20, y - 20    // Fin base end
        );
        
        // White underbelly
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.getShapeRenderer().ellipse(x - 50, y - 20, 100, 25);
        
        // Head & jaws detail
        shapeRenderer.setColor(new Color(0.4f, 0.45f, 0.5f, 1f));
        shapeRenderer.getShapeRenderer().ellipse(x + 45, y, 20, 15);
        
        // Mouth with animated jaws
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.getShapeRenderer().arc(x + 48, y, 15, 180, 180);
        
        // Teeth
        shapeRenderer.setColor(Color.WHITE);
        for (int i = 0; i < 6; i++) {
            float toothX = x + 48 + MathUtils.cos((i * 30 + 180) * MathUtils.degreesToRadians) * 15;
            float toothY = y + MathUtils.sin((i * 30 + 180) * MathUtils.degreesToRadians) * 15;
            shapeRenderer.getShapeRenderer().triangle(
                toothX, toothY,
                toothX - 2, toothY - (5 + jawOffset),
                toothX + 2, toothY - (5 + jawOffset)
            );
        }
        
        // Eye
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.getShapeRenderer().circle(x + 30, y + 8, 4);
        
        // Gill slits
        shapeRenderer.setColor(Color.DARK_GRAY);
        for (int i = 0; i < 3; i++) {
            shapeRenderer.getShapeRenderer().rect(x + 20 - (i * 10), y - 10, 2, 20);
        }
        
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