package world.bentobox.verticalborder.commands.admin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.verticalborder.VerticalBorderAddon;
import world.bentobox.verticalborder.database.BorderIslandData;

/**
 * Admin command to update vertical border location after an island has been moved.
 * Removes barriers from the old location and places them at the new location.
 * Y coordinates (ceiling/floor heights) are preserved.
 *
 * Usage: /[gamemode] admin vb update <player>
 */
public class UpdateCommand extends CompositeCommand {

    /**
     * Create the update command.
     * @param addon The VerticalBorder addon instance
     * @param parent The parent command
     */
    public UpdateCommand(VerticalBorderAddon addon, CompositeCommand parent) {
        super(addon, parent, "update", "u");
    }

    /**
     * Get the VerticalBorder addon instance.
     * @return The addon
     */
    private VerticalBorderAddon getVerticalBorderAddon() {
        return (VerticalBorderAddon) getAddon();
    }

    @Override
    public void setup() {
        setPermission("admin.verticalborder.update");
        setParametersHelp("verticalborder.admin.update.parameters");
        setDescription("verticalborder.admin.update.description");
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        if (args.isEmpty()) {
            showHelp(this, user);
            return false;
        }

        // Get target player
        UUID targetUUID = getPlayers().getUUID(args.get(0));
        if (targetUUID == null) {
            user.sendMessage("general.errors.unknown-player", TextVariables.NAME, args.get(0));
            return false;
        }

        // Get target's island
        Island island = getIslands().getIsland(getWorld(), targetUUID);
        if (island == null) {
            user.sendMessage("general.errors.player-has-no-island");
            return false;
        }

        // Get border data
        BorderIslandData data = getVerticalBorderAddon().getDataManager().getData(island);

        if (!data.isBorderEnabled()) {
            user.sendMessage("verticalborder.admin.update.border-disabled");
            return false;
        }

        // Get current island location from BentoBox
        Location newCenter = island.getCenter();
        int newCenterX = newCenter.getBlockX();
        int newCenterZ = newCenter.getBlockZ();
        int newRange = island.getProtectionRange();

        // Check if we have old location data
        if (!data.isLocationInitialized()) {
            // No old location data - just place barriers at current location
            user.sendMessage("verticalborder.admin.update.no-old-location");

            // Update and save location data
            data.updateLocation(newCenterX, newCenterZ, newRange);
            getVerticalBorderAddon().getDataManager().saveData(data);

            // Place barriers at current location
            getVerticalBorderAddon().getBarrierManager().createBordersForIsland(island, data);

            user.sendMessage("verticalborder.admin.update.placed-new",
                "[x]", String.valueOf(newCenterX),
                "[z]", String.valueOf(newCenterZ),
                TextVariables.NAME, args.get(0));
            return true;
        }

        // Check if location has actually changed
        int oldCenterX = data.getLastCenterX();
        int oldCenterZ = data.getLastCenterZ();
        int oldRange = data.getLastProtectionRange();

        if (oldCenterX == newCenterX && oldCenterZ == newCenterZ && oldRange == newRange) {
            user.sendMessage("verticalborder.admin.update.no-change");
            return false;
        }

        // Notify user that update is in progress
        user.sendMessage("verticalborder.admin.update.updating",
            "[old_x]", String.valueOf(oldCenterX),
            "[old_z]", String.valueOf(oldCenterZ),
            "[new_x]", String.valueOf(newCenterX),
            "[new_z]", String.valueOf(newCenterZ),
            TextVariables.NAME, args.get(0));

        // Update the border location (removes old barriers, places new ones)
        VerticalBorderAddon vbAddon = getVerticalBorderAddon();
        vbAddon.getBarrierManager().updateBorderLocation(island, data, oldCenterX, oldCenterZ, oldRange)
            .thenRun(() -> {
                // Update stored location data
                data.updateLocation(newCenterX, newCenterZ, newRange);
                vbAddon.getDataManager().saveData(data);

                // Send success message (on main thread)
                vbAddon.getPlugin().getServer().getScheduler().runTask(vbAddon.getPlugin(), () -> {
                    user.sendMessage("verticalborder.admin.update.success",
                        "[x]", String.valueOf(newCenterX),
                        "[z]", String.valueOf(newCenterZ),
                        TextVariables.NAME, args.get(0));
                });
            })
            .exceptionally(ex -> {
                // Send error message (on main thread)
                vbAddon.getPlugin().getServer().getScheduler().runTask(vbAddon.getPlugin(), () -> {
                    user.sendMessage("verticalborder.admin.update.error");
                });
                return null;
            });

        return true;
    }

    @Override
    public Optional<List<String>> tabComplete(User user, String alias, List<String> args) {
        if (args.size() == 1) {
            return Optional.of(Util.tabLimit(Util.getOnlinePlayerList(user), args.get(0)));
        }
        return Optional.empty();
    }
}
