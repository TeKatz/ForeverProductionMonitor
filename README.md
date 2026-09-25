# Forever Production Monitor

Forever Production Monitor is a NeoForge 1.21.1 companion mod for Applied Energistics 2.
It provides production/storage statistics, channel diagnostics, alarms, a Mini HUD and a
3D Network Map with multidimensional Quantum Bridge navigation and ExtendedAE Wireless
Connector path navigation.

## Current development line

Version 4.0.0 is the stability, architecture and performance pass after the 3.x Network Map
feature series. The goal is to preserve the user-facing 3.5.0 behaviour while reducing work
performed by large Network Maps and separating map-specific responsibilities that had grown
inside the main renderer.

Notable 4.0.0 maintenance work:

- cached map filter/search results instead of re-evaluating them several times per render pass,
- viewport culling for large 3D maps,
- a spatial cable-load calculation instead of comparing every cable with every device,
- reduced temporary server-side map-source retention for non-rendered dimensions,
- isolated Quantum Bridge / Wireless Connector path resolution,
- connection-scoped event baselines and remembered camera state,
- removed the unused legacy config screen,
- settings version text now comes from the loaded mod metadata instead of a hard-coded string.

The original project source was reconstructed from the stable 2.2.1 JAR. The historical
baseline verification remains documented in [BASELINE.md](BASELINE.md).

## Runtime target

- Minecraft 1.21.1
- NeoForge 21.1.249
- Applied Energistics 2 19.2.17
- Curios 9.5.1+1.21.1 (optional integration)
- Java 21

ExtendedAE integration is optional and does not create a hard runtime dependency.

## Build

Use Java 21:

```sh
./gradlew clean build --no-daemon
```

On Windows:

```bat
gradlew.bat clean build --no-daemon
```

The produced mod JAR is written to `build/libs/`. GitHub Actions performs the same clean
Java 21 build and uploads the resulting JAR as an artifact.
