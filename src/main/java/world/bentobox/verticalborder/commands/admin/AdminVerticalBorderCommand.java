package world.bentobox.verticalborder.commands.admin;

import java.util.List;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.verticalborder.VerticalBorderAddon;

/**
 * Main admin command for VerticalBorder.
 * Usage: /[gamemode] admin verticalborder (or vb)
 */
public class AdminVerticalBorderCommand extends CompositeCommand {

    /**
     * Create the admin vertical border command.
     * @param addon The VerticalBorder addon instance
     * @param parent The parent admin command
     */
    public AdminVerticalBorderCommand(VerticalBorderAddon addon, CompositeCommand parent) {
        super(addon, parent, "verticalborder", "vb");
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
        setPermission("admin.verticalborder");
        setDescription("verticalborder.admin.description");

        // Register subcommands
        VerticalBorderAddon vbAddon = getVerticalBorderAddon();
        new SetHeightCommand(vbAddon, this);
        new AdjustHeightCommand(vbAddon, this);
        new ReloadCommand(vbAddon, this);
        new InfoCommand(vbAddon, this);
        new ToggleCommand(vbAddon, this);
        new UpdateCommand(vbAddon, this);
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        showHelp(this, user);
        return true;
    }
}
