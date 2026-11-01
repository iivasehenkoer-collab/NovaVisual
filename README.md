# NovaVisual

A client-side visual and utility mod for Minecraft Java Edition 1.21.11 using Fabric.

## Version 1.3.1 development

### Visual features
- NovaVisual spatial-glass settings screen, opened with **Right Shift**
- Compact HUD with coordinates, health, armor, dimension and FPS
- Damage flash when the local player's health decreases
- Optional ambient End Rod particles around the local player
- Animated menu and Nova Blue / Nova Violet accent
- Fullbright toggle that restores the previous gamma value when disabled
- Hostile-mob outline toggle (players are not targeted)
- Client-side 3D cosmetic wings with selectable styles and RGB animation

### Server and utility features
- HolyWorld event marker that reads event announcements from chat when they contain X/Z coordinates
- PvP disconnect warning based on locally observed player damage activity; this is an estimate, not an authoritative server combat-tag check
- Optimization Center presets for vanilla render and simulation distance
- In-game Modrinth browser for Fabric mods compatible with Minecraft 1.21.11
- One-click mod JAR download and installation into the active game's mods folder
- Lightweight Internet screen with HTTP/HTTPS navigation, readable page text and clickable links

### Settings
NovaVisual loads and saves client preferences in **config/novavisual.properties**.

Settings are loaded when the client mod initializes and saved when settings screens close or the Minecraft client shuts down.

### Modrinth installer notes
The Modrinth screen searches projects marked as mods with Fabric support for Minecraft 1.21.11. It downloads the selected compatible JAR into the current Minecraft instance's mods folder. Restart Minecraft to load newly installed mods. Check each mod's description and dependencies before installing. Only install mods you trust.

### Browser limitations
The built-in browser is a lightweight HTML text reader, not Chromium. It displays readable page text and a selection of links, but does not execute JavaScript, render full CSS layouts, play video, or support complex web applications. YouTube and other JavaScript-heavy websites may not work as expected. Only HTTP and HTTPS addresses are accepted.

### Event marker limitations
The HolyWorld marker is chat-based. It only creates a waypoint when a matching event announcement includes recognizable X/Z coordinates. It does not poll a HolyWorld API and cannot infer event locations from messages without coordinates.

### Requirements
- JDK 21
- Minecraft Java Edition 1.21.11
- Fabric Loader 0.19.0+
- Fabric API for 1.21.11

### Build
Run gradle build from this directory. The remapped mod JAR should be in build/libs/.

### Install
Install Fabric Loader and Fabric API for Minecraft 1.21.11, then place the built JAR in .minecraft/mods.

This development version still requires in-game testing on the target Minecraft client and server. A successful Gradle build alone does not verify gameplay behaviour or server compatibility.
