package world.bentobox.verticalborder.commands.admin;

import java.util.List;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.verticalborder.VerticalBorderAddon;

/**
 * Admin command to reload the VerticalBorder configuration.
 * Usage: /[gamemode] admin vb reload
 */
public class ReloadCommand extends CompositeCommand {

    /**
     * Create the reload command.
     * @param addon The VerticalBorder addon instance
     * @param parent The parent command
     */
    public ReloadCommand(VerticalBorderAddon addon, CompositeCommand parent) {
        super(addon, parent, "reload");
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
        setPermission("admin.verticalborder.reload");
        setDescription("verticalborder.admin.reload.description");
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        // Reload the addon
        getVerticalBorderAddon().onReload();

        user.sendMessage("verticalborder.admin.reload.success");
        return true;
    }
}
