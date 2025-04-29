package io.github.gone.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Utility class to manage ShapeRenderer lifecycle.
 * Handles begin/end and color setting operations.
 */
public class ShapeRendererManager {
    private final ShapeRenderer shapeRenderer;
    private boolean isDrawing = false;
    
    public ShapeRendererManager() {
        shapeRenderer = new ShapeRenderer();
    }
    
    public void begin(ShapeRenderer.ShapeType shapeType) {
        if (isDrawing) {
            Gdx.app.error("ShapeRendererManager", "Called begin() while already drawing. Ending previous batch first.");
            end();
        }
        
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(shapeType);
        isDrawing = true;
    }
    
    public void end() {
        if (!isDrawing) {
            Gdx.app.error("ShapeRendererManager", "Called end() without a corresponding begin().");
            return;
        }
        
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        isDrawing = false;
    }
    
    public void setColor(Color color) {
        if (!isDrawing) {
            Gdx.app.error("ShapeRendererManager", "Called setColor() without first calling begin().");
            return;
        }
        shapeRenderer.setColor(color);
    }
    
    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }
    
    public void dispose() {
        if (isDrawing) {
            Gdx.app.error("ShapeRendererManager", "ShapeRendererManager disposed while still drawing. Calling end() first.");
            end();
        }
        shapeRenderer.dispose();
    }
} 