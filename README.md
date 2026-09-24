# Forever Production Monitor – rekonstruierte 2.2.1-Baseline

Dieses Projekt enthält die aus der stabilen Version 2.2.1 rekonstruierten Java-Quellen und ihre Ressourcen. Die Referenz-JAR gehört nicht zum Quellprojekt und wird nicht gepatcht. Diese Baseline enthält keine beabsichtigten Funktionsänderungen, neuen Features oder Optimierungen.

## Build

Voraussetzung: Java 21 und Zugang zu den in `settings.gradle` und `build.gradle` angegebenen Gradle- und Maven-Repositories.

```sh
./gradlew clean build --no-daemon
```

Unter Windows: `gradlew.bat clean build --no-daemon`. Die Mod-JAR liegt anschließend in `build/libs/`. Der GitHub-Actions-Workflow führt denselben Build mit Java 21 aus und lädt die JAR als Artefakt hoch.

Zielplattform: Minecraft 1.21.1, NeoForge 21.1.249, AE2 19.2.17 und Curios 9.5.1+1.21.1.

Der [Baseline-Abgleich](BASELINE.md) beschreibt die geprüfte Übereinstimmung mit der stabilen 2.2.1 und die Grenzen dieses Nachweises.
