package world.bentobox.verticalborder.commands.admin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.verticalborder.VerticalBorderAddon;
import world.bentobox.verticalborder.database.BorderIslandData;

/**
 * Admin command to view border information for a player's island.
 * Usage: /[gamemode] admin vb info <player>
 */
public class InfoCommand extends CompositeCommand {

    /**
     * Create the info command.
     * @param addon The VerticalBorder addon instance
     * @param parent The parent command
     */
    public InfoCommand(VerticalBorderAddon addon, CompositeCommand parent) {
        super(addon, parent, "info", "i");
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
        setPermission("admin.verticalborder.info");
        setParametersHelp("verticalborder.admin.info.parameters");
        setDescription("verticalborder.admin.info.description");
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

        // Display information
        user.sendMessage("verticalborder.admin.info.header", TextVariables.NAME, args.get(0));
        user.sendMessage("verticalborder.admin.info.enabled",
            "[status]", data.isBorderEnabled() ? user.getTranslation("verticalborder.enabled") : user.getTranslation("verticalborder.disabled"));
        user.sendMessage("verticalborder.admin.info.ceiling",
            "[y]", String.valueOf(data.getTopY()),
            "[status]", data.isCeilingEnabled() ? user.getTranslation("verticalborder.enabled") : user.getTranslation("verticalborder.disabled"));
        user.sendMessage("verticalborder.admin.info.floor",
            "[y]", String.valueOf(data.getBottomY()),
            "[status]", data.isFloorEnabled() ? user.getTranslation("verticalborder.enabled") : user.getTranslation("verticalborder.disabled"));

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
