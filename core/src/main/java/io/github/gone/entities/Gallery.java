package io.github.gone.entities;

import io.github.gone.fish.Fish;
import io.github.gone.progression.ProgressionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Gallery {
    private static Gallery instance;
    public static synchronized Gallery getInstance() {
        if (instance == null) {
            instance = new Gallery();
        }
        return instance;
    }

    private Map<Fish, FishRegistry> gallery;

    public Gallery() {
        // TODO add persistence
        this.gallery = new HashMap<>();
    }

    public synchronized void updateGallery(Fish fishCaught)
    {
        if (instance.gallery.containsKey(fishCaught))
            updateFish(fishCaught, 1);
        else
            registerFish(fishCaught);
    }

    private synchronized void registerFish(Fish newFish)
    {
        instance.gallery.put(newFish, new FishRegistry(newFish));
    }

    private synchronized void updateFish(Fish fish, int caught)
    {
        FishRegistry fishEntry = instance.gallery.get(fish);
        fishEntry.nCaught += caught;
    }
}
