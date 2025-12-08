# VerticalBorder BentoBox Addon: Complete Implementation Guide

A vertical world border addon for Minecraft 1.21.1 that creates Y-axis restrictions using barrier blocks placed via FAWE is entirely feasible with the BentoBox and FAWE APIs. This guide provides the complete technical foundation for implementation, including code examples, architectural patterns, and API references.

## Project foundation and dependencies

The addon requires **BentoBox 2.7.1+**, **Java 21**, and **FAWE 2.11.1+** for Minecraft 1.21.1 compatibility. The build configuration uses Maven with the CodeMC repository for BentoBox and Maven Central for FAWE.

```xml
<repositories>
    <repository>
        <id>codemc-repo</id>
        <url>https://repo.codemc.org/repository/maven-public/</url>
    </repository>
    <repository>
        <id>enginehub</id>
        <url>https://maven.enginehub.org/repo/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>world.bentobox</groupId>
        <artifactId>bentobox</artifactId>
        <version>2.7.1</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.fastasyncworldedit</groupId>
        <artifactId>FastAsyncWorldEdit-Core</artifactId>
        <version>2.11.1</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.fastasyncworldedit</groupId>
        <artifactId>FastAsyncWorldEdit-Bukkit</artifactId>
        <version>2.11.1</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

The `addon.yml` file defines the addon metadata:

```yaml
name: VerticalBorder
main: world.bentobox.verticalborder.VerticalBorderAddon
version: 1.0.0
api-version: 2.7.1
authors: [YourName]
depend: [FastAsyncWorldEdit]
description: Vertical Y-axis borders for BentoBox islands
```

## Core addon architecture

### Main addon class structure

The addon follows BentoBox's lifecycle pattern with `onLoad()`, `onEnable()`, `onReload()`, and `onDisable()` methods:

```java
public class VerticalBorderAddon extends Addon {
    private Settings settings;
    private BorderDataManager dataManager;
    private FAWEBarrierManager barrierManager;
    
    @Override
    public void onLoad() {
        saveDefaultConfig();
        settings = new Config<>(this, Settings.class).loadConfigObject();
        if (settings == null) settings = new Settings();
    }
    
    @Override
    public void onEnable() {
        // Check FAWE availability
        if (Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") == null) {
            logError("FastAsyncWorldEdit not found! Disabling addon.");
            setState(State.DISABLED);
            return;
        }
        
        // Initialize managers
        dataManager = new BorderDataManager(this);
        barrierManager = new FAWEBarrierManager(this);
        
        // Register listeners
        registerListener(new IslandEventListener(this));
        registerListener(new ChunkLoadListener(this));
        registerListener(new PlayerMoveListener(this));
        
        // Register commands under each gamemode
        getPlugin().getAddonsManager().getGameModeAddons().forEach(gm -> {
            if (!settings.getDisabledGameModes().contains(gm.getDescription().getName())) {
                gm.getAdminCommand().ifPresent(cmd -> new AdminVerticalBorderCommand(this, cmd));
                gm.getPlayerCommand().ifPresent(cmd -> new PlayerBorderCommand(this, cmd));
            }
        });
    }
}
```

### Recommended package structure

```
world/bentobox/verticalborder/
├── VerticalBorderAddon.java           # Main addon entry point
├── Settings.java                      # @ConfigEntry annotated settings
├── database/
│   └── BorderIslandData.java          # Per-island DataObject
├── managers/
│   ├── BorderDataManager.java         # Database operations
│   └── FAWEBarrierManager.java        # FAWE block placement
├── listeners/
│   ├── IslandEventListener.java       # Island create/delete/reset
│   ├── ChunkLoadListener.java         # Barrier regeneration
│   └── PlayerMoveListener.java        # Y-limit enforcement
├── commands/
│   ├── PlayerBorderCommand.java       # Toggle command
│   └── admin/
│       ├── AdminVerticalBorderCommand.java
│       ├── SetHeightCommand.java
│       └── ReloadCommand.java
└── tasks/
    └── BorderParticleTask.java        # Visual particle rendering
```

## Per-island data storage with BentoBox database

BentoBox uses a NoSQL-style database storing serialized Java objects. The `DataObject` interface with `@Expose` annotations defines what gets persisted.

### BorderIslandData DataObject

```java
@Table(name = "VerticalBorderData")
public class BorderIslandData implements DataObject {
    
