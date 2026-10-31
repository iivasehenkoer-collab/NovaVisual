# NovaVisual

A client-side visual settings mod for Minecraft Java Edition 1.21.11 using Fabric.

## Version 1.1.0 development

### Included / in development
- NovaVisual settings screen (Right Shift)
- Fullbright toggle using the client gamma setting
- Enemy ESP toggle for hostile mobs using Minecraft's glowing outline
- Existing prototype toggles: animated menu, compact HUD, damage flash and ambient particles
- Nova Blue / Nova Violet accent selector
- Client-only, non-pausing settings screen

### Requirements
- JDK 21
- Minecraft Java Edition 1.21.11
- Fabric Loader 0.19.0+
- Fabric API for 1.21.11

### Build
Run `gradle build` from this directory. The remapped mod JAR should be in `build/libs/`.

### Install
Install Fabric Loader and Fabric API for Minecraft 1.21.11, then place the built JAR in `.minecraft/mods`.

## Project layout
- `src/main/java` — Java source
- `src/main/resources` — Fabric metadata and translations
- `builds/` — version source archives

The Fullbright and Enemy ESP code is now implemented in the client tick handler, but the project still requires a successful Gradle build and in-game test before this development version can be called verified.
