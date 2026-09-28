# Changelog

All notable public release changes for Forever Production Monitor are documented here.

## 5.0.0

### Crafting Diagnostics
- Added a dedicated **Crafting Diagnostics** workspace for active jobs, encoded patterns and pattern providers.
- Added direct crafting requests from diagnostics with AE-style additive amount controls and clear/reset handling.
- Added pattern input/output inspection, duplicate-output alternatives, provider details and reverse dependency traversal.
- Added provider location handoff into the existing dimension-aware Network Map.
- Added bounded, cycle-safe diagnostics snapshots with explicit partial-result handling for very large networks.

### Themes and interface
- Expanded the theme system with substantially richer visual identities and animations.
- Reworked **Cats**, **Carbon Industrial**, **Oritech**, **Forever**, **Mekanism**, **AE2** and **Quantum** presentation.
- Added interactive theme elements where appropriate, including the Cats theme eye tracking and click-heart reaction.
- Added a full **Custom Theme Designer** with live preview, configurable colors, backgrounds, particles, tabs, buttons and animation styling.
- Added a dedicated **Custom Mini HUD Designer** with independent palette, structure and decorative styles.
- Removed the Aurora theme.

### Mini HUD
- Reworked Mini HUD settings into compact **General / Content / Layout / Appearance** sections with a permanent live preview.
- Added preview zoom controls and centered preview rendering.
- Added content filters for items, fluids, energy and infinite/creative storage.
- Added stored-amount display sorting for highest-first and lowest-first workflows.
- Added independent Custom Mini HUD styling while preserving interface-theme matching.
- Improved theme-aware framing and layout at different GUI scales.

### Compatibility and validation
- Minecraft 1.21.1
- NeoForge 21.1.249+
- Applied Energistics 2 19.2.17 up to, but not including, 20.0.0
- Curios 9.5.1+
- ExtendedAE remains optional.
- 5.0.0 was manually tested in-game before release.

## 4.0.0

### Network Map
- Added and stabilized multidimensional AE2 Network Map navigation.
- Added AE2 Quantum Bridge **Follow Path** navigation between dimensions.
- Added ExtendedAE ME Wireless Connector **Follow Path** navigation inside the same dimension.
- Added detected-dimension selection for a single logical AE2 grid.
- Added per-dimension remembered camera state and persistent camera bookmarks.
- Added session Network Events for topology, power and channel changes.
- Added large-map rendering optimizations, viewport culling and reused projection calculations.
- Optimized cable heatmap calculations and made them lazy.
- Improved Event panel text handling for long names and messages.

### Diagnostics
- Added cross-dimensional Missing Channel location handling so the locator reports the device's real dimension.
- Preserved network-wide diagnostics for AE2 grids connected through Quantum Bridges.

### Stability and maintenance
- Reduced temporary server-side Network Map data retained for dimensions that are not being rendered.
- Split Network Map path resolution and derived rendering data into dedicated components.
- Scoped session-only event and automatic camera state to the active client connection.
- Removed an unused legacy config screen.
- Version display now comes from loaded mod metadata instead of a hard-coded string.

### Project
- Added an MIT license.
- Added transparent AI-development disclosure and public project documentation.
- Verified 4.0.0 in-game before accepting it as the current stable baseline.

## Development history before 4.0.0

The repository began from a reconstruction of Timo's own stable 2.2.1 build. Subsequent internal versions introduced storage diagnostics, channel/device views, dashboard and alarms, themes and animations, the 3D Network Map, multidimensional navigation and ExtendedAE Wireless Connector support before being consolidated into the 4.0.0 public-release baseline.