    @Expose
    private String uniqueId;  // Island's unique ID - required as primary key
    
    @Expose
    private int topY = 320;   // Ceiling Y level
    
    @Expose
    private int bottomY = -64; // Floor Y level
    
    @Expose
    private boolean borderEnabled = true;
    
    @Expose
    private boolean ceilingEnabled = true;
    
    @Expose
    private boolean floorEnabled = true;
    
    public BorderIslandData() {} // Required for GSON
    
    public BorderIslandData(String islandId) {
        this.uniqueId = islandId;
    }
    
    @Override
    public String getUniqueId() { return uniqueId; }
    
    @Override
    public void setUniqueId(String uniqueId) { this.uniqueId = uniqueId; }
    
    // Getters/setters for all @Expose fields...
}
```

### BorderDataManager for database operations

```java
public class BorderDataManager {
    private final VerticalBorderAddon addon;
    private final Database<BorderIslandData> database;
    private final Map<String, BorderIslandData> cache = new ConcurrentHashMap<>();
    
    public BorderDataManager(VerticalBorderAddon addon) {
        this.addon = addon;
        this.database = new Database<>(addon, BorderIslandData.class);
        database.loadObjects().forEach(data -> cache.put(data.getUniqueId(), data));
    }
    
    public BorderIslandData getData(Island island) {
        return cache.computeIfAbsent(island.getUniqueId(), id -> {
            BorderIslandData data = database.loadObject(id);
            return data != null ? data : new BorderIslandData(id);
        });
    }
    
    public void saveData(BorderIslandData data) {
        cache.put(data.getUniqueId(), data);
        database.saveObjectAsync(data); // Non-blocking async save
    }
    
    public void deleteData(String islandId) {
        cache.remove(islandId);
        database.deleteID(islandId);
    }
}
```

## FAWE integration for barrier block placement

FAWE enables asynchronous block operations essential for placing barriers across large vertical regions without causing server lag.

### FAWEBarrierManager implementation

```java
public class FAWEBarrierManager {
    private final Plugin plugin;
    
