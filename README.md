# Forever Fixes (1.21.1)

Compatibility fixes for the ForeverWorld modpack (NeoForge 1.21.1). No content. Install on both client and server. Each fix only
applies when the mods it patches are installed; the client-side fixes can be switched off in `config/foreverfixes-client.toml`.

## Fixes

**Rendering and animation**
- Mo' Bends + Armor Model API: RPG-series armor (Paladins, Wizards, Archers, Rogues, Armory) no longer loses its textures.
- Wavey Capes + Mo' Bends: no double tilt while crouching; the cape lies along the back in creative flight.
- Mo' Bends + MCA: players keep their MCA body.
- Punchy + MCA: first-person arms show the MCA look instead of the Minecraft skin.
- Sable: Ragdolls + Mo' Bends / MCA: a ragdolled player's normal body is no longer drawn next to the ragdoll, and ragdolls of
  MCA players show only the MCA look (no second head in the Minecraft skin); with Wavey Capes the ragdoll has only the Wavey cape.
- Tall worlds (JJThunder): the far clip plane is raised so mountain tops stay visible.
- Sodium: terrain more than one render distance above or below the camera no longer disappears.

**Gameplay**
- Better Combat: no server crash at startup from items without attribute modifiers.
- Traveler's Titles: dimension titles also show when arriving above the build height, and Create: Cosmonautics' deep space and
  Moon have names.

**Create: Cosmonautics / Sable ships**
- Ships keep their blocks, machine data, passengers and seats when moving between dimensions of different height, including
  2096-block-tall worlds (also fixes Cosmonautics issue #116).
- Players stay seated through the atmosphere transition, even under full thrust.
- Ship links to sub-levels that were not moved no longer crash the server.
- Gyroscope Stabilizers only damp rotation in deep space instead of spinning the ship.
- Ships saved with the ship library load intact in dimensions with a different bottom.

**Lighting**
- ScalableLux: no crash when leaving a world with Sable ships or ragdolls loaded ("Index … out of bounds for length 25").

**World generation**
- Chunks no longer fail to generate ("Biome decoration: Index … out of bounds") when mods add features in conflicting orders.

## Building

`./gradlew build` (Java 21) → `build/libs/foreverfixes-<version>.jar`.

The mixins compile against jars of the patched mods, which are not part of this repository. Put these in `libs/`:
- `mobends-1.21.1-5.6.2-neoforge.jar` and `waveycapes-neoforge-1.11.2-mc1.21.1.jar`;
- `dimensional-sable-classes-1.0.5.jar`: only the `dev/egg/**` classes from Create: Cosmonautics 26.08.307;
- `sable-classes-2.0.6.jar`: only the `dev/ryanhcode/**` classes from Sable 2.0.6;
- `sable-player-ragdoll-classes-0.7.5.jar`: only the `dev/**` classes from Sable: Ragdolls 0.7.5;
- `scalablelux-classes-0.3.0-alpha.0.8.jar`: only the `ca/spottedleaf/starlight/**` classes from ScalableLux.

## License

0BSD (see LICENSE). No code from the patched mods is included.
