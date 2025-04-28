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
    
    public ShapeRendererManager() {
        shapeRenderer = new ShapeRenderer();
    }
    
    public void begin(ShapeRenderer.ShapeType shapeType) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(shapeType);
    }
    
    public void end() {
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }
    
    public void setColor(Color color) {
        shapeRenderer.setColor(color);
    }
    
    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }
    
    public void dispose() {
        shapeRenderer.dispose();
    }
} 