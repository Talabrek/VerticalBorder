package world.bentobox.verticalborder.commands.admin;

import java.util.Arrays;
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
 * Admin command to toggle border settings for a player's island.
 * Usage: /[gamemode] admin vb toggle <player> <all|ceiling|floor>
 */
public class ToggleCommand extends CompositeCommand {

    /**
     * Create the toggle command.
     * @param addon The VerticalBorder addon instance
     * @param parent The parent command
     */
    public ToggleCommand(VerticalBorderAddon addon, CompositeCommand parent) {
        super(addon, parent, "toggle", "t");
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
        setPermission("admin.verticalborder.toggle");
        setParametersHelp("verticalborder.admin.toggle.parameters");
        setDescription("verticalborder.admin.toggle.description");
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        if (args.size() < 2) {
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

        // Parse toggle type
        String toggleType = args.get(1).toLowerCase();
        boolean newStatus;
        String statusKey;

        switch (toggleType) {
            case "all":
                newStatus = !data.isBorderEnabled();
                data.setBorderEnabled(newStatus);
                statusKey = "verticalborder.admin.toggle.all";
                break;
            case "ceiling":
                newStatus = !data.isCeilingEnabled();
                data.setCeilingEnabled(newStatus);
                statusKey = "verticalborder.admin.toggle.ceiling";
                break;
            case "floor":
                newStatus = !data.isFloorEnabled();
                data.setFloorEnabled(newStatus);
                statusKey = "verticalborder.admin.toggle.floor";
                break;
            default:
                user.sendMessage("verticalborder.admin.toggle.invalid-type");
                return false;
        }

        // Save data
        getVerticalBorderAddon().getDataManager().saveData(data);

        // Update barriers if needed
        if (newStatus && data.isBorderEnabled()) {
            getVerticalBorderAddon().getBarrierManager().createBordersForIsland(island, data);
        } else if (!newStatus || !data.isBorderEnabled()) {
            getVerticalBorderAddon().getBarrierManager().removeBordersForIsland(island, data);
        }

        // Send success message
        String statusText = newStatus ? user.getTranslation("verticalborder.enabled") : user.getTranslation("verticalborder.disabled");
        user.sendMessage(statusKey + ".success",
            "[status]", statusText,
            TextVariables.NAME, args.get(0));

        return true;
    }

    @Override
    public Optional<List<String>> tabComplete(User user, String alias, List<String> args) {
        if (args.size() == 1) {
            return Optional.of(Util.tabLimit(Util.getOnlinePlayerList(user), args.get(0)));
        }
        if (args.size() == 2) {
            return Optional.of(Util.tabLimit(Arrays.asList("all", "ceiling", "floor"), args.get(1)));
        }
        return Optional.empty();
    }
}
