package io.github.gone.progression;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import java.util.HashMap;
import java.util.Map;

public class PreferencesPersistenceFramework implements PersistenceFramework {
    private final Preferences preferences;
    private final String preferencesName;

    public PreferencesPersistenceFramework(String preferencesName) {
        this.preferencesName = preferencesName;
        this.preferences = Gdx.app.getPreferences(preferencesName);
    }

    public static PreferencesPersistenceFramework getInstance(String preferencesName) {
        return new PreferencesPersistenceFramework(preferencesName);
    }

    @Override
    public synchronized void save(Map<String, ?> data) {
        for (Map.Entry<String, ?> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Integer) {
                preferences.putInteger(key, (Integer) value);
            } else if (value instanceof Float) {
                preferences.putFloat(key, (Float) value);
            } else if (value instanceof String) {
                preferences.putString(key, (String) value);
            } else if (value instanceof Boolean) {
                preferences.putBoolean(key, (Boolean) value);
            } else if (value instanceof Long) {
                preferences.putLong(key, (Long) value);
            }
        }
        preferences.flush();
    }

    @Override
    public synchronized Map<String, ?> load() {
        Map<String, Object> result = new HashMap<>();
        Gdx.app.log("PersistenceFramework", "Loading data from preferences: " + preferencesName);

        for (String key : preferences.get().keySet()) {
            Object value = null;
            // Try to retrieve as various types, starting with the most specific
            if (preferences.contains(key)) {
                try {
                    value = preferences.getInteger(key, 0); // Try integer
                } catch (ClassCastException e) {
                    try {
                        value = preferences.getFloat(key, 0.0f); // Try float
                    } catch (ClassCastException e2) {
                        try {
                            value = preferences.getLong(key, 0L); // Try long
                        } catch (ClassCastException e3) {
                            try {
                                value = preferences.getBoolean(key, false); // Try boolean
                            } catch (ClassCastException e4) {
                                value = preferences.getString(key, null); // Default to string
                            }
                        }
                    }
                }
            }

            if (value != null) {
                result.put(key, value);
                Gdx.app.log("PersistenceFramework", "  Key: " + key + ", Loaded Value: " + value + " (Type: " + value.getClass().getSimpleName() + ")");
            } else {
                Gdx.app.log("PersistenceFramework", "  Key: " + key + ", Value is null or could not be loaded.");
            }
        }

        if (result.isEmpty()) {
            Gdx.app.log("PersistenceFramework", "  No data found in preferences.");
        }

        return result;
    }

    public synchronized Preferences getPreferences() {
        return preferences;
    }

    @Override
    public synchronized boolean exists() {
        return !preferences.get().isEmpty();
    }

    @Override
    public synchronized void clear() {
        preferences.clear();
        preferences.flush();
        Gdx.app.log("PersistenceFramework", "Cleared preferences for: " + preferencesName);
    }
} 