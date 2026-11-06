# NovaVisual

A client-side visual and utility mod for Minecraft Java Edition 1.21.11 using Fabric.

## Version 1.8.0 development

### Visual features
- Spatial-glass settings screen, opened with **Right Shift**
- Compact HUD with coordinates, health, armor, dimension, FPS, ping and CPS
- Compact FPS history graph
- PvP Target HUD with target name, health and health bar
- Keystrokes HUD with WASD and left/right mouse button indicators
- Totem Counter showing carried totems
- Armor Durability HUD with four equipment icons, remaining durability percentages and warning-colored bars
- Potion Effects HUD with effect-colored markers, amplifier levels, countdowns and expiry warnings
- Player Resource Bars HUD for health, armor, hunger/saturation and XP level/progress
- Low-health Vignette with pulsing red edge tint below 35% health, intensity scaling toward critical health
- HUD Layout controls with independently persisted widget toggles, including Armor Durability, Potion Effects, Resource Bars and Low-health Vignette; expanded screen spacing to fit the additional control
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

The 1.7.0 Player Resource Bars source revision was built successfully by GitHub Actions (run #101, 2026-11-05); the JAR and source ZIP were published in the build-101 prerelease. The 1.8.0 low-health vignette revision requires CI build confirmation and in-game testing.
