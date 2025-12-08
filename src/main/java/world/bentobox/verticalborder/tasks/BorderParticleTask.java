package world.bentobox.verticalborder.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.verticalborder.VerticalBorderAddon;
import world.bentobox.verticalborder.database.BorderIslandData;

/**
 * Task that displays particles near vertical borders to warn players.
 */
public class BorderParticleTask extends BukkitRunnable {

    private final VerticalBorderAddon addon;
    private static final int PARTICLE_SPACING = 2;

    private Particle particleType;
    private Particle.DustOptions dustOptions;

    /**
     * Create a new BorderParticleTask.
     * @param addon The VerticalBorder addon instance
     */
    public BorderParticleTask(VerticalBorderAddon addon) {
        this.addon = addon;
        loadParticleSettings();
    }

    /**
     * Load particle settings from config.
     */
    public void loadParticleSettings() {
        // Parse particle type from config
        String particleTypeName = addon.getSettings().getParticleType();
        try {
            this.particleType = Particle.valueOf(particleTypeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            addon.logWarning("Invalid particle type '" + particleTypeName + "', defaulting to DUST");
            this.particleType = Particle.DUST;
        }

        // Create dust options if using DUST particle
        if (this.particleType == Particle.DUST) {
            Color dustColor = Color.fromRGB(
                Math.max(0, Math.min(255, addon.getSettings().getDustColorRed())),
                Math.max(0, Math.min(255, addon.getSettings().getDustColorGreen())),
                Math.max(0, Math.min(255, addon.getSettings().getDustColorBlue()))
            );
            float dustSize = (float) addon.getSettings().getDustSize();
            this.dustOptions = new Particle.DustOptions(dustColor, dustSize);
        }
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Check if player is in a BentoBox world
            if (!addon.getPlugin().getIWM().inWorld(player.getWorld())) {
                continue;
            }

            // Get the island at the player's location
            addon.getIslands().getIslandAt(player.getLocation()).ifPresent(island -> {
                showParticlesForPlayer(player, island);
            });
        }
    }

    /**
     * Show particles to a player near vertical borders.
     * @param player The player to show particles to
     * @param island The island the player is on
     */
    private void showParticlesForPlayer(Player player, Island island) {
        BorderIslandData data = addon.getDataManager().getData(island);

        if (!data.isBorderEnabled()) {
            return;
        }

        int playerY = player.getLocation().getBlockY();
        int warningDistance = addon.getSettings().getWarningDistance();

        // Show ceiling particles when approaching top
        if (data.isCeilingEnabled() && playerY >= data.getTopY() - warningDistance) {
            showHorizontalParticlePlane(player, island, data.getTopY());
        }

        // Show floor particles when approaching bottom
        if (data.isFloorEnabled() && playerY <= data.getBottomY() + warningDistance) {
            showHorizontalParticlePlane(player, island, data.getBottomY());
        }
    }

    /**
     * Display a horizontal plane of particles at a specific Y level.
     * @param player The player to show particles to
     * @param island The island for boundary checking
     * @param y The Y level to show particles at
     */
    private void showHorizontalParticlePlane(Player player, Island island, int y) {
        Location playerLoc = player.getLocation();
        int displayRadius = addon.getSettings().getParticleDisplayRadius();

        int minX = playerLoc.getBlockX() - displayRadius;
        int maxX = playerLoc.getBlockX() + displayRadius;
        int minZ = playerLoc.getBlockZ() - displayRadius;
        int maxZ = playerLoc.getBlockZ() + displayRadius;

        for (int x = minX; x <= maxX; x += PARTICLE_SPACING) {
            for (int z = minZ; z <= maxZ; z += PARTICLE_SPACING) {
                Location particleLoc = new Location(player.getWorld(), x, y, z);

                // Only show particles within the island protection area
                if (island.onIsland(particleLoc)) {
                    spawnParticle(player, x + 0.5, y + 0.5, z + 0.5);
                }
            }
        }
    }

    /**
     * Spawn a particle at the specified location for the player.
     * @param player The player to show the particle to
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    private void spawnParticle(Player player, double x, double y, double z) {
        if (particleType == Particle.DUST && dustOptions != null) {
            player.spawnParticle(particleType, x, y, z, 1, dustOptions);
        } else {
            player.spawnParticle(particleType, x, y, z, 1);
        }
    }
}
