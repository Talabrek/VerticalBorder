# VerticalBorder Plugin - Development Progress

## Project Overview
A BentoBox addon that creates vertical Y-axis world borders using barrier blocks placed via FAWE.

**Target Version:** Minecraft 1.21.1
**Dependencies:** BentoBox 2.5.4-SNAPSHOT, FAWE 2.9.2, Java 17
**Build Output:** `target/verticalborder-1.0.0.jar`

---

## Current Status: BUILD SUCCESSFUL - READY FOR TESTING

### Session 1 - Initial Setup
**Date:** 2025-12-08

#### Completed Features
- [x] Project structure setup
- [x] Maven configuration (pom.xml)
- [x] addon.yml configuration
- [x] Main VerticalBorderAddon class
- [x] Settings class with @ConfigEntry annotations
- [x] BorderIslandData DataObject
- [x] BorderDataManager for database operations
- [x] FAWEBarrierManager for block placement
- [x] IslandEventListener (island create/delete/reset)
- [x] ChunkLoadListener (barrier regeneration)
- [x] PlayerMoveListener (Y-limit enforcement)
- [x] BorderParticleTask (visual feedback)
- [x] Admin commands (verticalborder/vb) with subcommands: setheight, reload, info, toggle
- [x] Player commands (border toggle)
- [x] Locales/messages file (en-US.yml)
- [x] Successful build

### Session 2 - Island Move Update Feature
**Date:** 2025-12-08

#### Completed Features
- [x] Added `/[gamemode] admin vb update <player>` command for island relocations
- [x] Added location tracking to BorderIslandData (lastCenterX, lastCenterZ, lastProtectionRange)
- [x] Added updateBorderLocation() method to FAWEBarrierManager
- [x] Updated IslandEventListener to store location when borders are created
- [x] Added locale messages for update command
- [x] Successful build

### Session 3 - Particle Config & Adjust Height Command
**Date:** 2025-12-08

#### Completed Features
- [x] Added configurable particle type (default: DUST)
- [x] Added configurable particle display radius (default: 5)
- [x] Added configurable dust particle color (RGB, default: blue 0,100,255)
- [x] Added configurable dust particle size (default: 1.0)
- [x] Added `/[gamemode] admin vb adjustheight <player> <top|bottom> <adjustment>` command
- [x] Adjust height supports positive and negative values (e.g., 20 or -20)
- [x] Updated Settings.java with new particle configuration options
- [x] Updated BorderParticleTask to use configurable particles
- [x] Added locale messages for adjustheight command
- [x] Successful build

---

## File Structure
```
VerticalBorder/
├── pom.xml
├── PROGRESS.md (this file)
├── verticalborder_specification.md
├── target/
│   └── verticalborder-1.0.0.jar    <-- Plugin JAR file
└── src/main/
    ├── java/world/bentobox/verticalborder/
    │   ├── VerticalBorderAddon.java
    │   ├── Settings.java
    │   ├── database/
    │   │   └── BorderIslandData.java
    │   ├── managers/
    │   │   ├── BorderDataManager.java
    │   │   └── FAWEBarrierManager.java
    │   ├── listeners/
    │   │   ├── IslandEventListener.java
    │   │   ├── ChunkLoadListener.java
    │   │   └── PlayerMoveListener.java
    │   ├── commands/
    │   │   ├── PlayerBorderCommand.java
    │   │   └── admin/
    │   │       ├── AdminVerticalBorderCommand.java
    │   │       ├── SetHeightCommand.java
    │   │       ├── ReloadCommand.java
    │   │       ├── InfoCommand.java
    │   │       ├── ToggleCommand.java
    │   │       ├── UpdateCommand.java
    │   │       └── AdjustHeightCommand.java  <-- NEW
    │   └── tasks/
    │       └── BorderParticleTask.java
    └── resources/
        ├── addon.yml
        ├── config.yml              <-- Required by BentoBox
        └── locales/
            └── en-US.yml
```

---

## Bug Fixes Attempted

### BUG-001: Dependency Resolution Failed
**Description:** BentoBox 2.7.1 not found in Maven repositories
**Symptoms:** Build fails with "Could not find artifact world.bentobox:bentobox:jar:2.7.1"
**Root Cause:** Version 2.7.1 was not available in the CodeMC repository
**Fix Attempted:** Changed BentoBox version to 2.5.4-SNAPSHOT and added maven-snapshots repository
**Result:** SUCCESS
**Notes:** The -SNAPSHOT suffix is required for BentoBox versions in Maven

### BUG-002: Java Version Mismatch
**Description:** Compilation fails with "invalid target release: 21"
**Symptoms:** Maven using Java 17 but pom.xml targeting Java 21
**Root Cause:** System Maven configured with Java 17
**Fix Attempted:** Changed maven.compiler.source/target from 21 to 17
**Result:** SUCCESS
**Notes:** FAWE 2.11.1 requires Java 21, so downgraded to FAWE 2.9.2 for Java 17 compatibility

