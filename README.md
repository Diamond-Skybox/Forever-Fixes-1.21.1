# Forever Fixes (1.21.1)

Compatibility fixes for the ForeverWorld modpack (NeoForge 1.21.1). No content. Install on both client and server. Each fix only
applies when the mods it patches are installed; the client-side fixes can be switched off in `config/foreverfixes-client.toml`.

## Fixes

**Rendering and animation**
- Mo' Bends + Armor Model API: RPG-series armor (Paladins, Wizards, Archers, Rogues, Armory) no longer loses its textures.
- Wavey Capes + Mo' Bends: no double tilt while crouching; the cape lies along the back in creative flight.
- Mo' Bends + MCA: players keep their MCA body.
- Punchy + MCA: first-person arms show the MCA look instead of the Minecraft skin.
- Paraglider + Mo' Bends / Punchy: the open paraglider is held above the head while gliding, in third person (Mo' Bends'
  gliding pose plays again) and in first person (Punchy no longer pulls it down into the hand).
- MCA: female gender physics + RPG-series armor (Armor Model API): the breasts show the armor's own texture instead of
  purple.
- Better Inventory: other mods' inventory buttons (Spell Atlas, Field Guide, Spice of Life food book, Doggy Talents dog
  inventories) sit in its gutter, below the gear tab's slots; Spell Atlas's inventory panel and NTGL's chassis tabs show on its screen too.
- Sable: Ragdolls + Mo' Bends / MCA: a ragdolled player's normal body is no longer drawn next to the ragdoll, and ragdolls of
  MCA players show only the MCA look (no second head in the Minecraft skin); with Wavey Capes the ragdoll has only the Wavey cape.
- Tall worlds (JJThunder): the far clip plane is raised so mountain tops stay visible.
- Sodium: terrain more than one render distance above or below the camera no longer disappears.
- Simulated Coasters: no second flush of the world buffer every frame when there are no coasters to draw (with Iris it
  re-sorted everything buffered so far), and the track placement previews are cleared once instead of every tick.
- NTGL: weapon animations only tick while the weapon is drawn; every gun ever seen (in hands, inventories, recipe viewers)
  kept ticking for the rest of the session.
- Hexerei: book page ids are parsed once instead of every tick for every page of every book.
- Databank 1.3.2: no "Main: …, Backup: …" debug line in the log every frame a post-processing shader runs.
- Astral Sorcery + Enchantment Descriptions: enchanted items and books show the enchantment descriptions again.
- Create: Stock Market + Create: Auction House: the Stock Market screen has an Auction House tab; closing the Auction House
  opened from it goes back to Stock Market.

**Gameplay**
- No server crash ("ConcurrentModificationException" in DimensionDataStorage.save, then a hang) when mods add saved data from
  world-generation threads during an autosave, e.g. Immersive Petroleum's reservoir regions during a pregen.
- Better Combat: no server crash at startup from items without attribute modifiers.
- T.O Magic 'n Extras 4.4.0.1: no crash at startup ("Adding duplicate value" for its loot modifier codec).
- Dungeons and Taverns: its internal boss-marker enchantments ("Illegal enchantment", "LOOT TABLE ERROR - REPORT TO MOD/DATAPACK
  AUTHOR", "I AM ERROR") no longer come out of loot that enchants books randomly, and are removed from items players carry
  (list in `enchantments.blocked`, `config/foreverfixes-common.toml`).
- Loot no longer gives blank enchanted books: when enchant_with_levels cannot fit any of its enchantments (Dungeons and Taverns'
  desert ruin and nether keep books), one is picked from the same list; blank books players carry become plain books
  (`enchantments.fixBlankBooks`).
- Trades: villager / wandering trader offers selling items listed in `trades.removedResults` are removed (default: AE2's Fluix
  Researcher selling charged certus quartz).
- Dimensions and planets are reached by Tempad, stargate, rocket or their own way in: waystones, Tombstone, FTB Essentials
  commands, recall and portal spells and similar teleports cannot move players into another dimension (`teleports.*`;
  same-dimension use unchanged).
- OpenComputers: optionally (`loot.removeOpenComputersChestLoot` in `config/foreverfixes-common.toml`) no loot disks or
  components in vanilla chests; its own structures keep their loot.
- Create Numismatified Waystones + Waystones 21.1.46: teleports actually take the coin fare (the server used Waystones' free
  default, so the same coins paid for every trip).
- Create: Auction House 3.2.1 + Create: Numismatics 1.1.0: spending works again (savings deposits, minigame bets, purchases
  failed on the server: Numismatics changed how money is taken from an account). Same fix for Numismatified Waystones' card payments.
- Create Numismatified Waystones: a bound bank card works on the waystone screen (its balance was read on the client, where
  Numismatics has no bank: 0 spurs and an error in the log on every screen update).
- Waystones UI Plus 1.6.0 + Waystones 21.1.46: no crash when editing a waystone from the UI Plus screen.
- Traveler's Titles: dimension titles also show when arriving above the build height, and Create: Cosmonautics' deep space and
  Moon have names.

**Create: Cosmonautics / Sable ships**
- Ships keep their blocks, machine data, passengers and seats when moving between dimensions of different height, including
  2096-block-tall worlds (also fixes Cosmonautics issue #116).
- Players stay seated through the atmosphere transition, even under full thrust.
- Game sounds keep playing between Y 1000 and 2000 (Cosmonautics muted them there, which in the tall JJThunder overworld
  silenced the mountain tops).
- In dimensions taller than Y 1000 the planet drawn beneath, space fog, daylight stars and the altitude render distance start
  at the dimension's build top instead of Y 1000 (mountain terrain in JJThunder); `cosmonautics.spaceEffectsStartY` sets
  another height.
- The sun's animated plasma texture is rebuilt once per second instead of every tick (it took about a quarter of the render
  thread's time anywhere the universe sky shows, the overworld included); `cosmonautics.starTextureIntervalTicks`.
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
- `mobends-1.21.1-5.6.3-neoforge.jar` and `waveycapes-neoforge-1.11.2-mc1.21.1.jar`;
- `dimensional-sable-classes-1.0.5.jar`: only the `dev/egg/**` classes from Create: Cosmonautics 26.08.307;
- `sable-classes-2.0.6.jar`: only the `dev/ryanhcode/**` classes from Sable 2.0.6;
- `sable-player-ragdoll-classes-0.7.5.jar`: only the `dev/**` classes from Sable: Ragdolls 0.7.5;
- `scalablelux-classes-0.3.0-alpha.0.8.jar`: only the `ca/spottedleaf/starlight/**` classes from ScalableLux.
- the mod jars of Armor Model API 1.1.0, MCA: female gender physics 0.1.6, Better Inventory 2.0.1, Field Guide 1.22.2,
  Spice of Life: Onion 1.5.7, Spell Atlas 1.88.22, Doggy Talents Next 1.19.1, NTGL 3.2.0, Simulated Coasters 0.1.5 and
  Hexerei 0.5.0.3.

## License

0BSD (see LICENSE). No code from the patched mods is included.
