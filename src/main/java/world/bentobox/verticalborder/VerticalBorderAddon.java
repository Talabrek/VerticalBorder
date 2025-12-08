package world.bentobox.verticalborder;

import org.bukkit.Bukkit;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.verticalborder.commands.PlayerBorderCommand;
import world.bentobox.verticalborder.commands.admin.AdminVerticalBorderCommand;
import world.bentobox.verticalborder.listeners.ChunkLoadListener;
import world.bentobox.verticalborder.listeners.IslandEventListener;
import world.bentobox.verticalborder.listeners.PlayerMoveListener;
import world.bentobox.verticalborder.managers.BorderDataManager;
import world.bentobox.verticalborder.managers.FAWEBarrierManager;
import world.bentobox.verticalborder.tasks.BorderParticleTask;

/**
 * Main addon class for VerticalBorder.
 * Creates vertical Y-axis borders for BentoBox islands using barrier blocks via FAWE.
 */
public class VerticalBorderAddon extends Addon {

    private Settings settings;
    private BorderDataManager dataManager;
    private FAWEBarrierManager barrierManager;
    private BorderParticleTask particleTask;
    private boolean faweEnabled = false;

    @Override
    public void onLoad() {
        // Save default config
        saveDefaultConfig();

        // Load settings
        settings = new Config<>(this, Settings.class).loadConfigObject();
        if (settings == null) {
            settings = new Settings();
        }
    }

    @Override
    public void onEnable() {
        // Check if FAWE is available
        if (Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null) {
            faweEnabled = true;
            log("FastAsyncWorldEdit found! Barrier placement enabled.");
        } else {
            log("FastAsyncWorldEdit not found. Barrier placement disabled, only player movement enforcement will work.");
        }

        // Initialize managers
        dataManager = new BorderDataManager(this);
        barrierManager = new FAWEBarrierManager(this);

        // Register listeners
        registerListener(new IslandEventListener(this));
        registerListener(new ChunkLoadListener(this));
        registerListener(new PlayerMoveListener(this));

        // Register commands with each game mode
        getPlugin().getAddonsManager().getGameModeAddons().forEach(gameMode -> {
            if (!settings.getDisabledGameModes().contains(gameMode.getDescription().getName())) {
                log("Registering VerticalBorder commands for " + gameMode.getDescription().getName());

                gameMode.getAdminCommand().ifPresent(adminCmd ->
                    new AdminVerticalBorderCommand(this, adminCmd));

                gameMode.getPlayerCommand().ifPresent(playerCmd ->
                    new PlayerBorderCommand(this, playerCmd));
            }
        });

        // Start particle task if enabled
        if (settings.isParticlesEnabled()) {
            particleTask = new BorderParticleTask(this);
            particleTask.runTaskTimer(getPlugin(), 20L, settings.getParticleInterval());
        }

        log("VerticalBorder addon enabled successfully!");
    }

    @Override
    public void onReload() {
        // Reload settings
        settings = new Config<>(this, Settings.class).loadConfigObject();
        if (settings == null) {
            settings = new Settings();
        }

        // Restart particle task with new settings
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }

        if (settings.isParticlesEnabled()) {
            particleTask = new BorderParticleTask(this);
            particleTask.runTaskTimer(getPlugin(), 20L, settings.getParticleInterval());
        }

        log("VerticalBorder addon reloaded!");
    }

    @Override
    public void onDisable() {
        // Cancel particle task
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }

        // Save all data
        if (dataManager != null) {
            dataManager.saveAll();
        }

        log("VerticalBorder addon disabled.");
    }

    /**
     * Get the addon settings.
     * @return Settings object
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * Get the border data manager.
     * @return BorderDataManager
     */
    public BorderDataManager getDataManager() {
        return dataManager;
    }

    /**
     * Get the FAWE barrier manager.
     * @return FAWEBarrierManager
     */
    public FAWEBarrierManager getBarrierManager() {
        return barrierManager;
    }

    /**
     * Check if FAWE is enabled.
     * @return true if FAWE is available
     */
    public boolean isFaweEnabled() {
        return faweEnabled;
    }
}
