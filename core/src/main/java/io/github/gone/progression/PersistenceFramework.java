package io.github.gone.progression;

import java.util.Map;

public interface PersistenceFramework {
    void save(Map<String, ?> data);

    Map<String, ?> load();

    boolean exists();
} 