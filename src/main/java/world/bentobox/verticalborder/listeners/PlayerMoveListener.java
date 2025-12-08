package world.bentobox.verticalborder.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.verticalborder.VerticalBorderAddon;
import world.bentobox.verticalborder.database.BorderIslandData;

/**
 * Listens for player movement to enforce Y-axis boundaries.
 */
public class PlayerMoveListener implements Listener {

    private final VerticalBorderAddon addon;

    /**
     * Create a new PlayerMoveListener.
     * @param addon The VerticalBorder addon instance
     */
    public PlayerMoveListener(VerticalBorderAddon addon) {
        this.addon = addon;
    }

    /**
     * Handle player movement - enforce vertical boundaries.
     * @param event The player move event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        if (to == null) {
            return;
        }

        // Only check block position changes for performance
        Location from = event.getFrom();
        if (from.getBlockX() == to.getBlockX() &&
            from.getBlockY() == to.getBlockY() &&
            from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        // Check if we're in a BentoBox world
        if (!addon.getPlugin().getIWM().inWorld(to)) {
            return;
        }

        Player player = event.getPlayer();

        // Bypass for admins with bypass permission
        if (player.hasPermission("verticalborder.bypass")) {
            return;
        }

        // Get the island at the destination
        Island island = addon.getIslands().getIslandAt(to).orElse(null);
        if (island == null) {
            return;
        }

        // Get border data for this island
        BorderIslandData data = addon.getDataManager().getData(island);
        if (!data.isBorderEnabled()) {
            return;
        }

        int playerY = to.getBlockY();
        User user = User.getInstance(player);

        // Check ceiling
        if (data.isCeilingEnabled() && playerY >= data.getTopY()) {
            if (addon.getSettings().isTeleportBack()) {
                Location safe = to.clone();
                safe.setY(data.getTopY() - addon.getSettings().getTeleportDistance());
                event.setTo(safe);
            }
            user.sendMessage("verticalborder.messages.hit-ceiling");
            return;
        }

        // Check floor
        if (data.isFloorEnabled() && playerY <= data.getBottomY()) {
            if (addon.getSettings().isTeleportBack()) {
                Location safe = to.clone();
                safe.setY(data.getBottomY() + addon.getSettings().getTeleportDistance());
                event.setTo(safe);
            }
            user.sendMessage("verticalborder.messages.hit-floor");
        }
    }
}