### BUG-003: FAWE Class Version Error
**Description:** "class file has wrong version 65.0, should be 61.0"
**Symptoms:** FAWE classes compiled for Java 21 (65.0) but project using Java 17 (61.0)
**Root Cause:** FAWE 2.11.1 requires Java 21
**Fix Attempted:** Downgraded FAWE from 2.11.1 to 2.9.2
**Result:** SUCCESS
**Notes:** FAWE 2.9.2 is the last version compatible with Java 17

### BUG-004: API Incompatibility with Island.isAllowed()
**Description:** "incompatible types: String cannot be converted to Flag"
**Symptoms:** PlayerBorderCommand using incorrect API for island permission check
**Root Cause:** island.isAllowed(user, "STRING") expects a Flag object, not a String
**Fix Attempted:** Changed to use island.getMemberSet().contains() instead
**Result:** SUCCESS
**Notes:** For future custom flags, need to register a proper Flag object

### BUG-005: Missing config.yml Resource
**Description:** "The embedded resource 'config.yml' cannot be found"
**Symptoms:** Addon fails to load with IllegalArgumentException during saveDefaultConfig()
**Root Cause:** BentoBox's saveDefaultConfig() expects a config.yml file in the JAR resources
**Fix Attempted:** Created config.yml in src/main/resources with all configuration options
**Result:** SUCCESS
**Notes:** Even when using @StoreAt annotation, BentoBox still requires the config.yml file

### BUG-006: NullPointerException in Command Classes
**Description:** "Cannot invoke VerticalBorderAddon.getDataManager() because this.addon is null"
**Symptoms:** Commands fail with NullPointerException when executed (e.g., /bsbadmin vb setheight)
**Root Cause:** Command classes stored addon as a local field, but BentoBox's CompositeCommand doesn't initialize local fields - must use getAddon() method
**Fix Attempted:** Removed local `addon` field from all command classes and replaced with `getVerticalBorderAddon()` helper method that casts `getAddon()` to VerticalBorderAddon
**Result:** SUCCESS
**Files Modified:** SetHeightCommand.java, ReloadCommand.java, InfoCommand.java, ToggleCommand.java, UpdateCommand.java, PlayerBorderCommand.java, AdminVerticalBorderCommand.java
**Notes:** In BentoBox command classes, always use `getAddon()` from CompositeCommand rather than storing a local reference

### BUG-007: ERROR Logs for Missing BorderIslandData Files
**Description:** "Could not load file 'database/BorderIslandData/BSkyBlock....json': File not found."
**Symptoms:** ERROR messages in console when accessing border data for islands that don't have saved data yet
**Root Cause:** BentoBox's database.loadObject() logs an ERROR when the file doesn't exist, even though returning null is expected behavior
**Fix Attempted:** Added database.objectExists(id) check before calling loadObject() to avoid triggering the error log
**Result:** SUCCESS
**Files Modified:** BorderDataManager.java
**Notes:** This is cosmetic - the addon worked correctly, but the ERROR logs were confusing

---

## Changes Log

### Version 1.0.0 (In Development)

| Date | Change Description | Files Modified |
|------|-------------------|----------------|
| 2025-12-08 | Initial project setup | All files created |
| 2025-12-08 | Fixed BentoBox version to 2.5.4-SNAPSHOT | pom.xml |
| 2025-12-08 | Downgraded Java target from 21 to 17 | pom.xml |
| 2025-12-08 | Downgraded FAWE from 2.11.1 to 2.9.2 | pom.xml |
| 2025-12-08 | Fixed island permission check API | PlayerBorderCommand.java |
| 2025-12-08 | Successful build | - |
| 2025-12-08 | Added update command for island relocations | UpdateCommand.java (new), BorderIslandData.java, FAWEBarrierManager.java, IslandEventListener.java, AdminVerticalBorderCommand.java, en-US.yml |
| 2025-12-08 | Fixed missing config.yml resource | config.yml (new) |
| 2025-12-08 | Fixed NullPointerException in command classes | SetHeightCommand.java, ReloadCommand.java, InfoCommand.java, ToggleCommand.java, UpdateCommand.java, PlayerBorderCommand.java, AdminVerticalBorderCommand.java |
| 2025-12-08 | Fixed ERROR logs for missing BorderIslandData | BorderDataManager.java |
| 2025-12-08 | Added configurable particle settings | config.yml, Settings.java, BorderParticleTask.java |
| 2025-12-08 | Added adjustheight command | AdjustHeightCommand.java (new), AdminVerticalBorderCommand.java, en-US.yml |
| 2025-12-08 | Added default ceiling/floor enabled options | config.yml, Settings.java, BorderDataManager.java |

