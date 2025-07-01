package io.github.gone.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import io.github.gone.entities.Gallery;

public class Player {
    private FishingRod currentRod;
    private Gallery playerGallery;

    public Player(float x, float y) {
        this.currentRod = new FishingRod(new Vector2(x, y));
        this.playerGallery = new Gallery();
    }

    public FishingRod getFishingRod() {
        return currentRod;
    }

    public Gallery getPlayerGallery() {
        return playerGallery;
    }

    public void dispose() {
        if (currentRod != null) {
            currentRod.dispose();
        }
        if (playerGallery != null) {
            // playerGallery.dispose(); // Assuming Gallery might need disposal
        }
    }
}
