# Forever Fixes (1.21.1)

Compatibility fixes for the ForeverWorld modpack (NeoForge 1.21.1). No content. Install on both client and server. Each fix only
applies when the mods it patches are installed; the client-side fixes can be switched off in `config/foreverfixes-client.toml`.

## Fixes

**Rendering and animation**
- Mo' Bends + Armor Model API: RPG-series armor (Paladins, Wizards, Archers, Rogues, Armory) no longer loses its textures
  (Mo' Bends before 6.0; 6.0 fixed it itself).
- Wavey Capes + Mo' Bends: no double tilt while crouching; the cape lies along the back in creative flight.
- Chromeware + Mo' Bends: cyber torso, arms and legs and Gorilla Arms replace the flesh parts (Mantis Blades and the Projectile
  Launcher replace the flesh forearm), and all of them, Monowire included, bend at Mo' Bends' elbows and knees. Third person;
  first-person hands stay as they were.
- Better Inventory: Chromeware's Cyberware button is a Surgery Chair icon in the gutter with the other mods' buttons.
- Chromeware first person: implant hardware stays on MCA's arm (it drew on the vanilla arm while MCA showed its own), and arms
  carrying hardware sit further apart instead of meeting in the middle (`chromeware.firstPersonArmSpread`, client config).
- Mo' Bends + MCA: players keep their MCA body.
- Punchy + MCA: first-person arms show the MCA look instead of the Minecraft skin.
- Paraglider + Mo' Bends / Punchy: the open paraglider is held above the head while gliding, in third person (Mo' Bends'
  gliding pose plays again) and in first person (Punchy no longer pulls it down into the hand).
- Punchy: armor that draws its own model (RPG series, e.g. Crusader) gets no black sleeve on the first-person arms.
- MCA: female gender physics + RPG-series armor (Armor Model API): the breasts show the armor's own texture instead of
  purple.
- Better Inventory: other mods' inventory buttons (Spell Atlas, Field Guide, Spice of Life food book, Doggy Talents dog
  inventories) sit in its gutter, below the gear tab's slots; Spell Atlas's inventory panel and NTGL's chassis tabs show on its screen too.
- Sable: Ragdolls + Mo' Bends / MCA: a ragdolled player's normal body is no longer drawn next to the ragdoll, and ragdolls of
  MCA players show only the MCA look (no second head in the Minecraft skin); with Wavey Capes the ragdoll has only the Wavey cape.
- Sable: Ragdoll Corpse + MCA: corpses of MCA players show the MCA look instead of the Minecraft skin, and with MCA: female
  gender physics they keep their breasts.
- Werewolves + MCA: a player in a transformed werewolf form shows the werewolf model instead of nothing; the MCA look returns in
  human form.
- Better Simple Clouds: clouds and storm fog seen through glass no longer cover the entities, chests and beacon beams in front of
  or behind the glass. Particles no longer count as glass (smoke inside a rain column showed inverted colours), and the storm
  fog and rain haze stop at a beacon beam's outer glow and at JourneyMap's waypoint beams instead of covering them; glass is
  drawn in front of the storm fog behind it (it vanished against rain columns).
  Wet ground no longer has dry dashed lines along block edges, and the undersides of blocks (Sable ships) are no longer wet
  (a fixed copy of its rain shader, MIT).
- Simple Clouds: the quarter-resolution storm fog is stretched to the screen by depth, so it stops at the edges of what is in
  front of it instead of bleeding over them in 4-pixel steps; its blur uses a copy of vanilla's, not the blur Exposure Expanded
  replaces (distant rain columns came out solid black).
  Distant rain columns no longer flicker along their edges against the sky (a fixed copy of its storm fog shader, which is
  PolyForm Perimeter 1.0.1 licensed, https://polyformproject.org/licenses/perimeter/1.0.1: each pixel's samples are offset and a
  step the rain starts or stops in counts only its rainy part).
- Key bindings no longer reset after a restart: options saves during mod loading are held back until mod keys have their saved
  bindings, and the saved bindings of keys added later (PneumaticCraft armor keys) are kept and applied when they appear.
- Immersive Petroleum: oil reservoir placement no longer freezes the server when two world generators run at once (a pregen
  and Distant Horizons generating for a player).
- Architectury: registering or removing an event listener while the event runs no longer crashes the game (ConcurrentModificationException
  in the client tick event at the title screen).
