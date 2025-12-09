package world.bentobox.verticalborder.managers;

import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;

import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.verticalborder.VerticalBorderAddon;
import world.bentobox.verticalborder.database.BorderIslandData;

/**
 * Manages barrier block placement and removal using FAWE.
 */
public class FAWEBarrierManager {

    private final VerticalBorderAddon addon;

    /**
     * Create a new FAWEBarrierManager.
     * @param addon The VerticalBorder addon instance
     */
    public FAWEBarrierManager(VerticalBorderAddon addon) {
        this.addon = addon;
    }

    /**
     * Place a horizontal barrier plane at a specific Y level.
     * @param bukkitWorld The world to place barriers in
     * @param minX Minimum X coordinate
     * @param maxX Maximum X coordinate
     * @param minZ Minimum Z coordinate
     * @param maxZ Maximum Z coordinate
     * @param y The Y level to place barriers at
     * @return CompletableFuture with the number of blocks changed
     */
    public CompletableFuture<Integer> placeHorizontalBarrierPlane(
            World bukkitWorld, int minX, int maxX, int minZ, int maxZ, int y) {

        if (!addon.isFaweEnabled() || !addon.getSettings().isPlaceBarrierBlocks()) {
            return CompletableFuture.completedFuture(0);
        }

        CompletableFuture<Integer> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(addon.getPlugin(), () -> {
            try {
                com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(bukkitWorld);
                BlockVector3 min = BlockVector3.at(minX, y, minZ);
                BlockVector3 max = BlockVector3.at(maxX, y, maxZ);
                CuboidRegion region = new CuboidRegion(world, min, max);

                try (EditSession session = WorldEdit.getInstance()
                        .newEditSessionBuilder()
                        .world(world)
                        .maxBlocks(-1)
                        .build()) {

                    // Only replace air blocks with barriers
                    Mask airMask = new BlockTypeMask(session,
                            BlockTypes.AIR, BlockTypes.CAVE_AIR, BlockTypes.VOID_AIR);
                    BlockState barrier = BlockTypes.BARRIER.getDefaultState();

                    int changed = session.replaceBlocks(region, airMask, barrier);
                    future.complete(changed);
                }
            } catch (Exception e) {
                addon.logError("Error placing barrier plane: " + e.getMessage());
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Fill a 3D volume with barrier blocks (replacing only air).
     * @param bukkitWorld The world to place barriers in
     * @param minX Minimum X coordinate
     * @param maxX Maximum X coordinate
     * @param minY Minimum Y coordinate
     * @param maxY Maximum Y coordinate
     * @param minZ Minimum Z coordinate
     * @param maxZ Maximum Z coordinate
     * @return CompletableFuture with the number of blocks changed
     */
    public CompletableFuture<Integer> fillVolumeWithBarriers(
            World bukkitWorld, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {

        if (!addon.isFaweEnabled() || !addon.getSettings().isPlaceBarrierBlocks()) {
            return CompletableFuture.completedFuture(0);
        }

        CompletableFuture<Integer> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(addon.getPlugin(), () -> {
            try {
                com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(bukkitWorld);
                BlockVector3 min = BlockVector3.at(minX, minY, minZ);
                BlockVector3 max = BlockVector3.at(maxX, maxY, maxZ);
                CuboidRegion region = new CuboidRegion(world, min, max);

                try (EditSession session = WorldEdit.getInstance()
                        .newEditSessionBuilder()
                        .world(world)
                        .maxBlocks(-1)
                        .build()) {

                    // Only replace air blocks with barriers
                    Mask airMask = new BlockTypeMask(session,
                            BlockTypes.AIR, BlockTypes.CAVE_AIR, BlockTypes.VOID_AIR);
                    BlockState barrier = BlockTypes.BARRIER.getDefaultState();

                    int changed = session.replaceBlocks(region, airMask, barrier);
                    future.complete(changed);
                }
            } catch (Exception e) {
                addon.logError("Error filling volume with barriers: " + e.getMessage());
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Remove barrier blocks from a 3D volume.
     * @param bukkitWorld The world to remove barriers from
     * @param minX Minimum X coordinate
     * @param maxX Maximum X coordinate
     * @param minY Minimum Y coordinate
     * @param maxY Maximum Y coordinate
     * @param minZ Minimum Z coordinate
     * @param maxZ Maximum Z coordinate
     * @return CompletableFuture with the number of blocks changed
     */
    public CompletableFuture<Integer> removeBarriersFromVolume(
            World bukkitWorld, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {

        if (!addon.isFaweEnabled()) {
            return CompletableFuture.completedFuture(0);
        }

        CompletableFuture<Integer> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(addon.getPlugin(), () -> {
            try {
                com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(bukkitWorld);
                BlockVector3 min = BlockVector3.at(minX, minY, minZ);
                BlockVector3 max = BlockVector3.at(maxX, maxY, maxZ);
                CuboidRegion region = new CuboidRegion(world, min, max);

                try (EditSession session = WorldEdit.getInstance()
                        .newEditSessionBuilder()
                        .world(world)
                        .maxBlocks(-1)
                        .build()) {

                    // Only remove barrier blocks
                    Mask barrierMask = new BlockTypeMask(session, BlockTypes.BARRIER);
                    BlockState air = BlockTypes.AIR.getDefaultState();

                    int changed = session.replaceBlocks(region, barrierMask, air);
                    future.complete(changed);
                }
            } catch (Exception e) {
                addon.logError("Error removing barriers from volume: " + e.getMessage());
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Remove barrier blocks from a horizontal plane at a specific Y level.
     * @param bukkitWorld The world to remove barriers from
     * @param minX Minimum X coordinate
     * @param maxX Maximum X coordinate
     * @param minZ Minimum Z coordinate
     * @param maxZ Maximum Z coordinate
     * @param y The Y level to remove barriers from
     * @return CompletableFuture with the number of blocks changed
     */
    public CompletableFuture<Integer> removeBarrierPlane(
            World bukkitWorld, int minX, int maxX, int minZ, int maxZ, int y) {

        if (!addon.isFaweEnabled()) {
            return CompletableFuture.completedFuture(0);
        }

        CompletableFuture<Integer> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(addon.getPlugin(), () -> {
            try {
                com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(bukkitWorld);
                BlockVector3 min = BlockVector3.at(minX, y, minZ);
                BlockVector3 max = BlockVector3.at(maxX, y, maxZ);
                CuboidRegion region = new CuboidRegion(world, min, max);

                try (EditSession session = WorldEdit.getInstance()
                        .newEditSessionBuilder()
                        .world(world)
                        .maxBlocks(-1)
                        .build()) {

                    // Only remove barrier blocks
                    Mask barrierMask = new BlockTypeMask(session, BlockTypes.BARRIER);
                    BlockState air = BlockTypes.AIR.getDefaultState();

                    int changed = session.replaceBlocks(region, barrierMask, air);
                    future.complete(changed);
                }
            } catch (Exception e) {
                addon.logError("Error removing barrier plane: " + e.getMessage());
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Create barriers for an island.
     * Fills all air blocks above the ceiling (to world max) and below the floor (to world min)
     * with barrier blocks to prevent access and mob spawning.
     * @param island The island to create barriers for
     * @param data The border data for the island
     */
    public void createBordersForIsland(Island island, BorderIslandData data) {
        if (!addon.isFaweEnabled() || !addon.getSettings().isPlaceBarrierBlocks()) {
            return;
        }

        if (!data.isBorderEnabled()) {
            return;
        }

        World world = island.getWorld();
        Location center = island.getCenter();
        int range = island.getProtectionRange();

        int minX = center.getBlockX() - range;
        int maxX = center.getBlockX() + range;
        int minZ = center.getBlockZ() - range;
        int maxZ = center.getBlockZ() + range;

        // World height limits
        int worldMinY = world.getMinHeight();
        int worldMaxY = world.getMaxHeight() - 1;

        // Fill volume above the ceiling (from topY to world max)
        if (data.isCeilingEnabled()) {
            fillVolumeWithBarriers(world, minX, maxX, data.getTopY(), worldMaxY, minZ, maxZ)
                .thenAccept(count -> {
                    if (count > 0) {
                        addon.log("Placed " + count + " ceiling barriers for island " + island.getUniqueId());
                    }
                });
        }

        // Fill volume below the floor (from world min to bottomY)
        if (data.isFloorEnabled()) {
            fillVolumeWithBarriers(world, minX, maxX, worldMinY, data.getBottomY(), minZ, maxZ)
                .thenAccept(count -> {
                    if (count > 0) {
                        addon.log("Placed " + count + " floor barriers for island " + island.getUniqueId());
                    }
                });
        }
    }

    /**
     * Remove barriers for an island.
     * Removes all barrier blocks from the volumes above the ceiling and below the floor.
     * @param island The island to remove barriers from
     * @param data The border data for the island
     */
    public void removeBordersForIsland(Island island, BorderIslandData data) {
        if (!addon.isFaweEnabled()) {
            return;
        }

        World world = island.getWorld();
        Location center = island.getCenter();
        int range = island.getProtectionRange();

        int minX = center.getBlockX() - range;
        int maxX = center.getBlockX() + range;
        int minZ = center.getBlockZ() - range;
        int maxZ = center.getBlockZ() + range;

        // World height limits
        int worldMinY = world.getMinHeight();
        int worldMaxY = world.getMaxHeight() - 1;

        // Remove ceiling barriers (from topY to world max)
        removeBarriersFromVolume(world, minX, maxX, data.getTopY(), worldMaxY, minZ, maxZ)
            .thenAccept(count -> {
                if (count > 0) {
                    addon.log("Removed " + count + " ceiling barriers for island " + island.getUniqueId());
                }
            });

        // Remove floor barriers (from world min to bottomY)
        removeBarriersFromVolume(world, minX, maxX, worldMinY, data.getBottomY(), minZ, maxZ)
            .thenAccept(count -> {
                if (count > 0) {
                    addon.log("Removed " + count + " floor barriers for island " + island.getUniqueId());
                }
            });
    }

    /**
     * Update barriers for an island (removes old and places new).
     * Handles height adjustments by removing old volume and placing new volume.
     * @param island The island to update barriers for
     * @param data The border data for the island
     * @param oldTopY The old ceiling Y level (for removal)
     * @param oldBottomY The old floor Y level (for removal)
     */
    public void updateBordersForIsland(Island island, BorderIslandData data, int oldTopY, int oldBottomY) {
        if (!addon.isFaweEnabled()) {
            return;
        }

        World world = island.getWorld();
        Location center = island.getCenter();
        int range = island.getProtectionRange();

        int minX = center.getBlockX() - range;
        int maxX = center.getBlockX() + range;
        int minZ = center.getBlockZ() - range;
        int maxZ = center.getBlockZ() + range;

        // World height limits
        int worldMinY = world.getMinHeight();
        int worldMaxY = world.getMaxHeight() - 1;

        // Update ceiling barriers if height changed
        if (data.isCeilingEnabled() && oldTopY != data.getTopY()) {
            // Remove old ceiling volume (from oldTopY to world max)
            removeBarriersFromVolume(world, minX, maxX, oldTopY, worldMaxY, minZ, maxZ)
                .thenRun(() -> {
                    // Place new ceiling volume (from new topY to world max)
                    fillVolumeWithBarriers(world, minX, maxX, data.getTopY(), worldMaxY, minZ, maxZ);
                });
        }

        // Update floor barriers if height changed
        if (data.isFloorEnabled() && oldBottomY != data.getBottomY()) {
            // Remove old floor volume (from world min to oldBottomY)
            removeBarriersFromVolume(world, minX, maxX, worldMinY, oldBottomY, minZ, maxZ)
                .thenRun(() -> {
                    // Place new floor volume (from world min to new bottomY)
                    fillVolumeWithBarriers(world, minX, maxX, worldMinY, data.getBottomY(), minZ, maxZ);
                });
        }
    }

    /**
     * Update barriers when an island has been moved to a new location.
     * Removes barriers from the old location and places them at the new location.
     * Y coordinates are preserved from the existing BorderIslandData.
     *
     * @param island The island (now at new location)
     * @param data The border data for the island
     * @param oldCenterX The old center X coordinate
     * @param oldCenterZ The old center Z coordinate
     * @param oldProtectionRange The old protection range
     * @return CompletableFuture that completes when the update is done
     */
    public CompletableFuture<Void> updateBorderLocation(Island island, BorderIslandData data,
            int oldCenterX, int oldCenterZ, int oldProtectionRange) {

        if (!addon.isFaweEnabled()) {
            return CompletableFuture.completedFuture(null);
        }

        if (!data.isBorderEnabled()) {
            return CompletableFuture.completedFuture(null);
        }

        World world = island.getWorld();

        // World height limits
        int worldMinY = world.getMinHeight();
        int worldMaxY = world.getMaxHeight() - 1;

        // Calculate old location bounds
        int oldMinX = oldCenterX - oldProtectionRange;
        int oldMaxX = oldCenterX + oldProtectionRange;
        int oldMinZ = oldCenterZ - oldProtectionRange;
        int oldMaxZ = oldCenterZ + oldProtectionRange;

        // Calculate new location bounds from current island data
        Location newCenter = island.getCenter();
        int newRange = island.getProtectionRange();
        int newMinX = newCenter.getBlockX() - newRange;
        int newMaxX = newCenter.getBlockX() + newRange;
        int newMinZ = newCenter.getBlockZ() - newRange;
        int newMaxZ = newCenter.getBlockZ() + newRange;

        CompletableFuture<Void> result = new CompletableFuture<>();

        // First, remove barriers from old location (volumes)
        CompletableFuture<Void> removalFuture = CompletableFuture.allOf();

        if (data.isCeilingEnabled()) {
            CompletableFuture<Integer> ceilingRemoval = removeBarriersFromVolume(world, oldMinX, oldMaxX, data.getTopY(), worldMaxY, oldMinZ, oldMaxZ);
            ceilingRemoval.thenAccept(count -> {
                if (count > 0) {
                    addon.log("Removed " + count + " ceiling barriers from old location for island " + island.getUniqueId());
                }
            });
            removalFuture = CompletableFuture.allOf(removalFuture, ceilingRemoval);
        }

        if (data.isFloorEnabled()) {
            CompletableFuture<Integer> floorRemoval = removeBarriersFromVolume(world, oldMinX, oldMaxX, worldMinY, data.getBottomY(), oldMinZ, oldMaxZ);
            floorRemoval.thenAccept(count -> {
                if (count > 0) {
                    addon.log("Removed " + count + " floor barriers from old location for island " + island.getUniqueId());
                }
            });
            removalFuture = CompletableFuture.allOf(removalFuture, floorRemoval);
        }

        // After removal completes, place barriers at new location (volumes)
        removalFuture.thenRun(() -> {
            if (data.isCeilingEnabled()) {
                fillVolumeWithBarriers(world, newMinX, newMaxX, data.getTopY(), worldMaxY, newMinZ, newMaxZ)
                    .thenAccept(count -> {
                        if (count > 0) {
                            addon.log("Placed " + count + " ceiling barriers at new location for island " + island.getUniqueId());
                        }
                    });
            }

            if (data.isFloorEnabled()) {
                fillVolumeWithBarriers(world, newMinX, newMaxX, worldMinY, data.getBottomY(), newMinZ, newMaxZ)
                    .thenAccept(count -> {
                        if (count > 0) {
                            addon.log("Placed " + count + " floor barriers at new location for island " + island.getUniqueId());
                        }
                    });
            }

            result.complete(null);
        }).exceptionally(ex -> {
            addon.logError("Error updating border location: " + ex.getMessage());
            result.completeExceptionally(ex);
            return null;
        });

        return result;
    }

    /**
     * Place barriers for a specific chunk region within an island.
     * Fills volumes above ceiling and below floor within the chunk bounds.
     * @param world The world
     * @param chunkMinX Chunk minimum X coordinate
     * @param chunkMaxX Chunk maximum X coordinate
     * @param chunkMinZ Chunk minimum Z coordinate
     * @param chunkMaxZ Chunk maximum Z coordinate
     * @param data The border data
     */
    public void placeBarriersInChunk(World world, int chunkMinX, int chunkMaxX, int chunkMinZ, int chunkMaxZ, BorderIslandData data) {
        if (!addon.isFaweEnabled() || !addon.getSettings().isPlaceBarrierBlocks()) {
            return;
        }

        if (!data.isBorderEnabled()) {
            return;
        }

        // World height limits
        int worldMinY = world.getMinHeight();
        int worldMaxY = world.getMaxHeight() - 1;

        // Fill volume above ceiling
        if (data.isCeilingEnabled()) {
            fillVolumeWithBarriers(world, chunkMinX, chunkMaxX, data.getTopY(), worldMaxY, chunkMinZ, chunkMaxZ);
        }

        // Fill volume below floor
        if (data.isFloorEnabled()) {
            fillVolumeWithBarriers(world, chunkMinX, chunkMaxX, worldMinY, data.getBottomY(), chunkMinZ, chunkMaxZ);
        }
    }
}
