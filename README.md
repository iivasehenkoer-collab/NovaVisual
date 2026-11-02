# NovaVisual

A client-side visual and utility mod for Minecraft Java Edition 1.21.11 using Fabric.

## Version 1.3.8 development

### Visual features
- NovaVisual spatial-glass settings screen, opened with **Right Shift**
- Compact HUD with coordinates, health, armor, dimension, FPS and multiplayer ping
- HUD Layout screen: independently show/hide coordinate, health/armor and dimension/FPS/ping rows
- Move the compact HUD between four screen corners
- Damage flash when the local player's health decreases
- Optional ambient End Rod particles orbiting the local player
- Optional End Rod particle trail behind the player while moving
- RGB Aura: animated multicolor dust-particle ring around the player
- Animated menu and Nova Blue / Nova Violet accent
- Fullbright toggle that restores the previous gamma value when disabled
- Hostile-mob outline toggle (players are not targeted)
- Client-side 3D cosmetic wings with selectable styles and RGB animation

### Server and utility features
- HolyWorld event marker reads event announcements from chat when they contain X/Z coordinates
- PvP disconnect warning based on locally observed player damage activity; this is an estimate, not an authoritative server combat-tag check
- Optimization Center with conservative render/simulation presets and Safe Mode reset
- In-game Modrinth browser for Fabric mods compatible with Minecraft 1.21.11
- One-click mod JAR download and installation into the active game's mods folder
- Lightweight Internet screen with HTTP/HTTPS navigation, readable page text and clickable links

### Settings
Client preferences are stored in **config/novavisual.properties** and saved when settings screens close or the Minecraft client shuts down.

### Requirements
- JDK 21
- Minecraft Java Edition 1.21.11
- Fabric Loader 0.19.0+
- Fabric API for 1.21.11

### Build
Run `gradle build` from this directory. The remapped mod JAR should be in `build/libs/`.

### Install
Install Fabric Loader and Fabric API for Minecraft 1.21.11, then place the built JAR in `.minecraft/mods`.

The project requires in-game testing on the target Minecraft client and server. A successful Gradle build alone does not verify gameplay behaviour or server compatibility.
