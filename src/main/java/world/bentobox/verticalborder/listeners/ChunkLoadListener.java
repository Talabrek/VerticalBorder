package world.bentobox.verticalborder.listeners;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import world.bentobox.verticalborder.VerticalBorderAddon;
import world.bentobox.verticalborder.database.BorderIslandData;

/**
 * Listens for chunk load events to regenerate barriers when needed.
 */
public class ChunkLoadListener implements Listener {

    private final VerticalBorderAddon addon;
    private final Set<Long> pendingChunks;

    /**
     * Create a new ChunkLoadListener.
     * @param addon The VerticalBorder addon instance
     */
    public ChunkLoadListener(VerticalBorderAddon addon) {
        this.addon = addon;
        this.pendingChunks = ConcurrentHashMap.newKeySet();
    }

    /**
     * Handle chunk load events - regenerate barriers in the chunk if needed.
     * @param event The chunk load event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        // Check if regeneration is enabled
        if (!addon.getSettings().isRegenerateOnChunkLoad()) {
            return;
        }

        // Check if FAWE is available
        if (!addon.isFaweEnabled()) {
            return;
        }

        // Check if this is a BentoBox world
        if (!addon.getPlugin().getIWM().inWorld(event.getWorld())) {
            return;
        }

        Chunk chunk = event.getChunk();
        long chunkKey = getChunkKey(chunk.getX(), chunk.getZ());

        // Avoid processing the same chunk multiple times in quick succession
        if (pendingChunks.add(chunkKey)) {
            // Delay the regeneration slightly to avoid lag spikes during chunk loading
            Bukkit.getScheduler().runTaskLater(addon.getPlugin(), () -> {
                pendingChunks.remove(chunkKey);
                regenerateBarriersInChunk(chunk);
            }, 5L);
        }
    }

    /**
     * Regenerate barriers in a specific chunk.
     * @param chunk The chunk to regenerate barriers in
     */
    private void regenerateBarriersInChunk(Chunk chunk) {
        int chunkMinX = chunk.getX() << 4;
        int chunkMaxX = chunkMinX + 15;
        int chunkMinZ = chunk.getZ() << 4;
        int chunkMaxZ = chunkMinZ + 15;

        // Check center of chunk to find which island(s) it belongs to
        Location chunkCenter = new Location(chunk.getWorld(),
            chunkMinX + 8, 64, chunkMinZ + 8);

        addon.getIslands().getIslandAt(chunkCenter).ifPresent(island -> {
            BorderIslandData data = addon.getDataManager().getData(island);

            if (data.isBorderEnabled()) {
                // Get the intersection of chunk and island protection area
                Location islandCenter = island.getCenter();
                int protRange = island.getProtectionRange();

                int islandMinX = islandCenter.getBlockX() - protRange;
                int islandMaxX = islandCenter.getBlockX() + protRange;
                int islandMinZ = islandCenter.getBlockZ() - protRange;
                int islandMaxZ = islandCenter.getBlockZ() + protRange;

                // Calculate the actual area to place barriers (intersection)
                int placeMinX = Math.max(chunkMinX, islandMinX);
                int placeMaxX = Math.min(chunkMaxX, islandMaxX);
                int placeMinZ = Math.max(chunkMinZ, islandMinZ);
                int placeMaxZ = Math.min(chunkMaxZ, islandMaxZ);

                // Only place if there's an actual intersection
                if (placeMinX <= placeMaxX && placeMinZ <= placeMaxZ) {
                    addon.getBarrierManager().placeBarriersInChunk(
                        chunk.getWorld(),
                        placeMinX, placeMaxX,
                        placeMinZ, placeMaxZ,
                        data
                    );
                }
            }
        });
    }

    /**
     * Create a unique key for a chunk based on its coordinates.
     * @param x Chunk X coordinate
     * @param z Chunk Z coordinate
     * @return A unique long key
     */
    private long getChunkKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
}
