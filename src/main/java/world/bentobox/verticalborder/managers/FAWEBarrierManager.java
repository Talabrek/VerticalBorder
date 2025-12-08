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
     * Create barriers for an island (ceiling and floor).
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

        // Place ceiling barrier
        if (data.isCeilingEnabled()) {
            placeHorizontalBarrierPlane(world, minX, maxX, minZ, maxZ, data.getTopY())
                .thenAccept(count -> {
                    if (count > 0) {
                        addon.log("Placed " + count + " ceiling barriers for island " + island.getUniqueId());
                    }
                });
        }

        // Place floor barrier
        if (data.isFloorEnabled()) {
            placeHorizontalBarrierPlane(world, minX, maxX, minZ, maxZ, data.getBottomY())
                .thenAccept(count -> {
                    if (count > 0) {
                        addon.log("Placed " + count + " floor barriers for island " + island.getUniqueId());
                    }
                });
        }
    }

    /**
     * Remove barriers for an island (ceiling and floor).
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

        // Remove ceiling barrier
        if (data.isCeilingEnabled()) {
            removeBarrierPlane(world, minX, maxX, minZ, maxZ, data.getTopY())
                .thenAccept(count -> {
                    if (count > 0) {
                        addon.log("Removed " + count + " ceiling barriers for island " + island.getUniqueId());
                    }
                });
        }

        // Remove floor barrier
        if (data.isFloorEnabled()) {
            removeBarrierPlane(world, minX, maxX, minZ, maxZ, data.getBottomY())
                .thenAccept(count -> {
                    if (count > 0) {
                        addon.log("Removed " + count + " floor barriers for island " + island.getUniqueId());
                    }
                });
        }
    }

    /**
     * Update barriers for an island (removes old and places new).
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

        // Remove old barriers and place new ones
        if (data.isCeilingEnabled() && oldTopY != data.getTopY()) {
            removeBarrierPlane(world, minX, maxX, minZ, maxZ, oldTopY)
                .thenRun(() -> placeHorizontalBarrierPlane(world, minX, maxX, minZ, maxZ, data.getTopY()));
        }

        if (data.isFloorEnabled() && oldBottomY != data.getBottomY()) {
            removeBarrierPlane(world, minX, maxX, minZ, maxZ, oldBottomY)
                .thenRun(() -> placeHorizontalBarrierPlane(world, minX, maxX, minZ, maxZ, data.getBottomY()));
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

        // First, remove barriers from old location
        CompletableFuture<Void> removalFuture = CompletableFuture.allOf();

        if (data.isCeilingEnabled()) {
            CompletableFuture<Integer> ceilingRemoval = removeBarrierPlane(world, oldMinX, oldMaxX, oldMinZ, oldMaxZ, data.getTopY());
            ceilingRemoval.thenAccept(count -> {
                if (count > 0) {
                    addon.log("Removed " + count + " ceiling barriers from old location for island " + island.getUniqueId());
                }
            });
            removalFuture = CompletableFuture.allOf(removalFuture, ceilingRemoval);
        }

        if (data.isFloorEnabled()) {
            CompletableFuture<Integer> floorRemoval = removeBarrierPlane(world, oldMinX, oldMaxX, oldMinZ, oldMaxZ, data.getBottomY());
            floorRemoval.thenAccept(count -> {
                if (count > 0) {
                    addon.log("Removed " + count + " floor barriers from old location for island " + island.getUniqueId());
                }
            });
            removalFuture = CompletableFuture.allOf(removalFuture, floorRemoval);
        }

        // After removal completes, place barriers at new location
        removalFuture.thenRun(() -> {
            if (data.isCeilingEnabled()) {
                placeHorizontalBarrierPlane(world, newMinX, newMaxX, newMinZ, newMaxZ, data.getTopY())
                    .thenAccept(count -> {
                        if (count > 0) {
                            addon.log("Placed " + count + " ceiling barriers at new location for island " + island.getUniqueId());
                        }
                    });
            }

            if (data.isFloorEnabled()) {
                placeHorizontalBarrierPlane(world, newMinX, newMaxX, newMinZ, newMaxZ, data.getBottomY())
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

        if (data.isCeilingEnabled()) {
            placeHorizontalBarrierPlane(world, chunkMinX, chunkMaxX, chunkMinZ, chunkMaxZ, data.getTopY());
        }

        if (data.isFloorEnabled()) {
            placeHorizontalBarrierPlane(world, chunkMinX, chunkMaxX, chunkMinZ, chunkMaxZ, data.getBottomY());
        }
    }
}
