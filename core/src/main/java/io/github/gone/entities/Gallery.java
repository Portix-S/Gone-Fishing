package io.github.gone.entities;

import io.github.gone.fish.Fish;
import io.github.gone.progression.PersistenceFramework;
import io.github.gone.progression.PreferencesPersistenceFramework;
import io.github.gone.fish.FishFactory;
import com.badlogic.gdx.Gdx;

import java.util.HashMap;
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

    // Method to receive a new fish caught to the gallery
    public synchronized void updateGallery(Fish fishCaught)
    {
        if (gallery.containsKey(fishCaught.getName()))
            updateFish(fishCaught.getName());
        else
            registerFish(fishCaught);

        saveGallery();
    }

    // Update has a new fish, we should register it
    private synchronized void registerFish(Fish newFish)
    {
        gallery.put(newFish.getName(), new FishRegistry(newFish));
    }

    // Update has a fish already registered, just change the counter
    // TODO - Check if there's edge case to multiple fishes being caught
    private synchronized void updateFish(String fishName)
    {
        FishRegistry fishEntry = gallery.get(fishName);
        fishEntry.nCaught += 1;
    }

    // Persistence
    public synchronized void saveGallery() {
        Map<String, Integer> dataToSave = gallery
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().nCaught));

        persistenceFramework.save(dataToSave);
    }

    private synchronized void loadGallery() {
        if (!persistenceFramework.exists()) {
            Gdx.app.log("Gallery", "No existing fish gallery save file found.");
            return;
        }

        Map<String, ?> loadedData = persistenceFramework.load();
        if (loadedData == null)
        {
            Gdx.app.log("Gallery", "No saved fish gallery data found.");
            return;
        }

        Gdx.app.log("Gallery", "Loaded data:");
        for (Map.Entry<String, ?> entry : loadedData.entrySet()) {
            String fishName = entry.getKey();
            Object caughtCountObj = entry.getValue();

            if (caughtCountObj instanceof Integer) {
                int caughtCount = (Integer) caughtCountObj;
                Fish fish = fishFactory.createFishByName(fishName);
                Gdx.app.log("Gallery", "  " + fishName + ": " + caughtCount + " caught");

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
