package io.github.gone.minigames;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.gone.states.GameState; // Assuming GameState is in states package

public class MinigameManager {
    private ThrowMinigame throwMinigame;
    // Potentially other minigames later, e.g., CatchMinigame catchMinigame;

    public MinigameManager(float centerX, float centerY) {
        this.throwMinigame = new ThrowMinigame(centerX, centerY);
    }

    public void setupMinigame(GameState state) {
        // This method will be responsible for setting up the correct minigame
        // based on the GameState or other game logic.
        // For now, we'll just ensure ThrowMinigame can be started.
        // The actual instantiation of ThrowMinigame will likely move here or be passed in.
    }

    public void startThrowMinigame(float centerX, float centerY) {
        if (this.throwMinigame == null) {
            this.throwMinigame = new ThrowMinigame(centerX, centerY);
        }
        this.throwMinigame.start();
    }

    public void update(float delta) {
        if (throwMinigame != null && throwMinigame.isActive() || throwMinigame.isShowingResult()) {
            throwMinigame.update(delta);
        }
    }

    public void draw(SpriteBatch batch) {
        if (throwMinigame != null && throwMinigame.isActive() || throwMinigame.isShowingResult()) {
            throwMinigame.draw(batch);
        }
    }

    public void onClick() {
        if (throwMinigame != null && throwMinigame.isActive()) {
            throwMinigame.onClick();
        }
    }
    
    public boolean isMinigameActive() {
        return (throwMinigame != null && throwMinigame.isActive()) || (throwMinigame != null && throwMinigame.isShowingResult());
    }

    public ThrowMinigame getThrowMinigame() {
        return throwMinigame;
    }

    public void dispose() {
        if (throwMinigame != null) {
            throwMinigame.dispose();
        }
    }
}
