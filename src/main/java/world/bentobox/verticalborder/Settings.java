package world.bentobox.verticalborder;

import java.util.HashSet;
import java.util.Set;

import world.bentobox.bentobox.api.configuration.ConfigComment;
import world.bentobox.bentobox.api.configuration.ConfigEntry;
import world.bentobox.bentobox.api.configuration.ConfigObject;
import world.bentobox.bentobox.api.configuration.StoreAt;

/**
 * Configuration settings for the VerticalBorder addon.
 */
@StoreAt(filename = "config.yml", path = "addons/VerticalBorder")
@ConfigComment("VerticalBorder Configuration")
@ConfigComment("")
public class Settings implements ConfigObject {

    @ConfigComment("")
    @ConfigComment("Default Y-axis boundaries for new islands")
    @ConfigComment("")
    @ConfigComment("Default top Y boundary (ceiling) for new islands")
    @ConfigEntry(path = "defaults.top-y")
    private int defaultTopY = 320;

    @ConfigComment("")
    @ConfigComment("Default bottom Y boundary (floor) for new islands")
    @ConfigEntry(path = "defaults.bottom-y")
    private int defaultBottomY = -64;

    @ConfigComment("")
    @ConfigComment("Enable ceiling border by default for new islands")
    @ConfigEntry(path = "defaults.ceiling-enabled")
    private boolean defaultCeilingEnabled = true;

    @ConfigComment("")
    @ConfigComment("Enable floor border by default for new islands")
    @ConfigEntry(path = "defaults.floor-enabled")
    private boolean defaultFloorEnabled = true;

    @ConfigComment("")
    @ConfigComment("Particle visualization settings")
    @ConfigComment("")
    @ConfigComment("Enable particle visualization near borders")
    @ConfigEntry(path = "particles.enabled")
    private boolean particlesEnabled = true;

    @ConfigComment("")
    @ConfigComment("Particle update interval in ticks (20 = 1 second)")
    @ConfigEntry(path = "particles.interval")
    private int particleInterval = 10;

    @ConfigComment("")
    @ConfigComment("Distance from border to start showing particles")
    @ConfigEntry(path = "particles.warning-distance")
    private int warningDistance = 8;

    @ConfigComment("")
    @ConfigComment("Particle type to display (see Bukkit Particle enum)")
    @ConfigEntry(path = "particles.type")
    private String particleType = "DUST";

    @ConfigComment("")
    @ConfigComment("Radius around player to display particles")
    @ConfigEntry(path = "particles.display-radius")
    private int particleDisplayRadius = 5;

    @ConfigComment("")
    @ConfigComment("Dust particle color - red component (0-255)")
    @ConfigEntry(path = "particles.dust-color.red")
    private int dustColorRed = 0;

    @ConfigComment("")
    @ConfigComment("Dust particle color - green component (0-255)")
    @ConfigEntry(path = "particles.dust-color.green")
    private int dustColorGreen = 100;

    @ConfigComment("")
    @ConfigComment("Dust particle color - blue component (0-255)")
    @ConfigEntry(path = "particles.dust-color.blue")
    private int dustColorBlue = 255;

    @ConfigComment("")
    @ConfigComment("Dust particle size")
    @ConfigEntry(path = "particles.dust-size")
    private double dustSize = 1.0;

    @ConfigComment("")
    @ConfigComment("Border enforcement settings")
    @ConfigComment("")
    @ConfigComment("Teleport players back when they breach the border")
    @ConfigEntry(path = "enforcement.teleport-back")
    private boolean teleportBack = true;

    @ConfigComment("")
    @ConfigComment("Distance to teleport player back from border")
    @ConfigEntry(path = "enforcement.teleport-distance")
    private int teleportDistance = 2;

    @ConfigComment("")
    @ConfigComment("Barrier block settings")
    @ConfigComment("")
    @ConfigComment("Regenerate barriers when chunks load")
    @ConfigEntry(path = "barriers.regenerate-on-chunk-load")
    private boolean regenerateOnChunkLoad = true;

