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
 * Admin command to set vertical border heights for a player's island.
 * Usage: /[gamemode] admin vb setheight <player> <top|bottom> <value>
 */
public class SetHeightCommand extends CompositeCommand {

    /**
     * Create the setheight command.
     * @param addon The VerticalBorder addon instance
     * @param parent The parent command
     */
    public SetHeightCommand(VerticalBorderAddon addon, CompositeCommand parent) {
        super(addon, parent, "setheight", "sh");
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
        setPermission("admin.verticalborder.setheight");
        setParametersHelp("verticalborder.admin.setheight.parameters");
        setDescription("verticalborder.admin.setheight.description");
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        if (args.size() < 3) {
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

        // Parse height type
        String heightType = args.get(1).toLowerCase();
        if (!heightType.equals("top") && !heightType.equals("bottom")) {
            user.sendMessage("verticalborder.admin.setheight.invalid-type");
            return false;
        }

        // Parse value
        int value;
        try {
            value = Integer.parseInt(args.get(2));
        } catch (NumberFormatException e) {
            user.sendMessage("verticalborder.admin.setheight.invalid-number");
            return false;
        }

        // Validate Y value bounds
        if (value < -64 || value > 320) {
            user.sendMessage("verticalborder.admin.setheight.out-of-bounds");
            return false;
        }

        // Get or create border data
        BorderIslandData data = getVerticalBorderAddon().getDataManager().getData(island);
        int oldTopY = data.getTopY();
        int oldBottomY = data.getBottomY();

        // Update the appropriate height
        if (heightType.equals("top")) {
            // Validate top is above bottom
            if (value <= data.getBottomY()) {
                user.sendMessage("verticalborder.admin.setheight.top-below-bottom");
                return false;
            }
            data.setTopY(value);
        } else {
            // Validate bottom is below top
            if (value >= data.getTopY()) {
                user.sendMessage("verticalborder.admin.setheight.bottom-above-top");
                return false;
            }
            data.setBottomY(value);
        }

        // Save data
        getVerticalBorderAddon().getDataManager().saveData(data);

        // Update barriers
        getVerticalBorderAddon().getBarrierManager().updateBordersForIsland(island, data, oldTopY, oldBottomY);

        // Send success message
        user.sendMessage("verticalborder.admin.setheight.success",
            "[type]", heightType,
            "[value]", String.valueOf(value),
            TextVariables.NAME, args.get(0));

        return true;
    }

    @Override
    public Optional<List<String>> tabComplete(User user, String alias, List<String> args) {
        if (args.size() == 1) {
            return Optional.of(Util.tabLimit(Util.getOnlinePlayerList(user), args.get(0)));
        }
        if (args.size() == 2) {
            return Optional.of(Util.tabLimit(Arrays.asList("top", "bottom"), args.get(1)));
        }
        if (args.size() == 3) {
            return Optional.of(Arrays.asList("-64", "0", "64", "128", "192", "256", "320"));
        }
        return Optional.empty();
    }
}
