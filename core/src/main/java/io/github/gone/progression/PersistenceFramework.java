package io.github.gone.progression;

import java.util.Map;

public interface PersistenceFramework {
    void save(Map<String, ?> data);

    Map<String, ?> load();

    boolean exists();

//    Map<String, ?> getAll();
//    Object getValue(String valueKey);
}