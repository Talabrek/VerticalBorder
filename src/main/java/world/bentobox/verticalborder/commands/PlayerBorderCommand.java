package world.bentobox.verticalborder.commands;

import java.util.List;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.verticalborder.VerticalBorderAddon;
import world.bentobox.verticalborder.database.BorderIslandData;

/**
 * Player command to view and toggle their island's vertical border.
 * Usage: /[gamemode] border
 */
public class PlayerBorderCommand extends CompositeCommand {

    /**
     * Create the player border command.
     * @param addon The VerticalBorder addon instance
     * @param parent The parent player command
     */
    public PlayerBorderCommand(VerticalBorderAddon addon, CompositeCommand parent) {
        super(addon, parent, "border", "vborder");
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
        setPermission("border");
        setDescription("verticalborder.player.description");
        setOnlyPlayer(true);
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        // Get the player's island
        Island island = getIslands().getIsland(getWorld(), user.getUniqueId());
        if (island == null) {
            user.sendMessage("general.errors.no-island");
            return false;
        }

        // Check if user is a member of the island
        if (!island.getMemberSet().contains(user.getUniqueId())) {
            user.sendMessage("general.errors.not-a-member");
            return false;
        }

        // Get border data
        BorderIslandData data = getVerticalBorderAddon().getDataManager().getData(island);

        // If no arguments, show current status
        if (args.isEmpty()) {
            showBorderInfo(user, data);
            return true;
        }

        // Handle toggle argument
        String arg = args.get(0).toLowerCase();
        switch (arg) {
            case "toggle":
                return toggleBorder(user, island, data);
            case "info":
                showBorderInfo(user, data);
                return true;
            default:
                showHelp(this, user);
                return false;
        }
    }

    /**
     * Show border information to the player.
     * @param user The user to show info to
     * @param data The border data
     */
    private void showBorderInfo(User user, BorderIslandData data) {
        user.sendMessage("verticalborder.player.info.header");
        user.sendMessage("verticalborder.player.info.enabled",
            "[status]", data.isBorderEnabled() ?
                user.getTranslation("verticalborder.enabled") :
                user.getTranslation("verticalborder.disabled"));
        user.sendMessage("verticalborder.player.info.ceiling",
            "[y]", String.valueOf(data.getTopY()));
        user.sendMessage("verticalborder.player.info.floor",
            "[y]", String.valueOf(data.getBottomY()));
    }

    /**
     * Toggle the border for the player's island.
     * @param user The user toggling the border
     * @param island The player's island
     * @param data The border data
     * @return true if successful
     */
    private boolean toggleBorder(User user, Island island, BorderIslandData data) {
        boolean newStatus = !data.isBorderEnabled();
        data.setBorderEnabled(newStatus);

        // Save data
        getVerticalBorderAddon().getDataManager().saveData(data);

        // Update barriers
        if (newStatus) {
            getVerticalBorderAddon().getBarrierManager().createBordersForIsland(island, data);
        } else {
            getVerticalBorderAddon().getBarrierManager().removeBordersForIsland(island, data);
        }

        // Send message
        String statusText = newStatus ?
            user.getTranslation("verticalborder.enabled") :
            user.getTranslation("verticalborder.disabled");
        user.sendMessage("verticalborder.player.toggle.success", "[status]", statusText);

        return true;
    }
}