    public CompletableFuture<Integer> placeHorizontalBarrierPlane(
            org.bukkit.World bukkitWorld,
            int minX, int maxX, int minZ, int maxZ, int y) {
        
        CompletableFuture<Integer> future = new CompletableFuture<>();
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                World world = BukkitAdapter.adapt(bukkitWorld);
                BlockVector3 min = BlockVector3.at(minX, y, minZ);
                BlockVector3 max = BlockVector3.at(maxX, y, maxZ);
                CuboidRegion region = new CuboidRegion(world, min, max);
                
                try (EditSession session = WorldEdit.getInstance()
                        .newEditSessionBuilder()
                        .world(world)
                        .maxBlocks(-1)
                        .build()) {
                    
                    // Only replace air blocks with barriers
                    Mask airMask = new BlockTypeMask(session, 
                            BlockTypes.AIR, BlockTypes.CAVE_AIR, BlockTypes.VOID_AIR);
                    BlockState barrier = BlockTypes.BARRIER.getDefaultState();
                    
                    int changed = session.replaceBlocks(region, airMask, barrier);
                    future.complete(changed);
                }
            } catch (MaxChangedBlocksException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
    
    public CompletableFuture<Integer> removeBarrierPlane(
            org.bukkit.World bukkitWorld,
            int minX, int maxX, int minZ, int maxZ, int y) {
        
        CompletableFuture<Integer> future = new CompletableFuture<>();
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            World world = BukkitAdapter.adapt(bukkitWorld);
            BlockVector3 min = BlockVector3.at(minX, y, minZ);
            BlockVector3 max = BlockVector3.at(maxX, y, maxZ);
            CuboidRegion region = new CuboidRegion(world, min, max);
            
            try (EditSession session = WorldEdit.getInstance()
                    .newEditSessionBuilder()
                    .world(world)
                    .maxBlocks(-1)
                    .build()) {
                
                // Only remove barrier blocks
                Mask barrierMask = new BlockTypeMask(session, BlockTypes.BARRIER);
                BlockState air = BlockTypes.AIR.getDefaultState();
                
                int changed = session.replaceBlocks(region, barrierMask, air);
                future.complete(changed);
            } catch (MaxChangedBlocksException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}
```

### Creating full ceiling/floor barriers for an island

```java
public void createBordersForIsland(Island island, BorderIslandData data) {
    org.bukkit.World world = island.getWorld();
    Location center = island.getCenter();
    int range = island.getProtectionRange();
    
    int minX = center.getBlockX() - range;
    int maxX = center.getBlockX() + range;
    int minZ = center.getBlockZ() - range;
    int maxZ = center.getBlockZ() + range;
    
    // Place ceiling barrier
    if (data.isCeilingEnabled()) {
        placeHorizontalBarrierPlane(world, minX, maxX, minZ, maxZ, data.getTopY())
            .thenAccept(count -> addon.log("Placed " + count + " ceiling barriers"));
    }
    
    // Place floor barrier
    if (data.isFloorEnabled()) {
        placeHorizontalBarrierPlane(world, minX, maxX, minZ, maxZ, data.getBottomY())
            .thenAccept(count -> addon.log("Placed " + count + " floor barriers"));
    }
}
```

## Island boundary calculations

BentoBox's `Island` class provides all necessary boundary data. **Protection range** defines the protected area radius, while **island range** defines the total footprint.

```java
Island island = addon.getIslands().getIsland(world, playerUUID);

// Get boundaries
Location center = island.getCenter();
int protectionRange = island.getProtectionRange();

// Calculate world coordinates
int minX = center.getBlockX() - protectionRange;
int maxX = center.getBlockX() + protectionRange;
int minZ = center.getBlockZ() - protectionRange;
int maxZ = center.getBlockZ() + protectionRange;

// Check if player location is within island
boolean onIsland = island.onIsland(location);        // Uses protection range
boolean inIslandSpace = island.inIslandSpace(x, z);  // Uses full range
```

## Event listeners for border management

### IslandEventListener handles lifecycle events

```java
public class IslandEventListener implements Listener {
    private final VerticalBorderAddon addon;
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onIslandCreated(IslandCreatedEvent event) {
        Island island = event.getIsland();
        BorderIslandData data = new BorderIslandData(island.getUniqueId());
        data.setTopY(addon.getSettings().getDefaultTopY());
        data.setBottomY(addon.getSettings().getDefaultBottomY());
        
        addon.getDataManager().saveData(data);
        addon.getBarrierManager().createBordersForIsland(island, data);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onIslandDeleted(IslandDeletedEvent event) {
        String islandId = event.getIsland().getUniqueId();
        BorderIslandData data = addon.getDataManager().getData(event.getIsland());
        
        // Remove barriers before deleting data
        addon.getBarrierManager().removeBordersForIsland(event.getIsland(), data);
        addon.getDataManager().deleteData(islandId);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onIslandReset(IslandResettedEvent event) {
        // Recreate with defaults
        Island island = event.getIsland();
        BorderIslandData data = new BorderIslandData(island.getUniqueId());
        addon.getDataManager().saveData(data);
        addon.getBarrierManager().createBordersForIsland(island, data);
    }
}
```

### ChunkLoadListener regenerates barriers

Since barrier blocks may not persist across chunk unloads, regenerate them on chunk load:

```java
public class ChunkLoadListener implements Listener {
    private final VerticalBorderAddon addon;
    private final Set<Long> pendingChunks = ConcurrentHashMap.newKeySet();
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!addon.getPlugin().getIWM().inWorld(event.getWorld())) return;
        
        Chunk chunk = event.getChunk();
        long chunkKey = (long) chunk.getX() << 32 | chunk.getZ() & 0xFFFFFFFFL;
        
        if (pendingChunks.add(chunkKey)) {
            Bukkit.getScheduler().runTaskLater(addon.getPlugin(), () -> {
                pendingChunks.remove(chunkKey);
                regenerateBarriersInChunk(chunk);
            }, 5L);
        }
    }
    
    private void regenerateBarriersInChunk(Chunk chunk) {
        int chunkMinX = chunk.getX() << 4;
        int chunkMinZ = chunk.getZ() << 4;
        
        // Find islands intersecting this chunk
        for (int x = chunkMinX; x < chunkMinX + 16; x++) {
            for (int z = chunkMinZ; z < chunkMinZ + 16; z++) {
                Location loc = new Location(chunk.getWorld(), x, 64, z);
                addon.getIslands().getIslandAt(loc).ifPresent(island -> {
                    BorderIslandData data = addon.getDataManager().getData(island);
                    if (data.isBorderEnabled()) {
                        placeBarrierColumn(chunk.getWorld(), x, z, data);
                    }
                });
            }
        }
    }
}
```

### PlayerMoveListener enforces Y boundaries

```java
public class PlayerMoveListener implements Listener {
    private final VerticalBorderAddon addon;
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        if (to == null || !addon.getPlugin().getIWM().inWorld(to)) return;
        
        Player player = event.getPlayer();
        Island island = addon.getIslands().getIslandAt(to).orElse(null);
        if (island == null) return;
        
        BorderIslandData data = addon.getDataManager().getData(island);
        if (!data.isBorderEnabled()) return;
        
        int playerY = to.getBlockY();
        
        // Enforce ceiling
        if (playerY >= data.getTopY() && data.isCeilingEnabled()) {
            Location safe = to.clone();
            safe.setY(data.getTopY() - 2);
            event.setTo(safe);
            player.sendMessage("§cYou hit the vertical border ceiling!");
        }
        
        // Enforce floor
        if (playerY <= data.getBottomY() && data.isFloorEnabled()) {
            Location safe = to.clone();
            safe.setY(data.getBottomY() + 2);
            event.setTo(safe);
            player.sendMessage("§cYou hit the vertical border floor!");
        }
    }
}
```

## Particle visualization for border feedback

Show particles near ceiling/floor to warn players of approaching barriers:

```java
public class BorderParticleTask extends BukkitRunnable {
    private static final int PARTICLE_DISTANCE = 8;
    
    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!addon.getPlugin().getIWM().inWorld(player.getWorld())) continue;
            
            addon.getIslands().getIslandAt(player.getLocation()).ifPresent(island -> {
                BorderIslandData data = addon.getDataManager().getData(island);
                if (!data.isBorderEnabled()) return;
                
                int playerY = player.getLocation().getBlockY();
                
                // Show ceiling particles when approaching top
                if (data.isCeilingEnabled() && playerY >= data.getTopY() - PARTICLE_DISTANCE) {
                    showHorizontalParticlePlane(player, island, data.getTopY());
                }
                
                // Show floor particles when approaching bottom
                if (data.isFloorEnabled() && playerY <= data.getBottomY() + PARTICLE_DISTANCE) {
                    showHorizontalParticlePlane(player, island, data.getBottomY());
                }
            });
        }
    }
    
    private void showHorizontalParticlePlane(Player player, Island island, int y) {
        Location pLoc = player.getLocation();
        int viewDist = 12;
        
        BlockData barrierData = Material.BARRIER.createBlockData();
        
        for (int x = pLoc.getBlockX() - viewDist; x <= pLoc.getBlockX() + viewDist; x += 2) {
            for (int z = pLoc.getBlockZ() - viewDist; z <= pLoc.getBlockZ() + viewDist; z += 2) {
                if (island.onIsland(new Location(player.getWorld(), x, y, z))) {
                    player.spawnParticle(Particle.BLOCK_MARKER, 
                        x + 0.5, y + 0.5, z + 0.5, 1, barrierData);
                }
            }
        }
    }
}
```

## Admin command implementation

### AdminVerticalBorderCommand with subcommands

```java
public class AdminVerticalBorderCommand extends CompositeCommand {
    
