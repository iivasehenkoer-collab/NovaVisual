# NovaVisual

A client-side visual and utility mod for Minecraft Java Edition 1.21.11 using Fabric.

## Version 1.11.0 development

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
- World Info HUD with dimension, biome key, local light level, chunk coordinates, and crosshair-targeted block coordinates/light; uses a reserved lower HUD lane at GUI widths of 400+ to avoid widget collisions
- Compass HUD: yaw-relative cardinal/intercardinal tape, heading degrees and center tick, with scaled-screen width handling
- HUD Layout grouped into Layout, Combat and Status pages to fit controls at smaller GUI scales
- Damage flash, ambient particles, RGB Aura and moving particle trail
- Nova Blue / Nova Violet accent, animated menu and Fullbright
- Hostile-mob outline toggle (players are not targeted)
- Client-side 3D cosmetic wings with selectable styles and RGB animation

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
Run `gradle build`; the remapped JAR should be in `build/libs/`.

GitHub Actions run #109 successfully built the 1.10.0 source revision (commit 1255854) on 2026-11-08. Version 1.11.0 adds a persisted yaw-relative Compass HUD; CI build and in-game verification are pending for this revision.
