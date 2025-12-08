# VerticalBorder

A BentoBox addon that adds vertical (Y-axis) boundaries to island borders using barrier blocks placed via FastAsyncWorldEdit.

## Features

- **Vertical Boundaries** - Creates invisible barrier ceilings and floors at configurable Y-levels
- **Per-Island Customization** - Each island can have unique height limits
- **FAWE Integration** - Uses FastAsyncWorldEdit for efficient, lag-free block placement
- **Particle Visualization** - Configurable warning particles when players approach borders
- **Admin Controls** - Full command suite for server administrators
- **Player Toggle** - Players can toggle their border visibility
- **Automatic Regeneration** - Barriers regenerate on chunk load to ensure persistence

## Requirements

| Dependency | Version | Required |
|------------|---------|----------|
| [BentoBox](https://github.com/BentoBoxWorld/BentoBox) | 2.7.1+ | Yes |
| [FastAsyncWorldEdit](https://github.com/IntellectualSites/FastAsyncWorldEdit) | 2.11.1+ | Yes |
| Java | 21+ | Yes |
| Minecraft | 1.21.1 | Yes |

## Installation

1. Download the latest `verticalborder-x.x.x.jar` from [Releases](../../releases)
2. Place the JAR in your server's `plugins/BentoBox/addons/` folder
3. Ensure FastAsyncWorldEdit is installed in your `plugins/` folder
4. Restart your server
5. Configure the addon in `plugins/BentoBox/addons/VerticalBorder/config.yml`

## Configuration

```yaml
# Default Y-axis boundaries for new islands
defaults:
  top-y: 320          # Ceiling height
  bottom-y: -64       # Floor height
  ceiling-enabled: true
  floor-enabled: true

# Particle visualization settings
particles:
  enabled: true
  interval: 10        # Update interval in ticks (20 = 1 second)
  warning-distance: 8 # Distance from border to show particles
  type: DUST          # Particle type (DUST, FLAME, END_ROD, etc.)
  display-radius: 5
  dust-color:
    red: 0
    green: 100
    blue: 255
  dust-size: 1.0

# Border enforcement settings
enforcement:
  teleport-back: true
  teleport-distance: 2

# Barrier block settings
barriers:
  regenerate-on-chunk-load: true
  place-barrier-blocks: true

# Disable for specific game modes
disabled-gamemodes: []
```

## Commands

### Player Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/[gamemode] border` | Toggle border particle visibility | `[gamemode].island.border` |

### Admin Commands

All admin commands are accessed via `/[gamemode] admin verticalborder` (or `/[gamemode] admin vb`):

| Command | Description | Permission |
|---------|-------------|------------|
| `setheight <player> <top\|bottom> <value>` | Set island height limit | `[gamemode].admin.verticalborder.setheight` |
| `adjustheight <player> <top\|bottom> <amount>` | Adjust height by amount | `[gamemode].admin.verticalborder.adjustheight` |
| `info <player>` | View island border info | `[gamemode].admin.verticalborder.info` |
| `toggle <player> <ceiling\|floor\|all>` | Toggle border components | `[gamemode].admin.verticalborder.toggle` |
| `update <player>` | Force update island barriers | `[gamemode].admin.verticalborder.update` |
| `reload` | Reload addon configuration | `[gamemode].admin.verticalborder.reload` |

**Example:** For BSkyBlock, use `/bsb admin vb setheight Steve top 256`

## How It Works

1. **Island Creation** - When an island is created, the addon automatically places invisible barrier blocks at the configured ceiling and floor Y-levels across the island's protection area.

2. **Player Movement** - When players approach or attempt to pass through the vertical borders, they are teleported back to safety (if enforcement is enabled).

3. **Visual Feedback** - Configurable particles appear when players are within the warning distance of a border, giving them visual indication of the boundary.

4. **Chunk Loading** - Barriers are regenerated when chunks load to ensure they persist even after server restarts or world modifications.

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `[gamemode].island.border` | Use the border toggle command | `true` |
| `[gamemode].admin.verticalborder` | Access admin vertical border commands | `op` |
| `[gamemode].admin.verticalborder.setheight` | Set island height limits | `op` |
| `[gamemode].admin.verticalborder.adjustheight` | Adjust island height limits | `op` |
| `[gamemode].admin.verticalborder.info` | View island border information | `op` |
| `[gamemode].admin.verticalborder.toggle` | Toggle border components | `op` |
| `[gamemode].admin.verticalborder.update` | Force update island barriers | `op` |
| `[gamemode].admin.verticalborder.reload` | Reload addon configuration | `op` |

Replace `[gamemode]` with your game mode prefix (e.g., `bskyblock`, `acidisland`, `caveblock`).

## Building from Source

```bash
# Clone the repository
git clone https://github.com/Talabrek/VerticalBorder.git
cd VerticalBorder

# Build with Maven
mvn clean package

# The compiled JAR will be in target/verticalborder-x.x.x.jar
```

## Project Structure

```
src/main/java/world/bentobox/verticalborder/
├── VerticalBorderAddon.java          # Main addon entry point
├── Settings.java                     # Configuration settings
├── database/
│   └── BorderIslandData.java         # Per-island data storage
├── managers/
│   ├── BorderDataManager.java        # Database operations
│   └── FAWEBarrierManager.java       # FAWE block placement
├── listeners/
│   ├── IslandEventListener.java      # Island lifecycle events
│   ├── ChunkLoadListener.java        # Barrier regeneration
│   └── PlayerMoveListener.java       # Y-limit enforcement
├── commands/
│   ├── PlayerBorderCommand.java      # Player toggle command
│   └── admin/                        # Admin command suite
└── tasks/
    └── BorderParticleTask.java       # Particle visualization
```

## Compatibility

This addon is compatible with all BentoBox game modes:
- BSkyBlock
- AcidIsland
- CaveBlock
- SkyGrid
- Boxed
- And any other BentoBox game mode

## Support

- **Issues**: [GitHub Issues](../../issues)
- **BentoBox Discord**: [discord.bentobox.world](https://discord.bentobox.world)

## License

This project is open source. See the repository for license details.

## Credits

- Built for [BentoBox](https://github.com/BentoBoxWorld/BentoBox)
- Uses [FastAsyncWorldEdit](https://github.com/IntellectualSites/FastAsyncWorldEdit) for efficient block operations