    public AdminVerticalBorderCommand(VerticalBorderAddon addon, CompositeCommand parent) {
        super(addon, parent, "verticalborder", "vb");
    }
    
    @Override
    public void setup() {
        setPermission("admin.verticalborder");
        setDescription("verticalborder.admin.description");
        
        new SetHeightCommand(getAddon(), this);
        new ReloadCommand(getAddon(), this);
    }
    
    @Override
    public boolean execute(User user, String label, List<String> args) {
        showHelp(this, user);
        return true;
    }
}

public class SetHeightCommand extends CompositeCommand {
    
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
        
        // Parse: /bsb admin vb setheight <player> <top|bottom> <value>
        UUID targetUUID = getPlayers().getUUID(args.get(0));
        if (targetUUID == null) {
            user.sendMessage("general.errors.unknown-player", TextVariables.NAME, args.get(0));
            return false;
        }
        
        Island island = getIslands().getIsland(getWorld(), targetUUID);
        if (island == null) {
            user.sendMessage("general.errors.player-has-no-island");
            return false;
        }
        
        String heightType = args.get(1).toLowerCase();
        int value;
        try {
            value = Integer.parseInt(args.get(2));
        } catch (NumberFormatException e) {
            user.sendMessage("verticalborder.error.invalid-number");
            return false;
        }
        
