# Damage Teleport

Fabric mod for Minecraft 1.21.1 that randomly teleports damaged players between 5 and 50 blocks away. Mostly useful for challenge-focussed modpacks.

## Building

1. Install Java 21 and Gradle 8.8 or newer (the Gradle wrapper is not committed yet).
2. From the repository root run:
   ```powershell
   gradle build
   ```
3. The compiled jar will be created at `build/libs/damage-teleport-1.0.0.jar`.

## Installation

1. Install Fabric Loader 0.16.5 (or newer compatible with 1.21.1).
2. Drop the jar from `build/libs` and Fabric API for 1.21.1 into your Minecraft `mods` folder.

## Gameplay behaviour

- Whenever a survival/adventure player takes damage they are teleported to a random location.
- Teleport distance is uniform between 5 and 50 blocks horizontally with a small vertical variance.
- The mod searches for a safe destination with solid ground and two free air blocks; teleportation is skipped if no safe spot is found after several attempts.
