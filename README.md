# NovaVisual

A client-side visual settings mod for Minecraft Java Edition 1.21.11 using Fabric.

## Version 1.0.0

**Status:** source prototype; gameplay effects are not implemented yet.

### Included
- NovaVisual settings screen (Right Shift)
- Animated menu, compact HUD, damage flash and ambient-particle prototype toggles
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

The 1.0.0 switches are UI prototypes; HUD rendering, damage flash and ambient particle effects are planned for later releases.
