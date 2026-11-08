# Changelog

## 1.10.0 - development
- Added optional Target Range readout to the PvP Target HUD, showing live distance to the living entity under the crosshair in meters.
- World Info HUD now shows distance to a crosshair-targeted block (measured to its center) when Target Range is enabled.
- Added a persisted Target Range toggle to HUD / Layout → Combat; configuration format advanced to version 6 while retaining fallback loading for previous settings.
- Clamped the target card width to scaled screen dimensions and trimmed its text to reduce overflow at small GUI resolutions.
- Verified Entity.distanceTo(Entity) and Vec3d.distanceTo(Vec3d) against Yarn 1.21.11+build.6 documentation. Gradle CI and in-game verification pending for this revision.

## 1.9.0 - development
- Added World Info HUD: current dimension, biome registry key, player light level, chunk coordinates and crosshair-targeted block name/coordinates/light level.
- Added a persisted World Info HUD toggle; config format advanced to version 5 while retaining defaults for older config keys.
- Placed the World Info panel in a reserved lower HUD lane and suppress it at very narrow GUI widths (<400 scaled pixels) to avoid covering the key, armor and resource widgets.
- Reorganized HUD / Layout into Layout, Combat and Status pages so the growing widget set remains usable at smaller GUI scales.
- Verified the required WorldView.getBiome/getLightLevel, RegistryEntry.getKey and BlockHitResult.getBlockPos mappings against Yarn 1.21.11+build.6 documentation.
- GitHub Actions run #106 passed on 2026-11-07; in-game verification remains pending.

## 1.8.0 - development
- Added a low-health vignette visual warning with a red edge tint that activates below 35% health.
- Vignette intensity scales with remaining health and uses a subtle pulse; no mixin or server interaction is required.
- Added a persisted Low-health Vignette toggle to the main settings screen and HUD / Layout; configuration format advanced to version 4 while retaining fallback loading for previous keys.
- Adjusted HUD Layout vertical spacing and panel bounds so its expanded controls do not overlap the heading or Back button.
- GitHub Actions Gradle build and JAR/source ZIP packaging succeeded in run #104 on 2026-11-06; in-game verification remains pending.

## 1.7.0 - development
- Added a Player Resource Bars HUD for health, armor points, hunger/saturation, and XP level/progress.
- Added ratio-based color-coded bars in the existing spatial-glass HUD style.
- Added a persisted Player Resource Bars toggle to HUD / Layout and expanded the control layout to fit.
- GitHub Actions Gradle build, JAR/source ZIP packaging and Release build-101 succeeded on 2026-11-05; in-game verification remains pending.

## 1.6.0 - development
- Added a Potion Effects HUD using the active client-side status effect collection.
- Shows effect names, amplifier levels, effect-color markers and remaining duration, sorted by shortest remaining duration.
- Added warning colors for effects expiring within 10 seconds and support for infinite durations.
- Caps the visible list at eight effects and reports additional effects in an overflow row.
- Added a persisted Potion Effects HUD toggle and repositioned HUD Layout controls.
- Gradle CI and in-game verification pending for this revision.

## 1.5.0 - development
- Added an Armor Durability HUD for helmet, chestplate, leggings and boots.
- Displays each damageable item's remaining durability percentage and a green/yellow/red warning bar.
- Added a persisted Armor Durability HUD toggle to HUD / Layout and adjusted the screen's control spacing.
- GitHub Actions Gradle build succeeded in run #99 on 2026-11-03; in-game verification remains pending.

## 1.4.0 - development
- Added a PvP Target HUD for the living entity under the crosshair, including health text and a health bar.
- Added a Keystrokes HUD for WASD and left/right mouse buttons.
- Added a Totem Counter for totems in the player's inventory.
- Added persistent toggles for all three combat HUD widgets in HUD / Layout.
- GitHub Actions Gradle build succeeded in run #98 on 2026-11-02; in-game verification remains pending.

## 1.3.9 - development
- Added a live left-click CPS counter and compact 32-sample FPS history graph.

## 1.3.8 - development
- Added multiplayer ping to the Compact HUD status row.

## 1.3.7 - development
- Implemented RGB Aura as an animated ring of colored dust particles.

## 1.3.6 - development
- Added HUD Layout controls for coordinates, health/armor, dimension/FPS and four screen-corner positions.
- Persisted HUD layout settings.
- Fixed the Modrinth browser duplicate-blur crash and implemented the Particle Trail toggle.

## 1.3.5 - development
- Prevented the Optimization Center from invoking the vanilla blur pipeline twice.

## 1.3.4 - development
- Fixed the Optimization Center background blur crash.

## 1.3.3 - development
- Fixed Optimization Center duplicate blur rendering.

## 1.3.2 - development
- Added conservative optimization presets and Safe Mode reset.
- Fixed Fullbright gamma maximum and restoration.

## 1.3.1 - development
- Implemented Compact HUD, damage flash and ambient particles.
- Added persistent client settings and improved the main settings layout.

## 1.3.0 - development
- Added the in-game Modrinth browser and mod JAR installer.

## 1.2.0 - development
- Added the lightweight HTTP/HTTPS text browser.

## 1.1.0 - development
- Added Fullbright and hostile-mob ESP toggles.

## 1.0.0
- Initial Fabric client mod, settings screen and prototype visual toggles.

Note: the built-in browser is a lightweight HTML text reader, not a full JavaScript-capable browser. Gradle builds and in-game testing are separate verification steps.
