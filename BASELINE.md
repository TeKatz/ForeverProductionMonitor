# Abgleich der rekonstruierten 2.2.1-Baseline

Referenz: stabile `ForeverProductionMonitor-1.21.1-NeoForge-2.2.1.jar`, SHA-256 `3eaa7c95442432ed816e7215437db026f3b1acdd2c56a9adb91ceda0ca57a9d5`. Sie wurde nur lesend für den Vergleich verwendet.

Prüfstand: `./gradlew clean build --no-daemon` mit Java 21 im GitHub-Actions-Workflow. Der Build von Commit `97f1107172e8780b6e77e76931a7f2ace2add6b9` war erfolgreich; die erzeugte JAR hatte SHA-256 `f8d071f5e7a199d47df1cf57e5b569364fe336c1703da1b7dfc1c9768d2e3dcf`.

| Prüfung | Ergebnis |
| --- | --- |
| Oberste Mod-Klassen und Java-Dateien | 20 von 20 vorhanden |
| Mod-Ressourcen einschließlich Sprachdateien, Texturen, Rezepte und Mod-Metadaten | 22 von 22 bytegleich |
| Klassennamen in der Referenz und im Neubau | 105 gemeinsam; vier zusätzliche Referenzklassen sind synthetische Switch-Hilfsklassen (`$1`) |
| Öffentliche und geschützte Methodennamen samt JVM-Deskriptoren der gemeinsamen Klassen | Keine fehlenden oder zusätzlichen Signaturen |
| Bytecode der gemeinsamen Klassen | Nicht bytegleich; 96 Referenzklassen tragen Java-17-Klassenversion 61 und wurden mit Java-21-Klassenversion 65 neu gebaut. Neun Referenzklassen tragen bereits Version 65. |

Der Abgleich bestätigt Paketstruktur, Ressourcen und die verglichenen Schnittstellen. Er beweist keine vollständige Verhaltensgleichheit im Spiel. Ein Laufzeittest in einer Minecraft-Instanz mit AE2 und Curios sowie ein vollständiger Vergleich aller Methodenabläufe wurden nicht durchgeführt. Die rekonstruierte JAR darf deshalb nicht als byteidentische Kopie der stabilen JAR behandelt werden.
