package world.bentobox.verticalborder.placeholders;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
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
 *
 * Self-referencing (uses requesting player's island):
 * - %verticalborder_top_y% - Returns the top Y boundary for the player's island
 * - %verticalborder_bottom_y% - Returns the bottom Y boundary for the player's island
 * - %verticalborder_ceiling_enabled% - Returns whether ceiling is enabled
 * - %verticalborder_floor_enabled% - Returns whether floor is enabled
 * - %verticalborder_border_enabled% - Returns whether border is enabled
 * - %verticalborder_height_range% - Returns total height range
 *
 * Player-specific (for leaderboards, looks up another player's island):
 * - %verticalborder_top_y_<player>% - Returns top Y for specified player's island
 * - %verticalborder_bottom_y_<player>% - Returns bottom Y for specified player's island
 * - %verticalborder_height_range_<player>% - Returns height range for specified player's island
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
        String lowerParams = params.toLowerCase();

        // Check for player-specific placeholders (e.g., top_y_PlayerName)
        if (lowerParams.startsWith("top_y_")) {
            String targetName = params.substring(6); // Remove "top_y_"
            return getPlayerValue(targetName, "top_y", player);
        }
        if (lowerParams.startsWith("bottom_y_")) {
            String targetName = params.substring(9); // Remove "bottom_y_"
            return getPlayerValue(targetName, "bottom_y", player);
        }
        if (lowerParams.startsWith("height_range_")) {
            String targetName = params.substring(13); // Remove "height_range_"
            return getPlayerValue(targetName, "height_range", player);
        }
        if (lowerParams.startsWith("ceiling_enabled_")) {
            String targetName = params.substring(16); // Remove "ceiling_enabled_"
            return getPlayerValue(targetName, "ceiling_enabled", player);
        }
        if (lowerParams.startsWith("floor_enabled_")) {
            String targetName = params.substring(14); // Remove "floor_enabled_"
            return getPlayerValue(targetName, "floor_enabled", player);
        }
        if (lowerParams.startsWith("border_enabled_")) {
            String targetName = params.substring(15); // Remove "border_enabled_"
            return getPlayerValue(targetName, "border_enabled", player);
        }

        // Self-referencing placeholders - require the requesting player
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
            return getDefaultValue(lowerParams);
        }

        // Get the border data for this island
        BorderIslandData data = addon.getDataManager().getData(island);
        if (data == null) {
            return getDefaultValue(lowerParams);
        }

        // Handle different placeholder parameters
        return switch (lowerParams) {
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
     * Gets a value for a specific player's island.
     * Used for leaderboards and displaying other players' values.
     *
     * @param playerName The name of the player to look up
     * @param valueType The type of value to retrieve (top_y, bottom_y, height_range, etc.)
     * @param requestingPlayer The player requesting the placeholder (used for world context)
     * @return The value as a string, or default/empty if not found
     */
    private String getPlayerValue(String playerName, String valueType, Player requestingPlayer) {
        // Try to find the player by name
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(playerName);
        UUID targetUUID = targetPlayer.getUniqueId();

        // Determine which world to check - use requesting player's world if available,
        // otherwise check all game mode worlds
        Island island = null;

        if (requestingPlayer != null) {
            // Check the requesting player's current world first
            island = addon.getIslands().getIsland(requestingPlayer.getWorld(), targetUUID);
        }

        // If not found and we have a requesting player, try their world
        if (island == null) {
            // Try to find island in any registered game mode world
            for (World world : Bukkit.getWorlds()) {
                if (addon.getPlugin().getIWM().inWorld(world)) {
                    island = addon.getIslands().getIsland(world, targetUUID);
                    if (island != null) {
                        break;
                    }
                }
            }
        }

        if (island == null) {
            return getDefaultValue(valueType);
        }

        BorderIslandData data = addon.getDataManager().getData(island);
        if (data == null) {
            return getDefaultValue(valueType);
        }

        return switch (valueType.toLowerCase()) {
            case "top_y" -> String.valueOf(data.getTopY());
            case "bottom_y" -> String.valueOf(data.getBottomY());
            case "ceiling_enabled" -> String.valueOf(data.isCeilingEnabled());
            case "floor_enabled" -> String.valueOf(data.isFloorEnabled());
            case "border_enabled" -> String.valueOf(data.isBorderEnabled());
            case "height_range" -> String.valueOf(data.getTopY() - data.getBottomY());
            default -> "";
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
