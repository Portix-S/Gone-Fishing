package io.github.gone.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class GameState {
    public abstract void update(float delta);
    public abstract void draw(SpriteBatch batch);
}
