package world.bentobox.verticalborder.placeholders;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.verticalborder.VerticalBorderAddon;
import world.bentobox.verticalborder.database.BorderIslandData;

/**
 * PlaceholderAPI expansion for VerticalBorder addon.
 *
 * Provides the following placeholders:
 * - %verticalborder_top_y% - Returns the top Y boundary for the player's island
 * - %verticalborder_bottom_y% - Returns the bottom Y boundary for the player's island
 * - %verticalborder_ceiling_enabled% - Returns whether ceiling is enabled
 * - %verticalborder_floor_enabled% - Returns whether floor is enabled
 * - %verticalborder_border_enabled% - Returns whether border is enabled
 */
public class VerticalBorderPlaceholders extends PlaceholderExpansion {

    private final VerticalBorderAddon addon;

    public VerticalBorderPlaceholders(VerticalBorderAddon addon) {
        this.addon = addon;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "verticalborder";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", addon.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return addon.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        // This will keep the expansion registered until the server shuts down
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        // Get the island the player is on or owns
        Island island = addon.getIslands().getIsland(player.getWorld(), player.getUniqueId());

        // If player doesn't own an island, check if they're standing on one
        if (island == null) {
            island = addon.getIslands().getIslandAt(player.getLocation()).orElse(null);
        }

        // No island found - return defaults or empty
        if (island == null) {
            return getDefaultValue(params);
        }

        // Get the border data for this island
        BorderIslandData data = addon.getDataManager().getData(island);
        if (data == null) {
            return getDefaultValue(params);
        }

        // Handle different placeholder parameters
        return switch (params.toLowerCase()) {
            case "top_y" -> String.valueOf(data.getTopY());
            case "bottom_y" -> String.valueOf(data.getBottomY());
            case "ceiling_enabled" -> String.valueOf(data.isCeilingEnabled());
            case "floor_enabled" -> String.valueOf(data.isFloorEnabled());
            case "border_enabled" -> String.valueOf(data.isBorderEnabled());
            case "height_range" -> String.valueOf(data.getTopY() - data.getBottomY());
            default -> null;
        };
    }

    /**
     * Returns default values when no island is found.
     * @param params The placeholder parameter
     * @return Default value or empty string
     */
    private String getDefaultValue(String params) {
        return switch (params.toLowerCase()) {
            case "top_y" -> String.valueOf(addon.getSettings().getDefaultTopY());
            case "bottom_y" -> String.valueOf(addon.getSettings().getDefaultBottomY());
            case "ceiling_enabled" -> String.valueOf(addon.getSettings().isDefaultCeilingEnabled());
            case "floor_enabled" -> String.valueOf(addon.getSettings().isDefaultFloorEnabled());
            case "border_enabled" -> "false";
            case "height_range" -> String.valueOf(
                addon.getSettings().getDefaultTopY() - addon.getSettings().getDefaultBottomY());
            default -> "";
        };
    }
}
