package io.github.gone.progression;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import java.util.HashMap;
import java.util.Map;

public class PreferencesPersistenceFramework implements PersistenceFramework {
    private static PreferencesPersistenceFramework instance;
    private final Preferences preferences;

    private PreferencesPersistenceFramework(String preferencesName) {
        this.preferences = Gdx.app.getPreferences(preferencesName);
    }

    public static synchronized PreferencesPersistenceFramework getInstance(String preferencesName) {
        if (instance == null) {
            instance = new PreferencesPersistenceFramework(preferencesName);
        }
        return instance;
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
        return new HashMap<>();
    }

    public synchronized Preferences getPreferences() {
        return preferences;
    }

    @Override
    public synchronized boolean exists() {
        return preferences.contains("current_exp") || preferences.contains("current_level") ||
               preferences.contains("total_fish_caught") || preferences.contains("total_weight");
    }
} 