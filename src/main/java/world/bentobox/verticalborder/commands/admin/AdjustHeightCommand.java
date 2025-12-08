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
 * Admin command to adjust vertical border heights by adding or subtracting.
 * Usage: /[gamemode] admin vb adjustheight <player> <top|bottom> <adjustment>
 *
 * Example: /bsbadmin vb adjustheight Talabrek top 20 (raises ceiling by 20)
 * Example: /bsbadmin vb adjustheight Talabrek bottom -10 (lowers floor by 10)
 */
public class AdjustHeightCommand extends CompositeCommand {

    /**
     * Create the adjustheight command.
     * @param addon The VerticalBorder addon instance
     * @param parent The parent command
     */
    public AdjustHeightCommand(VerticalBorderAddon addon, CompositeCommand parent) {
        super(addon, parent, "adjustheight", "ah", "adj");
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
        setPermission("admin.verticalborder.adjustheight");
        setParametersHelp("verticalborder.admin.adjustheight.parameters");
        setDescription("verticalborder.admin.adjustheight.description");
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
            user.sendMessage("verticalborder.admin.adjustheight.invalid-type");
            return false;
        }

        // Parse adjustment value
        int adjustment;
        try {
            adjustment = Integer.parseInt(args.get(2));
        } catch (NumberFormatException e) {
            user.sendMessage("verticalborder.admin.adjustheight.invalid-number");
            return false;
        }

        // Get or create border data
        BorderIslandData data = getVerticalBorderAddon().getDataManager().getData(island);
        int oldTopY = data.getTopY();
        int oldBottomY = data.getBottomY();

        int newValue;
        if (heightType.equals("top")) {
            newValue = data.getTopY() + adjustment;

            // Validate bounds
            if (newValue < -64 || newValue > 320) {
                user.sendMessage("verticalborder.admin.adjustheight.out-of-bounds",
                    "[current]", String.valueOf(data.getTopY()),
                    "[adjustment]", String.valueOf(adjustment),
                    "[result]", String.valueOf(newValue));
                return false;
            }

            // Validate top is above bottom
            if (newValue <= data.getBottomY()) {
                user.sendMessage("verticalborder.admin.setheight.top-below-bottom");
                return false;
            }

            data.setTopY(newValue);
        } else {
            newValue = data.getBottomY() + adjustment;

            // Validate bounds
            if (newValue < -64 || newValue > 320) {
                user.sendMessage("verticalborder.admin.adjustheight.out-of-bounds",
                    "[current]", String.valueOf(data.getBottomY()),
                    "[adjustment]", String.valueOf(adjustment),
                    "[result]", String.valueOf(newValue));
                return false;
            }

            // Validate bottom is below top
            if (newValue >= data.getTopY()) {
                user.sendMessage("verticalborder.admin.setheight.bottom-above-top");
                return false;
            }

            data.setBottomY(newValue);
        }

        // Save data
        getVerticalBorderAddon().getDataManager().saveData(data);

        // Update barriers
        getVerticalBorderAddon().getBarrierManager().updateBordersForIsland(island, data, oldTopY, oldBottomY);

        // Send success message
        String adjustmentStr = adjustment >= 0 ? "+" + adjustment : String.valueOf(adjustment);
        user.sendMessage("verticalborder.admin.adjustheight.success",
            "[type]", heightType,
            "[adjustment]", adjustmentStr,
            "[new_value]", String.valueOf(newValue),
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
            return Optional.of(Arrays.asList("-50", "-20", "-10", "10", "20", "50"));
        }
        return Optional.empty();
    }
}