- Sable: rain, its splashes, Better Simple Clouds' downpour and its wet ground stop on ships instead of falling through them
  (the heightmap they used has no ships in it). Each ship keeps the tops of its block columns, read again only when a block on it
  changes, and lays them over the world when it moves, only while rain or snow falls on it: parked ships cost nothing.
  With Better Simple Clouds, wet decks keep their puddles and raindrop rings in place on a moving ship, and the dry ground under
  a ship follows it (the 8 nearest ships' column tops and poses go to the wet ground shader).
- Better Simple Clouds: wet ground only where it rains: no wet deserts, savannas, badlands or snow under storm clouds, plus a list
  of biomes that always stay dry (`bettersimpleclouds.dryBiomes`).
- Tall worlds (JJThunder): the far clip plane is raised so mountain tops stay visible.
- Sodium: terrain more than one render distance above or below the camera no longer disappears.
- Sable + Flywheel (Create): ships' and the world's Flywheel lighting updates no longer build a Java stream for every
  neighbouring section of every light change (the render thread waited on it for close to a third of each frame).
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
- Skeletons Rising: its "The Family Reunion" advancement check runs on players' ticks only, instead of searching the area
  around every entity every tick.
- Skeletons Rising: no server crash ("Ticking entity", SoulTerrorOnEntityTickUpdateProcedure) when a Soul Terror's dash comes up
  with no player within range; it skips that dash instead.
- Create: Nuclear: the radiation tick no longer removes the radiation effect from every entity that does not have it.
- Items that rewrite their own data every tick (Relics' Clot of Time) no longer re-check every unfinished "has item" advancement
  each tick: a slot whose item only changed its data reports it at most once a second (`performance.throttleDataOnlySlotChanges`).
- Edit Enchanting: removing enchantments at the disenchantment table also lowers the item's anvil prior-work penalty, like the
  grindstone, so disenchanted items are no longer "Too Expensive" (`enchantments.editEnchantingResetRepairCost`).
- Better Combat: no server crash at startup from items without attribute modifiers.
- T.O Magic 'n Extras 4.4.0.1: no crash at startup ("Adding duplicate value" for its loot modifier codec).
- Dungeons and Taverns: its internal boss-marker enchantments ("Illegal enchantment", "LOOT TABLE ERROR - REPORT TO MOD/DATAPACK
  AUTHOR", "I AM ERROR") no longer come out of loot that enchants books randomly, and are removed from items players carry
  (list in `enchantments.blocked`, `config/foreverfixes-common.toml`).
- Loot no longer gives blank enchanted books: when enchant_with_levels cannot fit any of its enchantments (Dungeons and Taverns'
  desert ruin and nether keep books), one is picked from the same list; blank books players carry become plain books
  (`enchantments.fixBlankBooks`).
- Every chest loot table (structure chests, barrels, chest carts; with Lootr, each player's roll) also holds 1-5 Numismatics spurs
  (`loot.chestCoins.*`; max 0 turns it off).
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

**Magic balance**
- Iron's Spells x Chromeware: cyberware trades magic for magic resistance. Each installed part takes its own share of spell power,
  max mana and mana regen (0.4% per capacity point, rounded to 0.5%), applied one after another from the costliest part down and
  after every other bonus; spell resistance is +0.65% per point up to +60% (or, with `resistMode = LOST_POTENTIAL`, 60% of the
  fraction of magic lost). Wearing Energy Field's personal shield generator costs another 75% of mana regen. Advancements of other
  Create-based mods raise the cyberware capacity limit by 2 each, up to +360 (a 720 limit, the cost of the costliest part in every
  slot). Values in `magic.chrome.*` (common config); each part and the shield list their share on the tooltip.
- Chromeware immunosuppressants: a Standard dose protects for one Minecraft day, a Military dose for half a day
  (`chromeware.suppressant*Ticks`); re-dosing while protected still adds it halved per dose.
- Corail Tombstone's Blessing gives one of a short holy list (regeneration, resistance, absorption, fire resistance, health boost,
  instant health, Angel Wings, Fortified, Blessed Strikes, Sanctified Coating, Vampire's Delight Blessing, Sunblock, Purification,
  Holy Power; `holy.blessingEffects`) instead of any beneficial effect in the pack.
- Vampirism: vampire players count as undead for Smite (`holy.vampirePlayersUndead`); vampire mobs are undead through the pack's tag data.
- Wild Magic (`wildMagic.*`): an armor enchantment from a data pack (default `foreverworld:wild_magic`) that, like Blessing, has a
  level x 1% chance per piece when you're hit, but gives any effect in the game, good, bad or neutral (instant-death, exploding,
  vampire/werewolf-converting, Pastel's Divinity and summon-timer effects left out; never rolled by random-enchant loot; harmful ones capped at level II). Server-side only.
- Lootr boss chests (`bossChest.*`): a boss (`#c:bosses`, minus Twilight Forest bosses and BOMD's Gauntlet/Obsidilith, which leave
  their own chests) drops nothing on the ground; a Lootr chest appears where it died. Only players who damaged it can open it, each
  gets their own roll of the boss's loot table (as the killer, with their own looting and luck), and it vanishes after 20 minutes.
- Chromeware x Female Gender Mod: on a bare chrome torso (no Synthskin) the breasts use the cyber body's pectoral plate texture,
  tinted with the player's Shells colour, instead of the skin (and no skin jacket layer over them); on any cyber torso the breast physics
  is held still.
- First-person arms with MCA + Mo' Bends + Punchy + Chromeware, as one system: MCA's own first-person arm draws on parts Mo' Bends
  rebuilt for the third-person body and comes out empty, so with Mo' Bends present FF draws the MCA skin and clothing on the posed
  arm; Chromeware's skin pieces (Mantis / Launcher / Extendo forearm copies, Gorilla Arms) wear the MCA skin and clothing instead of
  the Minecraft skin; and on Punchy's first-person arms Chromeware's limb rules and implant renderers run as they do on vanilla arms.
- Chromeware's first-person implant view (Mantis Blades, Launcher, Monowire, Gorilla Arms draw the whole body from the camera with the
  arms swung toward the eyes): Mo' Bends defers its animation for that pass (it pulled the arms down), and MCA's genetics resize is
  skipped in it (it moved the arms away from the camera); both still apply in third person.
- Chromeware armour fit (`chromeware.armorFit`): each cyber torso/arm takes 1/3 off the chestplate, each cyber leg 1/2 off leggings
  and boots (armour, toughness, knockback resistance, enchantment levels); armour isn't drawn on replaced limbs, and with every
  covered part replaced the piece comes off and won't go back on, even with Curse of Binding.
- `/bendsrec start <name> [self|look]` … `/bendsrec stop` (client, with Mo' Bends): records the live Mo' Bends pose of you or the
  entity you look at, one keyframe per tick, to `bendspacks/recorded/<name>.bendsanim`, the clip format Bends packs use. The
  stock player animations are Java code, so this is how to get them into the Blockbench plugin to study or edit.
- Chromeware x Mo' Bends joints: a cyber arm or leg is cut at Mo' Bends' joint. Every quad of Chromeware's limb mesh is split at
  the seam plane through the forearm / foreleg pivot (4 px below the shoulder, 6 below the hip); what lies above stays with the
  upper bone and what lies below turns with the lower one, so chrome and flesh share one joint line and nothing straddles it.
  `config/foreverfixes-chrome-joints.json` can move the seam and hinge, switch a limb to whole-piece mode with a `lower` list,
  and tweak pieces (hide above/below a bend angle, offset in pixels, rotation about the piece's centre); `/ffchrome reload`
  applies edits in game, `/ffchrome show` prints the current values. The Mantis/Launcher skin and sleeve copies and the Gorilla
  Arms elbow keep Mo' Bends' own pivot, and lower segments follow Mo' Bends' forearm/foreleg position keyframes.
- Chromeware x Mo' Bends arms and packs (`chromeware.packOwnsArms`): while a Bends pack node poses an arm bone, the pack owns it
  and Chromeware's implant hold pose (blades up by the face) isn't copied over it; when the pack lets go (walking, retracting)
  Chromeware's deploy / hold / slash poses return.
- Chromeware x Mo' Bends layers follow the pose: Mo' Bends only writes its pose into the vanilla model parts (which Chromeware's
  implant hardware and skin/sleeve copies draw from) after it has resolved the player's adaptive pivots, and it refuses to do that
  while a Player Animation Lib animation is on the model. Chromeware's blade and wire clips run the whole time an implant is out,
  so the layers stayed at the Player Animation Lib pose while the arm was elsewhere (pack-owned arms, or after a first-person
  round trip). Those clips only rotate, so the pivots are now resolved under them and the layers follow Mo' Bends again. The
  pivots and the vanilla rest positions are resolved once per renderer, and Chromeware's first-person pass pulls the implant arms
  toward the camera, so if blades were first deployed in first person (before any third-person look since launch) the camera-arm
  positions were baked in for the session (the rest positions live in a static per-model snapshot that every later sync re-reads):
  arms pushed back, the flesh bicep inside the body, held items at the elbow. Nothing is resolved, snapshotted or stored during a
  first-person pass any more.
- Connection timeouts (`network.timeoutSeconds`, default 600): vanilla drops a connection after 30 s without packets and a player
  after 15 s without a keep-alive answer, so a client that stalls for half a minute (shader compile, a big Sable assembly, a long GC)
  is kicked as "Timed out" and has to log back in. Both limits, on the server's side and on the client's, follow the setting; 0
  disables them (a dead connection then lasts until TCP gives up). Server-side it needs the Forever Fixes build on the server.
- Sable centre of mass (client, `sable.centerOfMass.*`): while the configured item is held (default Create Simulated's Creative
  Physics Staff, either hand), every sub-level within 128 blocks shows its centre of mass as a marker drawn through blocks (magenta
  cube with red / green / blue axis bars) and its total mass above it; `sable.centerOfMass.bounds` also outlines each sub-level's
  block bounds. Sable keeps mass data on the server only, so the client measures the plot blocks itself with Sable's MassTracker
  (same block masses and per-block centres), once a second per sub-level while the item is held.
- Chromeware x Mo' Bends geometry measurement: Mo' Bends measures each vanilla model part once per model by rendering its cubes
  into a capture buffer, and derives the elbow / knee pivots and the split meshes from that. Chromeware rewrites how a player's
  parts draw while that player renders (Mantis forearm cutout, hidden cyber limbs, shortened stumps), so a model first measured
  inside one of its render scopes (typically the first-person blade pass, blades deployed before any third-person look) got a
  6 px arm and an empty cyber limb: the forearm pivot sat at the shoulder, the forearm drew over the bicep, the hand and anything
  held ended at the elbow, and a relog didn't help because the cache is static. Chromeware's rules now stand aside while Mo' Bends
  measures.
- Chromeware x Mo' Bends Sandevistan copies: the Sandevistan draws the player again through a proxy (the live copy for its post
  effect and the afterimage captures). Mo' Bends animated the proxy as a player of its own, so the copy walked without swinging
  while the real player attacked; the proxy now takes the real player's finished pose of the frame (pack, Player Animation Lib
  copy and Forever Fixes adjustments included) and its frozen Player Animation Lib layers no longer re-pose it.
- Chromeware x Mo' Bends elbows: while Mantis Blades, the Monowire or the Projectile Launcher are out, Chromeware poses the arms
  through Player Animation Lib, and Mo' Bends straightened its forearms to match a rigid vanilla arm, locking the elbows in its
  own animations and in Bends packs. The forearm now keeps its Mo' Bends / pack bend under Chromeware's upper-arm pose.
- Mo' Bends pack transitions blend properly: 6.0.1 adds the two poses' quaternions weighted (1-t)/t, which the renderer shows as
  the model shrinking mid-transition (to nothing against a node that doesn't animate the bone) and popping back, and bones only
  one side animates snap. Non-additive keyframe layers now interpolate per bone with normalised quaternions, a side without the
  bone contributing the live stock pose, so clips fade in from and out to whatever Mo' Bends was doing.
- Mo' Bends' Packs tab works: 6.0.1 covers it with a "WIP" overlay that swallows clicks although the list and toggles behind it
  work; the overlay is left off that tab (the Customize tab keeps its). `/bendspack list|apply <key>|remove <key>|clear|reload`
  (client) does the same from chat, and reports Mo' Bends' reason when it rejects a pack.
- Mo' Bends pack conditions for Chromeware (client): `foreverfixes:mantis_deployed`, `foreverfixes:mantis_drawing` (a blade is
  still extending, the first 16 ticks after deploying; for a draw animation that hands over to an idle), `foreverfixes:mantis_striking_right` /
  `foreverfixes:mantis_striking_left` (that arm is mid-slash, 10 ticks; for swing clips on an additive layer) and `foreverfixes:sandevistan_active` can be
  used as `triggerCondition` types in a Bends pack's animator, with `core:not`/`core:and` around them like any other.
- Dark Doppelganger x Chromeware (`chromeware.doppelganger.*`): the boss, its mirror clones and shadow echoes copy the summoner's cyber
  limbs and Shell colours and draw them (armour over those limbs is hidden, as on the player), and the spell power it copies leaves
  out Forever Fixes' cyberware magic penalties. If the summoner has a Sandevistan, the boss gets one: a Chromeware time field it owns
  (everything else inside slows, the boss doesn't), 5 s on, 30 s cooldown (also before the first use), 48-block field, 90% slowdown,
  used when its target is within 24 blocks; all configurable. A summoner who pops their own Sandevistan is immune to it, as the boss
  is to theirs. A boss summoned by a Sandevistan user has a 10% chance (`sandevistanDropChance`) to drop one Clot of Time
  (`sandevistanDropItem`) when it dies: one roll, one item, Looting ignored, clones and echoes never, and only on a real death
  (loot-table simulators such as Drygmy charms don't get it).
- Energy Field: its reflection helper caches method lists per class instead of copying them on every first-person frame (3-4% of
  the render thread).
- Gamerule registration is locked: Twilight Forest registers its gamerules on a background thread while other mods register theirs, and
  two at once corrupted the gamerule map so a world crashed on its first tick (`GameRules.getInt` NPE), at random.
- Mob caps (`spawning.mobCapScale`): natural spawning per category scaled, monsters halved by default (35 instead of 70).
- Glowbrick's Printing Press: a type block with durability loses one point per printed book and crumbles at zero; the Typesetter refuses
  books carrying a `printing.noTypesetEnchantments` enchantment. The server data pack `pack-data/heimdall_wildmagic` gives bosses
  (`#c:bosses`, killed by a player) a 1% chance to drop a single-use Wild Magic typesetting (levels I-V, rarer the higher).
- Vampirism: holy things burn vampires: each worn Blessing piece, held Smite or Sanctified weapon, or held enchanted book carrying
  one of those deals 1 heart of holy damage per second and sets them alight (`holy.*`).
- Chromeware x Chronomancy: a Sandevistan keeps you outside stopped time: you move during a world-wide time stop like its caster, and Temporal Stasis / Paradox do not take hold on you while it is running (`magic.chrome.sandevistanTimeStopImmunity`: ACTIVE, INSTALLED or OFF).

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
- Aero Warptics + Dimensional Sable (Cosmonautics): Rift Drives can warp to Warp Anchors in other dimensions (planets). The ship
  flies its departure rift and corridor as usual, then Dimensional Sable moves it to its resting place beside the anchor; the
  arrival rift run-out is skipped and the drive arrives in cooldown. Needs `allowCrossDimensionWarp = true` in
  aerowarptics-server.toml. Cosmonautics' deep space is refused (ships there belong to orbit instances). Only what is on or against the ship
  goes with it (the 48-block entity margin used for Cosmonautics launches is 2 blocks for warps).
- Sable: a ship breaking glass, leaves or ice (or anything else changing blocks during the physics step) no longer freezes the
  server when a neighbouring block reacts by changing itself (seen with a Vista TV); those block changes reach the physics
  engine right after the step.

**Lighting**
- ScalableLux: no crash when leaving a world with Sable ships or ragdolls loaded ("Index … out of bounds for length 25").

**World generation**
- Chunks no longer fail to generate ("Biome decoration: Index … out of bounds") when mods add features in conflicting orders.
- Sky islands and airships (Create Sky Village, Sky Villages, ShellBound for AirShip) no longer generate inside mountains: the
  terrain under the whole structure is checked and it is raised until it clears the ground (`worldgen.floatingStructures.*`).
- Priority structures keep their place: fortresses, bastions and Nether boss arenas (list in `worldgen.priorityStructures.ids`) are
  worked out from the seed before anything else is placed near them, so smaller structures that would overlap them are left out,
  whichever chunk generates first. Works alongside No Structure Overlap, which then keeps the other structures apart.

## Building

`./gradlew build` (Java 21) → `build/libs/foreverfixes-<version>.jar`.

The mixins compile against jars of the patched mods, which are not part of this repository. Put these in `libs/`:
- `mobends-1.21.1-6.0.1-neoforge.jar`, `punchy-3.0a-neoforge-1.21.1.jar` and `waveycapes-neoforge-1.11.2-mc1.21.1.jar`;
- `dimensional-sable-classes-1.0.5.jar`: only the `dev/egg/**` classes from Create: Cosmonautics 26.08.307;
- `sable-classes-2.0.6.jar`: only the `dev/ryanhcode/**` classes from Sable 2.0.6;
- `sable-player-ragdoll-classes-0.7.5.jar`: only the `dev/**` classes from Sable: Ragdolls 0.7.5;
- `scalablelux-classes-0.3.0-alpha.0.8.jar`: only the `ca/spottedleaf/starlight/**` classes from ScalableLux.
- the mod jars of Armor Model API 1.1.0, MCA: female gender physics 0.1.6, Better Inventory 2.0.1, Field Guide 1.22.2,
  Spice of Life: Onion 1.5.7, Spell Atlas 1.88.22, Doggy Talents Next 1.19.1, NTGL 3.2.0, Simulated Coasters 0.1.5 and
  Hexerei 0.5.0.3.

## License

0BSD (see LICENSE). No code from the patched mods is included, except the two marked shader copies above
(`bsc_rain_haze.fsh`, MIT; `storm_fog.fsh`, PolyForm Perimeter 1.0.1 with Better Simple Clouds' MIT changes), which keep
their own licenses.
