# Changelog

All notable public release changes for Forever Production Monitor are documented here.

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
