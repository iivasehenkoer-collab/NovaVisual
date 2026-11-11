# NovaVisual

A client-side visual and utility mod for Minecraft Java Edition 1.21.11 using Fabric.

## Version 1.13.0 development

### Visual features
- Spatial-glass settings screen, opened with **Right Shift**
- Compact HUD with coordinates, health, armor, dimension, FPS, ping and CPS
- Compact FPS history graph
- PvP Target HUD with target name, health and health bar
- Target Range HUD: live distance to the living entity under the crosshair; block-target distance is also shown in World Info HUD when enabled
- Keystrokes HUD with WASD and left/right mouse button indicators
- Totem Counter showing carried totems
- Armor Durability HUD with equipment icons, remaining durability percentages and warning-colored bars
- Potion Effects HUD with effect-colored markers, amplifier levels, countdowns and expiry warnings
- Player Resource Bars HUD for health, armor, hunger/saturation and XP level/progress
- Low-health Vignette with pulsing red edge tint below 35% health
- World Info HUD with dimension, biome key, local light level, chunk coordinates, and crosshair-targeted block coordinates/light
- Compass HUD: yaw-relative cardinal/intercardinal tape, heading degrees and center tick
- **Waypoint HUD:** multiple named, persistent destinations; the active point shows a live relative arrow, horizontal distance, saved XYZ and vertical offset, with dimension-aware navigation
- **Waypoint Manager:** save named locations, select the active destination, delete points and browse up to 12 saved waypoints
- **F8** updates the active waypoint at the player's current position (creates Quick if none is active); **F9** deletes the active waypoint
- HUD Layout grouped into Layout, Combat and Status pages, including a Waypoint HUD toggle
- Damage flash, ambient particles, RGB Aura and moving particle trail
- Nova Blue / Nova Violet accent, animated menu and Fullbright
- Hostile-mob outline toggle (players are not targeted)
- Client-side 3D cosmetic wings with selectable styles and RGB animation

### Waypoint Manager
- Open **Right Shift → Waypoint Manager**.
- Enter a name such as Home, Base, Event or Farm, then choose **Save here**.
- Saving an existing name (case-insensitive) updates that waypoint instead of creating a duplicate.
- Select any saved row to make it the active navigation target; use **Delete** to remove a point.
- Up to 12 waypoints are stored in config/novavisual-waypoints.properties, separately from visual HUD preferences.
- Existing single-waypoint data from version 1.12.0 is migrated to a point named Waypoint on first launch.
- **F8** updates the selected point in place; if no point is active, F8 creates Quick. **F9** removes the active point.

### Server and utility features
- HolyWorld event marker reads chat announcements containing X/Z coordinates
- PvP disconnect warning based on locally observed player damage activity; not an authoritative server combat-tag check
- Optimization Center with conservative render/simulation presets and Safe Mode reset
- In-game Modrinth browser and one-click compatible Fabric mod JAR installation
- Lightweight HTTP/HTTPS text browser (not a JavaScript-capable Chromium browser)

### Requirements
- JDK 21
- Minecraft Java Edition 1.21.11
- Fabric Loader 0.19.0+
- Fabric API for 1.21.11

### Build
Run gradle build; the remapped JAR should be in build/libs/.

Version 1.13.0 adds a named multi-waypoint manager and keeps saved destinations separate from visual preferences. GitHub Actions build verification and in-game verification are separate; consult the Actions run and Release linked from the repository before installing a binary.
