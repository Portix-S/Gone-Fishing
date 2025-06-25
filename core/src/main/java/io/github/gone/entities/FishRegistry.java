package io.github.gone.entities;

import io.github.gone.fish.Fish;

public class FishRegistry {
    public Fish fishInfo;
    public int nCaught;

    public FishRegistry(Fish fishInfo) {
        this.fishInfo = fishInfo;
        nCaught = 1;
    }
}