        BorderIslandData data = addon.getDataManager().getData(island);
        if (heightType.equals("top")) {
            data.setTopY(value);
        } else if (heightType.equals("bottom")) {
            data.setBottomY(value);
        } else {
            user.sendMessage("verticalborder.error.invalid-type");
            return false;
        }
        
        addon.getDataManager().saveData(data);
        addon.getBarrierManager().updateBordersForIsland(island, data);
        
        user.sendMessage("verticalborder.admin.setheight.success");
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
            return Optional.of(Arrays.asList("64", "128", "256", "320"));
        }
        return Optional.empty();
    }
}
```

## Configuration with @ConfigEntry annotations

```java
@StoreAt(filename = "config.yml", path = "addons/VerticalBorder")
@ConfigComment("VerticalBorder Configuration")
public class Settings implements ConfigObject {
    
    @ConfigComment("Default top Y boundary for new islands")
    @ConfigEntry(path = "defaults.top-y")
    private int defaultTopY = 320;
    
    @ConfigComment("Default bottom Y boundary for new islands")
    @ConfigEntry(path = "defaults.bottom-y")
    private int defaultBottomY = -64;
    
    @ConfigComment("Enable particle visualization near borders")
    @ConfigEntry(path = "particles.enabled")
    private boolean particlesEnabled = true;
    
    @ConfigComment("Particle update interval in ticks (20 = 1 second)")
    @ConfigEntry(path = "particles.interval")
    private int particleInterval = 10;
    
    @ConfigComment("Distance from border to start showing particles")
    @ConfigEntry(path = "particles.warning-distance")
    private int warningDistance = 8;
    
    @ConfigComment("Teleport players back when they breach the border")
    @ConfigEntry(path = "enforcement.teleport-back")
    private boolean teleportBack = true;
    
    @ConfigComment("Regenerate barriers when chunks load")
    @ConfigEntry(path = "barriers.regenerate-on-chunk-load")
    private boolean regenerateOnChunkLoad = true;
    
    @ConfigComment("Game modes where this addon is disabled")
    @ConfigEntry(path = "disabled-gamemodes")
    private Set<String> disabledGameModes = new HashSet<>();
    
    // Getters and setters required for all fields...
}
```

## Key implementation considerations

**Performance optimization** is critical. Placing barriers across an entire island at **384 blocks** of Y-range (from -64 to 320) multiplied by potentially **thousands of X/Z coordinates** creates massive block operations. Limit actual barrier placement to horizontal planes at specific Y levels rather than full vertical walls—this reduces block count from millions to tens of thousands per island.

**Chunk persistence** requires careful handling. Barrier blocks placed via FAWE will persist in chunk data, but server configurations or world resets may clear them. The `ChunkLoadListener` pattern ensures barriers are regenerated on demand.

**FAWE threading** demands all EditSession operations run on async threads. The `EditSession.close()` method is **blocking**—never call it on the main thread. Use `Bukkit.getScheduler().runTaskAsynchronously()` for all FAWE operations.

**Island protection range vs range** distinction matters: `getProtectionRange()` returns the protected area where island rules apply, while `getRange()` returns the total island footprint. Use protection range for barrier placement to match player expectations.

## API references and resources

- **BentoBox JavaDocs**: https://javadocs.bentobox.world
- **BentoBox Documentation**: https://docs.bentobox.world
- **FAWE Documentation**: https://intellectualsites.gitbook.io/fastasyncworldedit/
- **FAWE JavaDocs**: https://intellectualsites.github.io/fastasyncworldedit-javadocs/
- **Border Addon Source** (reference): https://github.com/BentoBoxWorld/Border
- **BentoBox GitHub**: https://github.com/BentoBoxWorld/BentoBox
- **Maven Repository**: https://repo.codemc.org/repository/maven-public/

This implementation guide provides all necessary components for Claude Code to build a fully functional VerticalBorder addon. The code examples follow established BentoBox patterns from official addons and incorporate FAWE best practices for performant async block operations.