package world.bentobox.verticalborder.database;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;

/**
 * Stores per-island vertical border configuration data.
 */
@Table(name = "VerticalBorderData")
public class BorderIslandData implements DataObject {

    /**
     * Island's unique ID - used as the primary key.
     */
    @Expose
    private String uniqueId;

    /**
     * Top Y boundary (ceiling level).
     */
    @Expose
    private int topY = 320;

    /**
     * Bottom Y boundary (floor level).
     */
    @Expose
    private int bottomY = -64;

    /**
     * Whether the border is enabled for this island.
     */
    @Expose
    private boolean borderEnabled = true;

    /**
     * Whether the ceiling barrier is enabled.
     */
    @Expose
    private boolean ceilingEnabled = true;

    /**
     * Whether the floor barrier is enabled.
     */
    @Expose
    private boolean floorEnabled = true;

    /**
     * Last known center X coordinate where barriers were placed.
     * Used to remove barriers when island is moved.
     */
    @Expose
    private int lastCenterX = 0;

    /**
     * Last known center Z coordinate where barriers were placed.
     * Used to remove barriers when island is moved.
     */
    @Expose
    private int lastCenterZ = 0;

    /**
     * Last known protection range where barriers were placed.
     * Used to remove barriers when island is moved.
     */
    @Expose
    private int lastProtectionRange = 0;

    /**
     * Whether the location data has been initialized.
     */
    @Expose
    private boolean locationInitialized = false;

    /**
     * Required no-args constructor for GSON serialization.
     */
    public BorderIslandData() {
    }

    /**
     * Create new border data for an island.
     * @param islandId The island's unique ID
     */
    public BorderIslandData(String islandId) {
        this.uniqueId = islandId;
    }

    /**
     * Create new border data with specific Y boundaries.
     * @param islandId The island's unique ID
     * @param topY The top Y boundary
     * @param bottomY The bottom Y boundary
     */
    public BorderIslandData(String islandId, int topY, int bottomY) {
        this.uniqueId = islandId;
        this.topY = topY;
        this.bottomY = bottomY;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public int getTopY() {
        return topY;
    }

    public void setTopY(int topY) {
        this.topY = topY;
    }

    public int getBottomY() {
        return bottomY;
    }

    public void setBottomY(int bottomY) {
        this.bottomY = bottomY;
    }

    public boolean isBorderEnabled() {
        return borderEnabled;
    }

    public void setBorderEnabled(boolean borderEnabled) {
        this.borderEnabled = borderEnabled;
    }

    public boolean isCeilingEnabled() {
        return ceilingEnabled;
    }

    public void setCeilingEnabled(boolean ceilingEnabled) {
        this.ceilingEnabled = ceilingEnabled;
    }

    public boolean isFloorEnabled() {
        return floorEnabled;
    }

    public void setFloorEnabled(boolean floorEnabled) {
        this.floorEnabled = floorEnabled;
    }

    public int getLastCenterX() {
        return lastCenterX;
    }

    public void setLastCenterX(int lastCenterX) {
        this.lastCenterX = lastCenterX;
    }

    public int getLastCenterZ() {
        return lastCenterZ;
    }

    public void setLastCenterZ(int lastCenterZ) {
        this.lastCenterZ = lastCenterZ;
    }

    public int getLastProtectionRange() {
        return lastProtectionRange;
    }

    public void setLastProtectionRange(int lastProtectionRange) {
        this.lastProtectionRange = lastProtectionRange;
    }

    public boolean isLocationInitialized() {
        return locationInitialized;
    }

    public void setLocationInitialized(boolean locationInitialized) {
        this.locationInitialized = locationInitialized;
    }

    /**
     * Update the stored location data from an island.
     * @param centerX The island center X coordinate
     * @param centerZ The island center Z coordinate
     * @param protectionRange The island protection range
     */
    public void updateLocation(int centerX, int centerZ, int protectionRange) {
        this.lastCenterX = centerX;
        this.lastCenterZ = centerZ;
        this.lastProtectionRange = protectionRange;
        this.locationInitialized = true;
    }
}
