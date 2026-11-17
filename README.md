# NovaVisual

A client-side visual and utility mod for Minecraft Java Edition 1.21.11 using Fabric.

## Version 1.19.0 development

### PvP activity detection and disconnect warning
- Refined the combat activity estimate to use player-attributed attacker state instead of treating every health decrease as PvP.
- A local player's fresh damage event only starts the warning when the recorded attacker is another player.
- Damage animations from other players only count when the local player is recorded as their attacker, preventing unrelated fights elsewhere in the loaded world from triggering the warning.
- Resets combat tracking when changing worlds, disconnecting, or disabling the feature; re-enabling it takes a fresh baseline instead of comparing stale health/hurt-time values.
- Added a compact remaining-time progress bar to the existing warning banner. The 15-second timer remains a client-side estimate, not an authoritative server combat-tag check.
- Existing **PvP exit warning** setting and Game Menu disconnect confirmation remain in place; no configuration migration is required.

## Version 1.18.0 development

### PvP Target HUD improvements
- Expanded the crosshair target card with live armor points and absorption health when present.
- The health bar now changes color at low-health thresholds and renders absorption as a separate amber segment.
- The target card's accent briefly changes while the target is in its hurt animation; this is a visual target-state cue, not proof that the local player caused the damage.
- Fixed the main menu's duplicate decorative labels being painted over real buttons. The existing animated-menu option now applies a subtle, short entrance scale to the actual panel and controls.

## Version 1.17.0 development
- Tracks active potion effects by registry ID and shows a short HUD notification when an effect is applied, its amplifier changes, or it ends.
- Takes an initial snapshot on world entry, so existing effects do not generate a burst of false notifications.
- Added a persistent **Potion Effect Alerts** toggle to HUD / Layout → Status and an independently movable, scalable and opacity-adjustable HUD Editor widget.
- Settings format advances to version 14; older settings keep the enabled-by-default behavior.
- Enemy ESP remembers each hostile mob's previous glowing state and restores it when the mob leaves the active set or the feature is disabled.

## Version 1.16.0 development
- Detects when equipped damageable armor crosses below 20% remaining durability and shows a short warning card.
- Identifies the affected equipment slot and item; the warning fades automatically and can be moved, resized and made transparent in the HUD Editor.
- Added a persistent **Low Durability Alert** toggle to HUD / Layout → Status. The detector initializes from the currently equipped gear and does not warn just because a low-durability item was already equipped when joining a world.
- Settings format advances to version 13; older settings remain backward-compatible.

## Version 1.15.0 development
- Added ready-made PvP, Anarchy and Survival layouts directly to the HUD Editor.
- Each profile configures widget visibility, screen placement, size, opacity and accent color.
- PvP focuses on target/keystrokes/armor; Anarchy adds compass, waypoint, world information and resource panels; Survival emphasizes resources, navigation and player status.
- The selected profile is persisted. Any manual HUD position, scale, opacity or HUD visibility change switches the indicator to Custom.
- Existing custom layouts are kept as Custom on upgrade; presets are applied only when selected.

## Version 1.14.0 development
- Detects a local totem consumption by correlating a totem-count decrease with recent player damage and the vanilla 1 HP totem recovery state.
- Shows a short animated alert with the session pop count and remaining totems.
- Adds the alert as a draggable, scalable and opacity-adjustable HUD Editor widget; toggle it under **HUD / Layout → Combat**.

## Version 1.13.3 development
- Added a mouse-driven editor for independently moving HUD widgets.
- Added per-widget size controls (50%–200%), opacity controls (20%–100%) and a reset action.
- Positions are normalized to the scaled GUI size and saved in config/novavisual.properties.

## Version 1.13.2 development
- Fullbright remembers the gamma value that was active before it was enabled.
- The saved value is stored in config/novavisual.properties and restored when Fullbright is disabled, including after restarting Minecraft while the feature was enabled.

## Version 1.13.1 development
- Added shared rounded glass surfaces, translucent layered panels, subtle edge highlights and cyan/violet ambient glow.
- Applied the shared glass UI system to the NovaVisual home screen, HUD settings, waypoint manager, Optimization Center, Cosmetics Studio, Modrinth browser and lightweight web browser.

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
- **F8** updates the active waypoint at the player's current position (creates Quick if none is active); **F9** deletes the active point
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
- PvP disconnect warning based on locally observed player-attributed damage activity; not an authoritative server combat-tag check
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

GitHub Actions build verification and in-game verification are separate; consult the Actions run and Release linked from the repository before installing a binary.
