# Changelog

## 1.3.8 - development
- Added live multiplayer ping to the Compact HUD status row using the local player's player-list entry.
- Shows an em dash when no multiplayer ping entry is available (singleplayer or unavailable player list).
- Kept the HUD setting persisted and the existing status-row toggle controls the new ping field.
- Gradle build and in-game verification pending; no binary release is claimed.

## 1.3.7 - development
- Implemented RGB Aura as an animated ring of colored dust particles around the local player.
- Reused the persisted rgbAura setting; no config migration required.
- Gradle build and in-game verification are tracked separately.

## 1.3.6 - development
- Added HUD Layout controls for coordinates, health/armor, dimension/FPS and four HUD anchor positions.
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
