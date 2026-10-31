# NovaVisual

A client-side visual and utility mod for Minecraft Java Edition 1.21.11 using Fabric.

## Version 1.3.0 development

### Features
- NovaVisual settings screen (Right Shift)
- Fullbright toggle using the client gamma setting
- Enemy ESP toggle for hostile mobs using the vanilla glowing outline
- In-game Modrinth browser: search Fabric mods compatible with Minecraft 1.21.11
- One-click mod JAR download and installation into the game's `mods` folder
- In-game lightweight Internet screen with address bar, Back, Refresh, Home and clickable links
- Lightweight HTML text extraction using Jsoup
- Nova Blue / Nova Violet accent selector

### Modrinth installer notes
The Modrinth screen searches projects marked as mods with Fabric support for Minecraft 1.21.11. It downloads the selected compatible JAR into the current Minecraft instance's `mods` folder. Restart Minecraft to load newly installed mods. Check each mod's description and dependencies before installing. Only install mods you trust.

### Browser limitations
The built-in browser is a lightweight HTML text reader, not Chromium. It displays readable page text and a selection of links, but does not execute JavaScript, render full CSS layouts, play video, or support complex web applications. YouTube and other JavaScript-heavy websites may not work as expected. Only HTTP and HTTPS addresses are accepted.

### Requirements
- JDK 21
- Minecraft Java Edition 1.21.11
- Fabric Loader 0.19.0+
- Fabric API for 1.21.11

### Build
Run `gradle build` from this directory. The remapped mod JAR should be in `build/libs/`.

### Install
Install Fabric Loader and Fabric API for Minecraft 1.21.11, then place the built JAR in `.minecraft/mods`.

This development version requires a successful Gradle build and in-game testing before it can be called verified.
