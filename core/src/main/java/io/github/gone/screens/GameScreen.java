package io.github.gone.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.gone.GoneFishingGame;
import io.github.gone.entities.FishingRod;
import io.github.gone.input.InputHandler;

public class GameScreen implements Screen {
    private static final float WORLD_WIDTH = 480;
    private static final float WORLD_HEIGHT = 800;
    
    private final GoneFishingGame game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final FishingRod fishingRod;
    private final InputHandler inputHandler;
    
    public GameScreen(GoneFishingGame game) {
        this.game = game;
        this.batch = game.getBatch();
        
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        
        fishingRod = new FishingRod(new Vector2(WORLD_WIDTH / 2, WORLD_HEIGHT / 4));
        
        inputHandler = new InputHandler(fishingRod, viewport);
        Gdx.input.setInputProcessor(inputHandler);
    }
    
    @Override
    public void show() {
        // Called when this screen becomes the current screen
    }
    
    @Override
    public void render(float delta) {
        // Clear the screen
        Gdx.gl.glClearColor(0.15f, 0.4f, 0.7f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Update game logic
        update(delta);
        
        // Render game elements
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        
        batch.begin();
        fishingRod.draw(batch);
        batch.end();
    }
    
    private void update(float delta) {
        fishingRod.update(delta);
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
    }
    
    @Override
    public void pause() {
        // Called when game is paused
    }
    
    @Override
    public void resume() {
        // Called when game is resumed
    }
    
    @Override
    public void hide() {
        // Called when this screen is no longer the current screen
    }
    
    @Override
    public void dispose() {
        // Dispose of resources
        fishingRod.dispose();
    }
} 