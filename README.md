# Merlin's Old Fog
Client-side Fabric mod for Minecraft 26.2 that restores the classic 1.8.9 fog. Works with Sodium.

## In-game settings
- **Mod Menu** (optional) -> Merlin's Old Fog -> Config, or type `/oldfog`
- 3 pages: Allgemein / Nether / Farbe. Click the value in the middle to reset that option.
- `<<` `>>` = big step, `<` `>` = small step.

## Commands (need Fabric API)
| Command | Effect |
|---|---|
| `/oldfog` or `/oldfog gui` | open settings |
| `/oldfog on` / `off` / `toggle` | enable / disable |
| `/oldfog overworld <start> <end>` | fog range as fraction of render distance |
| `/oldfog nether <start> <end>` | same for the Nether |
| `/oldfog nether-fog on\|off` | apply in Nether |
| `/oldfog color <hex>` | set colour (e.g. `C0D8FF`) and enable it |
| `/oldfog color on\|off` | custom colour on/off |
| `/oldfog nethercolor on\|off` | custom colour in Nether |
| `/oldfog haze on\|off` | modern extra haze (off = removed) |
| `/oldfog status` / `reload` / `reset` | show / reload file / defaults |

## Config file
`config/merlins_old_fog.json` (hot-reloaded). Defaults = 1.8.9.

## Build
`./gradlew build` (JDK 25) -> `build/libs/merlins-old-fog-1.0.0.jar`
