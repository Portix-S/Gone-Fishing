package io.github.gone.input;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.gone.entities.FishingRod;

/**
 * Handles user input for the fishing game.
 */
public class InputHandler extends InputAdapter {
    private final FishingRod fishingRod;
    private final Viewport viewport;
    private final Vector3 touchPoint;
    
    public InputHandler(FishingRod fishingRod, Viewport viewport) {
        this.fishingRod = fishingRod;
        this.viewport = viewport;
        this.touchPoint = new Vector3();
    }
    
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Convert screen coordinates to world coordinates
        touchPoint.set(screenX, screenY, 0);
        viewport.unproject(touchPoint);
        
        // Delegate to the fishing rod to handle the click based on its current state
        fishingRod.handleClick(touchPoint.x, touchPoint.y);
        
        return true;
    }
    
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }
    
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }
} 