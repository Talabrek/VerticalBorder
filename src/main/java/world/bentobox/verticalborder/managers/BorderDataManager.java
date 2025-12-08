package world.bentobox.verticalborder.managers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.verticalborder.VerticalBorderAddon;
import world.bentobox.verticalborder.database.BorderIslandData;

/**
 * Manages border data storage and retrieval for islands.
 */
public class BorderDataManager {

    private final VerticalBorderAddon addon;
    private final Database<BorderIslandData> database;
    private final Map<String, BorderIslandData> cache;

    /**
     * Create a new BorderDataManager.
     * @param addon The VerticalBorder addon instance
     */
    public BorderDataManager(VerticalBorderAddon addon) {
        this.addon = addon;
        this.database = new Database<>(addon, BorderIslandData.class);
        this.cache = new ConcurrentHashMap<>();

        // Load all existing data into cache
        database.loadObjects().forEach(data -> cache.put(data.getUniqueId(), data));
        addon.log("Loaded " + cache.size() + " island border configurations.");
    }

    /**
     * Get border data for an island, creating default data if none exists.
     * @param island The island to get data for
     * @return BorderIslandData for the island
     */
    public BorderIslandData getData(Island island) {
        return cache.computeIfAbsent(island.getUniqueId(), id -> {
            // Check if object exists in database before trying to load
            // This avoids ERROR logs from BentoBox when file doesn't exist
            if (database.objectExists(id)) {
                BorderIslandData data = database.loadObject(id);
                if (data != null) {
                    return data;
                }
            }
            // Create new data with defaults
            BorderIslandData newData = new BorderIslandData(id,
                addon.getSettings().getDefaultTopY(),
                addon.getSettings().getDefaultBottomY());
            newData.setCeilingEnabled(addon.getSettings().isDefaultCeilingEnabled());
            newData.setFloorEnabled(addon.getSettings().isDefaultFloorEnabled());
            return newData;
        });
    }

    /**
     * Get border data by island ID, without creating new data.
     * @param islandId The island's unique ID
     * @return BorderIslandData or null if not found
     */
    public BorderIslandData getDataById(String islandId) {
        return cache.get(islandId);
    }

    /**
     * Save border data for an island.
     * @param data The border data to save
     */
    public void saveData(BorderIslandData data) {
        cache.put(data.getUniqueId(), data);
        database.saveObjectAsync(data);
    }

    /**
     * Delete border data for an island.
     * @param islandId The island's unique ID
     */
    public void deleteData(String islandId) {
        cache.remove(islandId);
        database.deleteID(islandId);
    }

    /**
     * Check if data exists for an island.
     * @param islandId The island's unique ID
     * @return true if data exists
     */
    public boolean hasData(String islandId) {
        return cache.containsKey(islandId);
    }

    /**
     * Save all cached data to the database.
     */
    public void saveAll() {
        cache.values().forEach(data -> database.saveObjectAsync(data));
        addon.log("Saved " + cache.size() + " island border configurations.");
    }

    /**
     * Get the cache map (for iteration purposes).
     * @return Map of island IDs to border data
     */
    public Map<String, BorderIslandData> getCache() {
        return cache;
    }
}
