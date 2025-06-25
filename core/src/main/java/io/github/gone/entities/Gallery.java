package io.github.gone.entities;

import io.github.gone.fish.Fish;
import io.github.gone.progression.ProgressionManager;
import io.github.gone.progression.PersistenceFramework;
import io.github.gone.progression.PreferencesPersistenceFramework;
import io.github.gone.fish.FishFactory;
import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Gallery {
    private static Gallery instance;
    public static synchronized Gallery getInstance() {
        if (instance == null) {
            instance = new Gallery();
        }
        return instance;
    }

    private Map<String, FishRegistry> gallery;
    private final PersistenceFramework persistenceFramework;
    private final FishFactory fishFactory;

    public Gallery() {
        this.persistenceFramework = PreferencesPersistenceFramework.getInstance("fish_gallery");
        this.fishFactory = new FishFactory();
        this.gallery = new HashMap<>();
        loadGallery();
    }

    public synchronized void updateGallery(Fish fishCaught)
    {
        if (gallery.containsKey(fishCaught.getName()))
            updateFish(fishCaught.getName(), 1);
        else
            registerFish(fishCaught);
        saveGallery();
    }

    private synchronized void registerFish(Fish newFish)
    {
        gallery.put(newFish.getName(), new FishRegistry(newFish));
    }

    private synchronized void updateFish(String fishName, int caught)
    {
        FishRegistry fishEntry = gallery.get(fishName);
        fishEntry.nCaught += caught;
    }

    public synchronized void saveGallery() {
        Map<String, Integer> dataToSave = gallery.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().nCaught));
        persistenceFramework.save(dataToSave);
    }

    private synchronized void loadGallery() {
        if (persistenceFramework.exists()) {
            Map<String, ?> loadedData = persistenceFramework.load();
            if (loadedData != null) {
                Gdx.app.log("Gallery", "Loading fish gallery from persistence:");
                for (Map.Entry<String, ?> entry : loadedData.entrySet()) {
                    String fishName = entry.getKey();
                    Object caughtCountObj = entry.getValue();

                    if (caughtCountObj instanceof Integer) {
                        int caughtCount = (Integer) caughtCountObj;
                        Gdx.app.log("Gallery", "  " + fishName + ": " + caughtCount + " caught");
                        Fish fish = fishFactory.createFishByName(fishName);
                        if (fish != null) {
                            FishRegistry fishRegistry = new FishRegistry(fish);
                            fishRegistry.nCaught = caughtCount;
                            gallery.put(fishName, fishRegistry);
                        } else {
                            Gdx.app.log("Gallery", "  Warning: Could not create Fish object for name: " + fishName + ". Skipping entry.");
                        }
                    } else {
                        Gdx.app.log("Gallery", "  Warning: Expected Integer for fish count, but got " + caughtCountObj.getClass().getSimpleName() + " for fish: " + fishName + ". Skipping entry.");
                    }
                }
            } else {
                Gdx.app.log("Gallery", "No saved fish gallery data found.");
            }
        } else {
            Gdx.app.log("Gallery", "No existing fish gallery save file found.");
        }
    }

    public synchronized void clearGallery() {
        gallery.clear();
        persistenceFramework.clear();
        Gdx.app.log("Gallery", "Cleared fish gallery data.");
    }

    public synchronized Map<String, FishRegistry> getGallery() {
        return gallery;
    }
}