    @ConfigComment("")
    @ConfigComment("Enable barrier block placement (requires FAWE)")
    @ConfigEntry(path = "barriers.place-barrier-blocks")
    private boolean placeBarrierBlocks = true;

    @ConfigComment("")
    @ConfigComment("Game modes where this addon is disabled")
    @ConfigComment("Add game mode names to disable (e.g., BSkyBlock, AcidIsland)")
    @ConfigEntry(path = "disabled-gamemodes")
    private Set<String> disabledGameModes = new HashSet<>();

    // Getters and Setters

    public int getDefaultTopY() {
        return defaultTopY;
    }

    public void setDefaultTopY(int defaultTopY) {
        this.defaultTopY = defaultTopY;
    }

    public int getDefaultBottomY() {
        return defaultBottomY;
    }

    public void setDefaultBottomY(int defaultBottomY) {
        this.defaultBottomY = defaultBottomY;
    }

    public boolean isDefaultCeilingEnabled() {
        return defaultCeilingEnabled;
    }

    public void setDefaultCeilingEnabled(boolean defaultCeilingEnabled) {
        this.defaultCeilingEnabled = defaultCeilingEnabled;
    }

    public boolean isDefaultFloorEnabled() {
        return defaultFloorEnabled;
    }

    public void setDefaultFloorEnabled(boolean defaultFloorEnabled) {
        this.defaultFloorEnabled = defaultFloorEnabled;
    }

    public boolean isParticlesEnabled() {
        return particlesEnabled;
    }

    public void setParticlesEnabled(boolean particlesEnabled) {
        this.particlesEnabled = particlesEnabled;
    }

    public int getParticleInterval() {
        return particleInterval;
    }

    public void setParticleInterval(int particleInterval) {
        this.particleInterval = particleInterval;
    }

    public int getWarningDistance() {
        return warningDistance;
    }

    public void setWarningDistance(int warningDistance) {
        this.warningDistance = warningDistance;
    }

    public String getParticleType() {
        return particleType;
    }

    public void setParticleType(String particleType) {
        this.particleType = particleType;
    }

    public int getParticleDisplayRadius() {
        return particleDisplayRadius;
    }

    public void setParticleDisplayRadius(int particleDisplayRadius) {
        this.particleDisplayRadius = particleDisplayRadius;
    }

    public int getDustColorRed() {
        return dustColorRed;
    }

    public void setDustColorRed(int dustColorRed) {
        this.dustColorRed = dustColorRed;
    }

    public int getDustColorGreen() {
        return dustColorGreen;
    }

    public void setDustColorGreen(int dustColorGreen) {
        this.dustColorGreen = dustColorGreen;
    }

    public int getDustColorBlue() {
        return dustColorBlue;
    }

    public void setDustColorBlue(int dustColorBlue) {
        this.dustColorBlue = dustColorBlue;
    }

    public double getDustSize() {
        return dustSize;
    }

    public void setDustSize(double dustSize) {
        this.dustSize = dustSize;
    }

    public boolean isTeleportBack() {
        return teleportBack;
    }

    public void setTeleportBack(boolean teleportBack) {
        this.teleportBack = teleportBack;
    }

    public int getTeleportDistance() {
        return teleportDistance;
    }

    public void setTeleportDistance(int teleportDistance) {
        this.teleportDistance = teleportDistance;
    }

    public boolean isRegenerateOnChunkLoad() {
        return regenerateOnChunkLoad;
    }

    public void setRegenerateOnChunkLoad(boolean regenerateOnChunkLoad) {
        this.regenerateOnChunkLoad = regenerateOnChunkLoad;
    }

    public boolean isPlaceBarrierBlocks() {
        return placeBarrierBlocks;
    }

    public void setPlaceBarrierBlocks(boolean placeBarrierBlocks) {
        this.placeBarrierBlocks = placeBarrierBlocks;
    }

    public Set<String> getDisabledGameModes() {
        return disabledGameModes;
    }

    public void setDisabledGameModes(Set<String> disabledGameModes) {
        this.disabledGameModes = disabledGameModes;
    }
}
