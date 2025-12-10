package world.bentobox.verticalborder.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import world.bentobox.bentobox.api.events.island.IslandCreatedEvent;
import world.bentobox.bentobox.api.events.island.IslandDeletedEvent;
import world.bentobox.bentobox.api.events.island.IslandResettedEvent;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.verticalborder.VerticalBorderAddon;
import world.bentobox.verticalborder.database.BorderIslandData;

/**
 * Listens for island lifecycle events to manage border data and barriers.
 */
public class IslandEventListener implements Listener {

    private final VerticalBorderAddon addon;

    /**
     * Create a new IslandEventListener.
     * @param addon The VerticalBorder addon instance
     */
    public IslandEventListener(VerticalBorderAddon addon) {
        this.addon = addon;
    }

    /**
     * Handle island creation - create default border data and place barriers.
     * @param event The island created event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandCreated(IslandCreatedEvent event) {
        Island island = event.getIsland();

        // Create new border data with defaults from config
        BorderIslandData data = new BorderIslandData(
            island.getUniqueId(),
            addon.getSettings().getDefaultTopY(),
            addon.getSettings().getDefaultBottomY()
        );
        data.setCeilingEnabled(addon.getSettings().isDefaultCeilingEnabled());
        data.setFloorEnabled(addon.getSettings().isDefaultFloorEnabled());

        // Store the current island location for future reference
        data.updateLocation(
            island.getCenter().getBlockX(),
            island.getCenter().getBlockZ(),
            island.getProtectionRange()
        );

        // Save the data
        addon.getDataManager().saveData(data);

        // Place barriers
        addon.getBarrierManager().createBordersForIsland(island, data);

        addon.log("Created vertical border for new island: " + island.getUniqueId());
    }

    /**
     * Handle island deletion - remove barriers and delete border data.
     * @param event The island deleted event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandDeleted(IslandDeletedEvent event) {
        Island island = event.getIsland();
        String islandId = island.getUniqueId();

        // Get existing data (if any)
        BorderIslandData data = addon.getDataManager().getDataById(islandId);

        if (data != null) {
            // Remove barriers before deleting data
            addon.getBarrierManager().removeBordersForIsland(island, data);

            // Delete the data
            addon.getDataManager().deleteData(islandId);

            addon.log("Removed vertical border for deleted island: " + islandId);
        }
    }

    /**
     * Handle island reset - recreate borders with default settings.
     * @param event The island reset event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandReset(IslandResettedEvent event) {
        Island island = event.getIsland();
        String islandId = island.getUniqueId();

        // Create new border data with defaults from config
        BorderIslandData newData = new BorderIslandData(
            islandId,
            addon.getSettings().getDefaultTopY(),
            addon.getSettings().getDefaultBottomY()
        );
        newData.setCeilingEnabled(addon.getSettings().isDefaultCeilingEnabled());
        newData.setFloorEnabled(addon.getSettings().isDefaultFloorEnabled());

        // Store the current island location for future reference
        newData.updateLocation(
            island.getCenter().getBlockX(),
            island.getCenter().getBlockZ(),
            island.getProtectionRange()
        );

        // Get old data if it exists to remove old barriers
        BorderIslandData oldData = addon.getDataManager().getDataById(islandId);
        if (oldData != null) {
            // Wait for removal to complete before placing new barriers
            addon.getBarrierManager().removeBordersForIsland(island, oldData)
                .thenCompose(v -> {
                    // Save the new data and place new barriers
                    addon.getDataManager().saveData(newData);
                    return addon.getBarrierManager().createBordersForIsland(island, newData);
                })
                .thenRun(() -> addon.log("Reset vertical border for island: " + islandId));
        } else {
            // No old data, just save and create new barriers
            addon.getDataManager().saveData(newData);
            addon.getBarrierManager().createBordersForIsland(island, newData)
                .thenRun(() -> addon.log("Reset vertical border for island: " + islandId));
        }
    }
}
