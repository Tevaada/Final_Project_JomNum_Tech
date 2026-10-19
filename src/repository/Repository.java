package repository;

import java.util.ArrayList;
import java.util.HashMap;

public class Repository<T> {
    private final HashMap<String, T> items = new HashMap<>();

    public void add(String id, T item) {
        validateId(id);

        if (item == null) {
            throw new IllegalArgumentException(
                "Cannot store a null item."
            );
        }

        if (items.containsKey(id)) {
            throw new IllegalArgumentException(
                "ID already exists: " + id
            );
        }

        items.put(id, item);
    }

    public T findById(String id) {
        validateId(id);
        return items.get(id);
    }

    public boolean exists(String id) {
        validateId(id);
        return items.containsKey(id);
    }

    public ArrayList<T> findAll() {
        return new ArrayList<>(items.values());
    }

    public void delete(String id) {
        validateId(id);

        if (!items.containsKey(id)) {
            throw new IllegalArgumentException(
                "ID not found: " + id
            );
        }

        items.remove(id);
    }

    private void validateId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                "ID cannot be empty."
            );
        }
    }
}
