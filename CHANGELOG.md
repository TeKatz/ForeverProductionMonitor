# Changelog

All notable public release changes for Forever Production Monitor are documented here.

## 5.4.1-SNAPSHOT (development, not released)

- Replaces independent simultaneous build-kit jobs with a server-side queue: consumer components are crafted first, each next calculation uses current stock after the previous job finishes, and completion checks the entire physical build kit again.
- Accounts for kit components consumed as ingredients in other kit recipes; one crafting CPU can now process the complete batch. Busy CPUs are waited for within a bounded window; missing ingredients and cancelled jobs remain visible failures.
- Prevents duplicate batches per player and clears pending queue calculations on disconnect/server stop. Already submitted AE2 jobs remain ordinary player jobs and are not deleted on disconnect.
- Adds explicit queued/completed UI feedback and regression checks for shared-component ordering and cyclic alternatives.
- Adds a narrow client mixin guard for AE2 19.2.x's negative pin-list range when a tenth distinct crafting result is pinned. The oldest pin is evicted before AE2's existing method runs; player/security action sources are unchanged.
- Pair with DeepCore 0.8.2-SNAPSHOT for rebuilt-controller Survey catalogue reuse and removal of four unreferenced, corrupt legacy PNGs.
- Network Map renderer and Pattern Planner import rules remain unchanged. Runtime verification is pending.

## 5.4.0-SNAPSHOT (development, not released)

- Integrates the existing Deep Core Construction Planner, Multiblock Constructor and JEI Pattern Planner queue on the in-game-tested 5.2.0 baseline (`87c299b`).
- Preserves the 5.0.1 Network Map crash and warning-layout hotfix; `NetworkMapView.java` is unchanged from that baseline.
- Construction Planner requests complete Tier I-III build kits and subtracts matching player-inventory and ME-stock items before requesting missing components.
- Multiblock Constructor supports Preview / Build / Dismantle, reuses existing matching blocks, consumes player inventory before ME stock, and preserves the previously implemented AE2 Drive orientation fix.
- JEI Crafting/Smithing buttons support immediate import, Shift-click queue toggling and Ctrl-click upload to an ExtendedAE Assembler Matrix. Processing patterns remain blocked on both client and server.
- Requires the paired DeepCore 0.8.1-SNAPSHOT construction API build for construction features. JEI and ExtendedAE remain optional for the rest of FPM.
- The separate 5.3.0 Fleet Diagnostics work is not included. The higher snapshot version distinguishes this integration from both that branch and earlier 5.2.0 builds.
- Runtime acceptance of this new combined build is pending. See [test instructions](docs/constructor-pattern-planner-5.4.0.md).

## 5.0.1

### Network Map stability
- Fixed GitHub issue **#35**, where opening the Network Topology View could crash the client with `IllegalStateException: Not building!` while rendering multipart geometry.
- Separated `RenderType.debugQuads()` face rendering from `RenderType.lines()` outline rendering so each buffer is acquired, written and flushed in its own pass.
- Moved the large-network truncation warning out of the filter row and into the bottom control bar.
- Made the full truncation warning scale to the available control-bar width instead of overlapping nearby controls or being cut off at larger GUI scales.
- Verified the fix in a fresh minimal test instance and in an ATM10TTS end-game world, including Network Map rendering, Heatmap, hover/selection, rotation and zoom.

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
- NeoForge 21.1.249+; **5.0.0 stable tested successfully with NeoForge 21.1.252**
- Applied Energistics 2 19.2.17 up to, but not including, 20.0.0; **5.0.0 stable tested successfully with AE2 19.2.18**
- Curios 9.5.1+
- ExtendedAE remains optional.
- 5.0.0 was manually tested in-game before release and subsequently re-tested successfully with AE2 19.2.18 and NeoForge 21.1.252 on Minecraft 1.21.1.

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

The repository began from a reconstruction of TeKatz's own stable 2.2.1 build. Subsequent internal versions introduced storage diagnostics, channel/device views, dashboard and alarms, themes and animations, the 3D Network Map, multidimensional navigation and ExtendedAE Wireless Connector support before being consolidated into the 4.0.0 public-release baseline.