---

## Known Issues
*None currently - awaiting testing*

---

## Testing Notes
**Installation:**
1. Copy `target/verticalborder-1.0.0.jar` to the BentoBox addons folder
2. Ensure FAWE (FastAsyncWorldEdit) is installed
3. Ensure a BentoBox game mode (BSkyBlock, AcidIsland, etc.) is installed
4. Restart the server

**Test Checklist:**
- [ ] Addon loads without errors
- [ ] Commands register correctly with game modes
- [ ] Barriers are placed when island is created
- [ ] Barriers are removed when island is deleted
- [ ] Player movement is restricted at Y boundaries
- [ ] Particles show near borders
- [ ] Admin setheight command works
- [ ] Admin toggle command works
- [ ] Player border toggle works
- [ ] Config reloads correctly
- [ ] **NEW:** Admin update command works after island move

**Testing the Update Command:**
1. Create an island for a player
2. Note the current location (visible in `/[gamemode] admin vb info <player>`)
3. Use your custom island move plugin to relocate the island
4. Run `/[gamemode] admin vb update <player>`
5. Verify barriers are removed from old location
6. Verify barriers are placed at new location
7. Verify Y heights remain unchanged

---

## Configuration Options
- `defaults.top-y`: Default ceiling Y level (default: 320)
- `defaults.bottom-y`: Default floor Y level (default: -64)
- `defaults.ceiling-enabled`: Enable ceiling border by default (default: true) **NEW**
- `defaults.floor-enabled`: Enable floor border by default (default: true) **NEW**
- `particles.enabled`: Enable particle visualization (default: true)
- `particles.interval`: Particle update interval in ticks (default: 10)
- `particles.warning-distance`: Distance from border to show particles (default: 8)
- `particles.type`: Particle type to display (default: DUST) **NEW**
- `particles.display-radius`: Radius around player to display particles (default: 5) **NEW**
- `particles.dust-color.red`: Dust particle red component 0-255 (default: 0) **NEW**
- `particles.dust-color.green`: Dust particle green component 0-255 (default: 100) **NEW**
- `particles.dust-color.blue`: Dust particle blue component 0-255 (default: 255) **NEW**
- `particles.dust-size`: Dust particle size (default: 1.0) **NEW**
- `enforcement.teleport-back`: Teleport players when breaching border (default: true)
- `enforcement.teleport-distance`: Distance to teleport player back (default: 2)
- `barriers.regenerate-on-chunk-load`: Regenerate barriers on chunk load (default: true)
- `barriers.place-barrier-blocks`: Enable barrier block placement via FAWE (default: true)
- `disabled-gamemodes`: List of disabled game modes

---

## Commands Reference

### Admin Commands
- `/[gamemode] admin verticalborder` or `/[gamemode] admin vb` - Main admin command
- `/[gamemode] admin vb setheight <player> <top|bottom> <value>` - Set border height
- `/[gamemode] admin vb adjustheight <player> <top|bottom> <adjustment>` - **NEW:** Adjust border height (e.g., 20 or -20)
- `/[gamemode] admin vb reload` - Reload configuration
- `/[gamemode] admin vb info <player>` - View border info for a player's island
- `/[gamemode] admin vb toggle <player> <all|ceiling|floor>` - Toggle border settings
- `/[gamemode] admin vb update <player>` - Update border location after island move

### Player Commands
- `/[gamemode] border` - View border info
- `/[gamemode] border toggle` - Toggle border on/off
- `/[gamemode] border info` - View border info

---

## Permissions
- `[gamemode].admin.verticalborder` - Access admin commands
- `[gamemode].admin.verticalborder.setheight` - Set border heights
- `[gamemode].admin.verticalborder.reload` - Reload configuration
- `[gamemode].admin.verticalborder.info` - View border info
- `[gamemode].admin.verticalborder.toggle` - Toggle border settings
- `[gamemode].admin.verticalborder.update` - Update border location
- `[gamemode].admin.verticalborder.adjustheight` - **NEW:** Adjust border height
- `[gamemode].border` - Use player border command
- `verticalborder.bypass` - Bypass vertical border restrictions

---

## Notes for Future Development
- When modifying FAWE operations, always ensure they run asynchronously
- The EditSession.close() method is blocking - never call on main thread
- Use protection range (not island range) for barrier placement
- Barriers are placed only at specific Y levels (ceiling/floor), not full vertical walls
- FAWE 2.9.2 is Java 17 compatible; upgrade to 2.11.1+ requires Java 21
- BentoBox versions require -SNAPSHOT suffix in Maven
- Consider adding a custom Flag for island-level permissions in future versions
- The update command stores lastCenterX, lastCenterZ, lastProtectionRange in BorderIslandData to track old barrier locations
- For existing islands without location data, the update command will just place barriers at the current location
