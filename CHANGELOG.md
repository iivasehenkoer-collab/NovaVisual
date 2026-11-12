# Changelog

## 1.13.2 - development
- Fullbright now captures the user's gamma before applying maximum brightness and persists that restore value in `novavisual.properties`.
- Disabling Fullbright restores the captured gamma even when Minecraft was restarted while Fullbright remained enabled; malformed or out-of-range stored values safely fall back/clamp.
- Advanced the settings format to version 9 while preserving backward-compatible defaults.
- GitHub Actions run #43199223460 completed successfully: Gradle build, source packaging, artifact upload and prerelease publication all passed. In-game verification has not been performed.

## 1.13.1 - development
- Added shared NovaGlassUi primitives for rounded translucent panels, glass cards, subtle highlights and ambient cyan/violet glow.
- Refreshed the main menu, HUD Layout, Waypoint Manager, Optimization Center, Cosmetics Studio, Modrinth browser and lightweight browser with a consistent visionOS-inspired visual language.
- Fixed the glass highlight ARGB color constant; no shaders, mixins or third-party visual assets are required.
- GitHub Actions build succeeded for the glass UI fix in run #43076946094; in-game verification has not been performed.

## 1.13.0 - development
- Replaced the single-waypoint model with a persistent collection of up to 12 named waypoints.
- Added the Waypoint Manager screen: save the current location under a custom name, select the active destination, delete entries and page through the saved list.
- Saving an existing name (case-insensitive) updates that entry; F8 updates the active point in place or creates Quick when none is active, and F9 deletes the active point.
- Moved waypoint persistence to config/novavisual-waypoints.properties so waypoint data remains separate from visual preferences.
- Migrates the legacy single waypoint from the 1.12.0 settings file on first launch.
- Updated the Waypoint HUD to display the active waypoint name and preserve dimension-aware navigation.
- Gradle CI and in-game verification are separate checks for this revision.

## 1.12.0 - development
- Added a single persistent client waypoint: F8 saves/replaces the current block position and F9 clears it.
- Added a dimension-aware Waypoint HUD with a relative forward/left/right/back arrow, horizontal distance, saved XYZ and vertical offset.
- The waypoint is stored in novavisual.properties and survives client restarts; navigation is suppressed in other dimensions and the HUD identifies the saved dimension.
- Added a persisted Waypoint HUD toggle to the main menu and HUD Layout → Status; configuration format advanced to version 8 while keeping previous keys backward-compatible.
- Added keybinding translations and updated README usage/build notes. Gradle CI and in-game verification are pending for this revision.

## 1.11.0 - development
- Added a yaw-relative Compass HUD with cardinal/intercardinal direction labels, heading degrees and a highlighted center tick.
- Added a persisted Compass HUD toggle to the main NovaVisual menu; configuration format advanced to version 7 with backward-compatible defaults for earlier keys.
- Kept the compass in a top-center lane below the PvP Target HUD and adjusted its width/tick visibility for scaled GUI dimensions.
- Run #112 successfully built and packaged version 1.11.0; in-game verification remains pending.

## 1.10.0 - development
- Added optional Target Range readout to the PvP Target HUD, showing live distance to the living entity under the crosshair in meters.
- World Info HUD now shows distance to a crosshair-targeted block (measured to its center) when Target Range is enabled.
- Added a persisted Target Range toggle to HUD / Layout → Combat; configuration format advanced to version 6 while retaining fallback loading for previous settings.
- Clamped the target card width to scaled screen dimensions and trimmed its text to reduce overflow at small GUI resolutions.
- Verified Entity.distanceTo(Entity) and Vec3d.distanceTo(Vec3d) against Yarn 1.21.11+build.6 documentation.

## 1.9.0 - development
- Added World Info HUD: current dimension, biome registry key, player light level, chunk coordinates and crosshair-targeted block name/coordinates/light level.
- Added a persisted World Info HUD toggle; config format advanced to version 5 while retaining defaults for older config keys.
- Placed the World Info panel in a reserved lower HUD lane and suppress it at very narrow GUI widths (<400 scaled pixels) to avoid covering the key, armor and resource widgets.
- Reorganized HUD / Layout into Layout, Combat and Status pages so the growing widget set remains usable at smaller GUI scales.

## 1.8.0 - development
- Added a low-health vignette visual warning with a red edge tint that activates below 35% health.
- Vignette intensity scales with remaining health and uses a subtle pulse; no mixin or server interaction is required.
- Added a persisted Low-health Vignette toggle to the main settings screen and HUD / Layout; configuration format advanced to version 4 while retaining fallback loading for previous keys.
- Adjusted HUD Layout vertical spacing and panel bounds so its expanded controls do not overlap the heading or Back button.

## 1.7.0 - development
- Added a Player Resource Bars HUD for health, armor points, hunger/saturation, and XP level/progress.
- Added ratio-based color-coded bars in the existing spatial-glass HUD style.
- Added a persisted Player Resource Bars toggle to HUD / Layout and expanded the control layout to fit.

## 1.6.0 - development
- Added a Potion Effects HUD using the active client-side status effect collection.
- Shows effect names, amplifier levels, effect-color markers and remaining duration, sorted by shortest remaining duration.
- Added warning colors for effects expiring within 10 seconds and support for infinite durations.
- Caps the visible list at eight effects and reports additional effects in an overflow row.

## 1.5.0 - development
- Added an Armor Durability HUD for helmet, chestplate, leggings and boots.
- Displays each damageable item's remaining durability percentage and a green/yellow/red warning bar.

## 1.4.0 - development
- Added a PvP Target HUD for the living entity under the crosshair, including health text and a health bar.
- Added a Keystrokes HUD for WASD and left/right mouse buttons.
- Added a Totem Counter for totems in the player's inventory.

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
