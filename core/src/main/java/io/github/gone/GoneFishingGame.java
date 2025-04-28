package io.github.gone;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.gone.screens.GameScreen;

public class GoneFishingGame extends Game {
    private SpriteBatch batch;
    
    @Override
    public void create() {
        batch = new SpriteBatch();
        setScreen(new GameScreen(this));
    }
    
    @Override
    public void render() {
        super.render();
    }
    
    @Override
    public void dispose() {
        batch.dispose();
        getScreen().dispose();
    }
    
    public SpriteBatch getBatch() {
        return batch;
    }
} 