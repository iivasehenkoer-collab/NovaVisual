# Changelog

## 1.3.6 - development
- Added a dedicated HUD Layout screen with independent coordinate, health/armor and dimension/FPS visibility controls.
- Added four HUD anchor positions: top-left, top-right, bottom-left and bottom-right.
- Persisted the new HUD visibility and anchor settings in config/novavisual.properties.
- Fixed the Modrinth browser blur crash reported by the client log by skipping the vanilla background blur and drawing a custom opaque backdrop.
- Made the existing Particle Trail toggle emit a subtle End Rod trail behind the moving player.
- Updated Modrinth integration User-Agent strings.
- Gradle and in-game verification pending; no binary release is claimed.


## 1.3.5 - development
- Explicitly override the Optimization Center background-render hook to avoid triggering Minecraft's blur pipeline a second time in the same frame.
- Keep the screen's custom opaque background and conservative preset controls.
- Build and in-game verification pending; no binary release claimed.


## 1.3.4 - development
- Fixed the Optimization Center blur crash by overriding the vanilla renderBackground hook so the custom opaque background does not trigger a second blur pass.
- Retained conservative render/simulation presets and Safe Mode reset.
- Build and in-game verification are pending; no release binary is claimed.


## 1.3.3 - development
- Fixed the Optimization Center blur crash by overriding the vanilla renderBackground hook with a no-op; the screen keeps its own opaque custom background.
- Retained conservative render/simulation presets and the Safe Mode reset from 1.3.2.
- No binary release is published until the repository build passes.


## 1.3.2 - development
- Fixed Optimization Center rendering crash by using a custom opaque backdrop instead of the vanilla blur request.
- Changed optimization presets to conservative render/simulation distances: 6/4, 8/5 and 10/6 chunks.
- Added Safe Mode reset to 12 render and 8 simulation chunks.
- Save optimization settings after applying or resetting.
- Fixed Fullbright gamma from invalid 16.0 to 1.0 and retained restoration of the previous value.


## 1.3.1 - development
- Implemented the Compact HUD with player coordinates, health, armor, dimension and FPS.
- Implemented a short red edge flash when the local player's health decreases.
- Implemented low-frequency ambient End Rod particles around the local player.
- Added persistent client settings in config/novavisual.properties.
- Added settings save hooks for screen transitions and client shutdown.
- Reworked the main settings panel into two columns and fixed overlapping navigation controls.
- Documented the chat-based event marker and the limits of the local PvP combat estimate.

## 1.3.0 - development
- Added an in-game Modrinth browser for Fabric mods compatible with Minecraft 1.21.11.
- Added paginated mod search and one-click JAR installation into the active instance's mods folder.
- Added basic download validation and collision-safe filenames.

## 1.2.0 - development
- Added an in-game lightweight Internet screen.
- Added HTTP/HTTPS address navigation, Back, Refresh and Home controls.
- Added readable HTML text extraction and clickable page links.
- Added scrolling for long page text.
- Bundled Jsoup for HTML parsing.

## 1.1.0 - development
- Added a Fullbright toggle that raises client gamma while enabled and restores the previous gamma when disabled.
- Added Enemy ESP for hostile mobs using the vanilla glowing outline.
- Added both controls to the NovaVisual settings screen.

## 1.0.0
- Initial Fabric client mod project for Minecraft Java Edition 1.21.11.
- Added NovaVisual settings screen, opened with Right Shift.
- Added prototype toggles for animated menu, compact HUD, damage flash and ambient particles.
- Added Nova Blue / Nova Violet accent selection.

Note: the built-in Internet screen is a lightweight HTML text reader, not a full JavaScript-capable browser. Gradle builds and in-game testing are separate verification steps.
