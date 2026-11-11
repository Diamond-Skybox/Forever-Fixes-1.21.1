package com.diamondskybox.foreverfixes;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/** config/foreverfixes-common.toml: settings that act on the server (or the single-player world). */
public final class ForeverFixesCommonConfig
{
	private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

	public static final ModConfigSpec.BooleanValue REMOVE_OPENCOMPUTERS_CHEST_LOOT = B
			.comment("OpenComputers: leave out the loot disks and components it adds to vanilla dungeon, temple, stronghold library and village",
					"chests. Its own structures' chests keep their loot.")
			.define("loot.removeOpenComputersChestLoot", false);

	public static final ModConfigSpec.ConfigValue<String> CHEST_COINS_ITEM = B
			.comment("Item added to every chest loot table (structure chests, barrels, chest carts; with Lootr, to each player's roll).")
			.define("loot.chestCoins.item", "numismatics:spur");

	public static final ModConfigSpec.IntValue CHEST_COINS_MIN = B
			.comment("Fewest of that item per chest.")
			.defineInRange("loot.chestCoins.min", 1, 1, 64);

	public static final ModConfigSpec.IntValue CHEST_COINS_MAX = B
			.comment("Most of that item per chest; 0 turns the extra coins off.")
			.defineInRange("loot.chestCoins.max", 5, 0, 64);

	public static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_ENCHANTMENTS = B
			.comment("Enchantments that never come out of random loot enchanting and are removed from items in players' inventories.",
					"Default: Dungeons and Taverns' internal boss markers (named \"Illegal enchantment\", \"LOOT TABLE ERROR - REPORT TO",
					"MOD/DATAPACK AUTHOR\" and \"I AM ERROR\"), which loot tables that enchant books from the whole registry could roll.",
					"The bosses keep them on their own weapons.")
			.defineListAllowEmpty("enchantments.blocked",
					List.of("nova_structures:boss_behaviour", "nova_structures:shulker_boss", "nova_structures:shulker_miniboss"), () -> "",
					o -> o instanceof String s && ResourceLocation.tryParse(s) != null);

	public static final ModConfigSpec.BooleanValue FIX_BLANK_BOOKS = B
			.comment("Loot that would be an enchanted book without enchantments (enchant_with_levels whose options cannot fit the rolled",
					"level) gets one enchantment from those options instead, and blank enchanted books in players' inventories become plain books.")
			.define("enchantments.fixBlankBooks", true);

	public static final ModConfigSpec.ConfigValue<List<? extends String>> REMOVED_TRADE_RESULTS = B
			.comment("Villager and wandering trader trades that sell one of these items are removed (item ids).",
					"Default: AE2's Fluix Researcher selling charged certus quartz (certus is meant to come only from AE2 growth and the planets).")
			.defineListAllowEmpty("trades.removedResults", List.of("ae2:charged_certus_quartz_crystal"), () -> "",
					o -> o instanceof String s && ResourceLocation.tryParse(s) != null);

	public static final ModConfigSpec.BooleanValue BLOCK_CROSS_DIMENSION_TELEPORTS = B
			.comment("Refuse moving a player into another dimension when the move comes from one of the teleport mods/features listed in",
					"teleports.crossDimensionDenyCallers. Between dimensions and planets players travel by Tempad, stargate or rocket, or",
					"through a dimension's own way in (its portal or travel feature). Teleports inside one dimension are not affected, and",
					"neither are portals, deaths, rockets, Tempad, stargates or admin commands.")
			.define("teleports.blockCrossDimension", true);

	public static final ModConfigSpec.BooleanValue CROSS_DIMENSION_OPS_EXEMPT = B
			.comment("Operators (permission level 2+) may still use those teleports across dimensions.")
			.define("teleports.opsExempt", true);

	public static final ModConfigSpec.ConfigValue<List<? extends String>> CROSS_DIMENSION_DENY_CALLERS = B
			.comment("Java package or class name prefixes: a cross-dimension move whose call stack passes through one of these is refused.")
			.defineListAllowEmpty("teleports.crossDimensionDenyCallers", List.of(
					"net.blay09.mods.waystones.", "ovh.corail.tombstone.", "dev.ftb.mods.ftbessentials.",
					"io.redspace.ironsspellbooks.spells.ender.RecallSpell", "io.redspace.ironsspellbooks.entity.spells.portal.",
					"io.redspace.ironsspellbooks.block.portal_frame.", "com.hollingsworth.arsnouveau.common.block.tile.PortalTile",
					"com.hollingsworth.arsnouveau.common.spell.effect.EffectBlink", "dev.semeth.leylines.spell.AnchorRecallSpell",
					"mysticmods.roots.item.HomesicknessCharm", "mysticmods.roots.util.TeleportUtil", "com.chronomancy.",
					"com.breakinblocks.neovitae.common.blockentity.TeleposerBlockEntity",
					"com.breakinblocks.neovitae.api.sigil.effects.TelepositionSigilEffect", "earth.terrarium.pastel.items.magic_items.EnderSpliceItem",
					"it.hurts.sskirillss.relics.items.relics.", "net.astralya.hexalia.util.TeleportUtil",
					"it.hurts.shatterbyte.reliquified_ars_nouveau.items.StaffOfTheSpectralWalkerItem", "hellfirepvp.astralsorcery.",
					"com.klikli_dev.occultism.common.block.EntityWormholeBlock"), () -> "", o -> o instanceof String);

	public static final ModConfigSpec.BooleanValue SKELETONS_RISING_PLAYERS_ONLY = B
			.comment("Skeletons Rising: check for 'The Family Reunion' advancement on players' ticks only.",
					"Its procedure ran on every entity's tick (client and server), each time searching a 16-block box for decayed",
					"skeletons and then for players; only players can earn the advancement.")
			.define("performance.skeletonsRisingPlayersOnly", true);

	public static final ModConfigSpec.BooleanValue NUCLEAR_SKIP_ABSENT_EFFECT_REMOVAL = B
			.comment("Create: Nuclear: remove the radiation effect only from entities that have it.",
					"Its radiation tick removed it from every living entity every tick, firing a NeoForge effect-removed event each time.")
			.define("performance.nuclearSkipAbsentEffectRemoval", true);

	public static final ModConfigSpec.BooleanValue THROTTLE_DATA_ONLY_SLOT_CHANGES = B
			.comment("Player inventory slots whose item only changed its data (same item, same count) report the change to advancements",
					"at most once a second instead of every tick. Items that rewrite their data every tick (Relics' Clot of Time) otherwise",
					"re-check every unfinished \"has item\" advancement each tick. The latest stack is still checked within a second.")
			.define("performance.throttleDataOnlySlotChanges", true);

	public static final ModConfigSpec.BooleanValue EDIT_ENCHANTING_RESET_REPAIR_COST = B
			.comment("Edit Enchanting: removing enchantments at the disenchantment table recalculates the item's anvil prior-work penalty",
					"from the enchantments left, like the grindstone (none left: no penalty). It is never raised.")
			.define("enchantments.editEnchantingResetRepairCost", true);

	public static final ModConfigSpec.BooleanValue ARCHITECTURY_COPY_ON_WRITE_EVENTS = B
			.comment("Architectury: registering or removing an event listener while that event runs no longer crashes the game",
					"(ConcurrentModificationException in the client tick event at the title screen). Changes go to a copy of the listener list.")
			.define("stability.architecturyCopyOnWriteEvents", true);

	public static final ModConfigSpec.BooleanValue IP_RESERVOIR_GENERATOR_BIOMES = B
			.comment("Immersive Petroleum: oil reservoirs read the biome from the world's biome source while they are placed, not through the",
					"server level. The level loads the chunk, and IP does that while holding the lock every reservoir placement waits for: with",
					"two world generators running (a pregen and Distant Horizons for a player) the server froze.")
			.define("worldgen.immersivePetroleumReservoirBiomes", true);

	public static final ModConfigSpec.ConfigValue<List<? extends String>> FLOATING_STRUCTURES = B
			.comment("Structures that float in the air (sky islands, airships): when one is placed, the terrain under its whole footprint is",
					"checked and the structure is raised until it clears the ground by worldgen.floatingStructures.clearance blocks; one that",
					"cannot fit under the build limit is not placed. Structure ids, or \"namespace:*\" for all of a mod's structures.")
			.defineListAllowEmpty("worldgen.floatingStructures.ids", List.of("create_sky_village:*", "skyvillages:*", "shellbound_for_airship:*",
					"mss:*", "mvs:floating_islands", "mvs:large_floating_island", "castle_in_the_sky:*", "delighto_flight:cloud_island",
					"envelope:collapsed_mail_hub", "create_structures_arise:createminiskyvillage", "create_structures_arise:pillagersteampunkairship",
					"cr_aero_structures:airship", "cr_aero_structures:airballoon", "cr_aero_structures:pillager_airship",
					"cr_aero_structures:pillager_airballoon"),
					() -> "", o -> o instanceof String s && !s.isBlank());

	public static final ModConfigSpec.ConfigValue<List<? extends String>> FLOATING_STRUCTURE_BANDS = B
			.comment("Height bands for floating structures that place themselves in code and ignore a datapack's start_height (Create:",
					"Aeronautics Structures puts its airships and balloons a fixed distance above the ground): \"id=min..max\", or",
					"\"namespace:*=min..max\". The structure's lowest block is moved to a height in the band, the same for a chunk on every",
					"seed-identical world, then the clearance below still applies. Only structures in worldgen.floatingStructures.ids.")
			.defineListAllowEmpty("worldgen.floatingStructures.heightBands", List.of("cr_aero_structures:airship=140..1800",
					"cr_aero_structures:airballoon=140..1800", "cr_aero_structures:pillager_airship=140..1800",
					"cr_aero_structures:pillager_airballoon=140..1800"),
					() -> "", o -> o instanceof String s && s.matches("[a-z0-9_.-]+:[a-z0-9_./*-]+=-?\\d+\\.\\.-?\\d+"));

	public static final ModConfigSpec.IntValue FLOATING_STRUCTURES_CLEARANCE = B
			.comment("Fewest blocks of air between a floating structure's lowest block and the terrain (or water surface) below it.")
			.defineInRange("worldgen.floatingStructures.clearance", 8, 0, 256);

	public static final ModConfigSpec.ConfigValue<List<? extends String>> PRIORITY_STRUCTURES = B
			.comment("Structures that keep their place: positions are known from the seed, so any other structure that would overlap one of these",
					"(within worldgen.priorityStructures.radius chunks) is not placed, no matter which generates first. Earlier entries win over",
					"later ones. Structure ids, or \"namespace:*\" for all of a mod's structures; empty turns it off. With No Structure Overlap these",
					"are accepted by it without being tracked. Read when a world loads: changes need a restart, and only new chunks are affected.")
			.defineListAllowEmpty("worldgen.priorityStructures.ids", List.of("cataclysm:burning_arena", "block_factorys_bosses:underworld_arena",
					"nova_structures:nether_keep", "betterfortresses:fortress", "minecraft:fortress", "minecraft:bastion_remnant",
					"cataclysm:soul_black_smith", "mns:mega_fortress", "bosses_of_mass_destruction:gauntlet_arena"),
					() -> "", o -> o instanceof String s && !s.isBlank());

	public static final ModConfigSpec.IntValue PRIORITY_STRUCTURES_RADIUS = B
			.comment("How far (chunks) from a structure's edge priority structures are looked for; it must cover how far their pieces reach",
					"from their start chunk.")
			.defineInRange("worldgen.priorityStructures.radius", 12, 1, 32);

	public static final ModConfigSpec.IntValue PRIORITY_STRUCTURES_MARGIN = B
			.comment("Extra blocks kept free around each piece of a priority structure.")
			.defineInRange("worldgen.priorityStructures.margin", 2, 0, 16);

	public static final ModConfigSpec.BooleanValue PRIORITY_STRUCTURES_LOG = B
			.comment("Log each structure left out for a priority structure.")
			.define("worldgen.priorityStructures.log", true);

	public static final ModConfigSpec.BooleanValue CHROME_MAGIC = B
			.comment("Iron's Spells x Chromeware / Energy Field: cyberware trades magic for magic resistance. Each installed part takes its own",
					"share of spell power, max mana and mana regen (capacity x the rates below, rounded to magic.chrome.rounding), applied one",
					"after another from the costliest part down and after every other bonus: 1200 mana with parts at -30%, -30%, -20% and -5%",
					"is 1200 x 0.7 x 0.7 x 0.8 x 0.95. Spell resistance comes from magic.chrome.resistMode. Wearing the Energy Field personal",
					"shield generator cuts mana regen further. Applied every second while the player is online.")
			.define("magic.chrome.enabled", true);

	public static final ModConfigSpec.DoubleValue CHROME_SPELL_POWER_PER_POINT = B
			.comment("Spell power each part takes per point of its capacity cost (0.004 = 0.4%: the 44-point Sandevistan takes 17.5%).")
			.defineInRange("magic.chrome.spellPowerPerPoint", 0.004D, 0.0D, 0.05D);

	public static final ModConfigSpec.DoubleValue CHROME_MAX_MANA_PER_POINT = B
			.comment("Max mana each part takes per point of its capacity cost.")
			.defineInRange("magic.chrome.maxManaPerPoint", 0.004D, 0.0D, 0.05D);

	public static final ModConfigSpec.DoubleValue CHROME_MANA_REGEN_PER_POINT = B
			.comment("Mana regen each part takes per point of its capacity cost.")
			.defineInRange("magic.chrome.manaRegenPerPoint", 0.004D, 0.0D, 0.05D);

	public static final ModConfigSpec.DoubleValue CHROME_ROUNDING = B
			.comment("Each part's share is rounded to the nearest multiple of this (0.005 = half a percent); 0 = no rounding.")
			.defineInRange("magic.chrome.rounding", 0.005D, 0.0D, 0.1D);

	public static final ModConfigSpec.ConfigValue<String> CHROME_RESIST_MODE = B
			.comment("How cyberware gives spell resistance. ADDITIVE: resistPerPoint per capacity point in use, capped at resistCap.",
					"LOST_POTENTIAL: lostPotentialShare of the fraction of magic the parts took (max mana lost 94.5% -> +56.7% at 0.6).")
			.define("magic.chrome.resistMode", "ADDITIVE", o -> o instanceof String s && (s.equals("ADDITIVE") || s.equals("LOST_POTENTIAL")));

	public static final ModConfigSpec.DoubleValue CHROME_RESIST_PER_POINT = B
			.comment("ADDITIVE: spell resistance gained per capacity point in use (0.0065 = +0.65%).")
			.defineInRange("magic.chrome.resistPerPoint", 0.0065D, 0.0D, 1.0D);

	public static final ModConfigSpec.DoubleValue CHROME_RESIST_CAP = B
			.comment("ADDITIVE: most spell resistance cyberware can give (0.6 = +60%).")
			.defineInRange("magic.chrome.resistCap", 0.6D, 0.0D, 10.0D);

	public static final ModConfigSpec.DoubleValue CHROME_LOST_POTENTIAL_SHARE = B
			.comment("LOST_POTENTIAL: share of the lost fraction of magic given back as spell resistance.")
			.defineInRange("magic.chrome.lostPotentialShare", 0.6D, 0.0D, 2.0D);

	public static final ModConfigSpec.IntValue CHROME_ADDON_ADVANCEMENT_POINTS = B
			.comment("Chromeware capacity limit gained per completed advancement of other Create-based mods (any mod that requires Create;",
					"Create's own advancements already count through Chromeware). 0 = off.")
			.defineInRange("magic.chrome.addonAdvancementPoints", 2, 0, 64);

	public static final ModConfigSpec.IntValue CHROME_ADDON_ADVANCEMENT_CAP = B
			.comment("Most capacity those advancements can add. 360 brings the limit to 720 (38 start + 272 Create + 50 cyber limbs + 360),",
					"the cost of the costliest part in every slot.")
			.defineInRange("magic.chrome.addonAdvancementCap", 360, 0, 10000);

	public static final ModConfigSpec.ConfigValue<List<? extends String>> CHROME_ADDON_EXCLUDED = B
			.comment("Mod ids whose advancements do not count toward the capacity limit.")
			.defineListAllowEmpty("magic.chrome.addonAdvancementExcluded", List.of("create", "chromeware", "aerowarptics"), () -> "", o -> o instanceof String);

	public static final ModConfigSpec.DoubleValue SHIELD_MANA_REGEN_PENALTY = B
			.comment("Mana regen lost while a personal shield generator is worn (0.75 = -75%), on top of the cyberware penalty.")
			.defineInRange("magic.chrome.shieldManaRegenPenalty", 0.75D, 0.0D, 1.0D);

	public static final ModConfigSpec.ConfigValue<List<? extends String>> SHIELD_ITEMS = B
			.comment("Items that count as a worn personal shield (Curios slots).")
			.defineListAllowEmpty("magic.chrome.shieldItems",
					List.of("energyfield:shield_amulet", "energyfield:shield_amulet_stage0", "energyfield:shield_amulet_stage1",
							"energyfield:shield_amulet_stage2", "energyfield:shield_amulet_stage3"), () -> "",
					o -> o instanceof String s && ResourceLocation.tryParse(s) != null);

	public static final ModConfigSpec.ConfigValue<String> SANDEVISTAN_TIME_STOP = B
			.comment("Chromeware x Chronomancy: a Sandevistan keeps you outside stopped time. ACTIVE = only while it is running (activating it",
					"during a time stop frees you); INSTALLED = having one installed is enough; OFF = no immunity. Covers the world-wide time stop",
					"(you move like its caster) and Temporal Stasis / Paradox on you.")
			.define("magic.chrome.sandevistanTimeStopImmunity", "ACTIVE", o -> o instanceof String s && (s.equals("INSTALLED") || s.equals("ACTIVE") || s.equals("OFF")));

	public static final ModConfigSpec.IntValue SUPPRESSANT_STANDARD_TICKS = B
			.comment("Chromeware: protection one Standard Grade Immunosuppressant dose gives, in ticks (24000 = one Minecraft day; Chromeware: 12000).",
					"A dose taken while still protected adds this halved once per dose in a row (diminishing returns, as in Chromeware).")
			.defineInRange("chromeware.suppressantStandardTicks", 24000, 1, 10000000);

	public static final ModConfigSpec.IntValue SUPPRESSANT_MILITARY_TICKS = B
			.comment("Chromeware: protection one Military Grade dose gives, in ticks (12000 = half a Minecraft day; Chromeware: 3600).")
			.defineInRange("chromeware.suppressantMilitaryTicks", 12000, 1, 10000000);

	public static final ModConfigSpec.ConfigValue<List<? extends String>> BLESSING_EFFECTS = B
			.comment("Corail Tombstone's Blessing gives one of these effects when hit instead of any beneficial effect in the game. Empty = Tombstone's own pick.")
			.defineListAllowEmpty("holy.blessingEffects", List.of("minecraft:regeneration", "minecraft:resistance", "minecraft:absorption",
					"minecraft:fire_resistance", "minecraft:health_boost", "minecraft:instant_health",
					"irons_spellbooks:angel_wings", "irons_spellbooks:fortify", "paladins:blessed_strikes", "farmers_spell:holy_spirit_oil",
					"vampiresdelight:blessing", "mowziesmobs:sunblock", "tombstone:purification", "spell_power:holy"), () -> "",
					o -> o instanceof String s && ResourceLocation.tryParse(s) != null);

	public static final ModConfigSpec.ConfigValue<List<? extends String>> WILD_MAGIC_EXCLUDED = B
			.comment("Effects Wild Magic never gives. * matches anything.",
					"Defaults leave out effects that kill outright, explode, or turn you into a vampire/werewolf, Pastel's Divinity (and its loud music), and internal summon timers.")
			.defineListAllowEmpty("wildMagic.excludedEffects", List.of("pastel:fatal_slumber", "pastel:deadly_poison", "pastel:eternal_slumber",
					"ars_nouveau:blasting", "vinery:creeper_effect", "kaleidoscope_world_liquor:explosion", "kaleidoscope_world_liquor:respawn",
					"vampirism:sanguinare", "werewolves:lupus_sanguinem", "werewolves:un_werewolf", "tombstone:ghostly_shape", "pastel:ascension", "pastel:divinity", "*_timer"), () -> "",
					o -> o instanceof String s && !s.isBlank());

	public static final ModConfigSpec.ConfigValue<String> WILD_MAGIC_ENCHANTMENT = B
			.comment("Enchantment id that works as Wild Magic (a data-pack enchantment; nothing happens if it doesn't exist).")
			.define("wildMagic.enchantment", "foreverworld:wild_magic");

	public static final ModConfigSpec.IntValue WILD_MAGIC_MAX_HARMFUL_AMPLIFIER = B
			.comment("Highest amplifier (0 = level I) Wild Magic gives a harmful effect, so Instant Damage or Wither can't one-shot.")
			.defineInRange("wildMagic.maxHarmfulAmplifier", 1, 0, 255);

	public static final ModConfigSpec.ConfigValue<List<? extends String>> PRINTING_NO_TYPESET = B
			.comment("Glowbrick's Printing Press: enchanted books carrying any of these can't go through the Typesetter (so a single-use typesetting",
					"can't be turned back into a reusable one). Type blocks with durability always lose one point per print.")
			.defineListAllowEmpty("printing.noTypesetEnchantments", List.of("foreverworld:wild_magic"), () -> "",
					o -> o instanceof String s && ResourceLocation.tryParse(s) != null);

	public static final ModConfigSpec.ConfigValue<List<? extends String>> MOB_CAP_SCALE = B
			.comment("Natural-spawn mob caps per category as \"category=scale\" (vanilla caps: monster 70, creature 10, ambient 15, axolotls 5,",
					"underground_water_creature 5, water_creature 5, water_ambient 20). 0.5 = half as many. Spawners and structures are not affected.")
			.defineListAllowEmpty("spawning.mobCapScale", List.of("monster=0.5"), () -> "", o -> o instanceof String s && s.contains("="));

	public static final ModConfigSpec.BooleanValue BOSS_CHEST_ENABLED = B
			.comment("Lootr boss chests: bosses that don't leave their own chest drop nothing on the ground; a Lootr chest appears where they died",
					"that only players who damaged the boss can open, each with their own roll of the boss's loot table. Needs Lootr.")
			.define("bossChest.enabled", true);

	public static final ModConfigSpec.ConfigValue<List<? extends String>> BOSS_CHEST_BOSSES = B
			.comment("Entities (ids or #tags) that leave a boss chest.")
			.defineListAllowEmpty("bossChest.bosses", List.of("#c:bosses"), () -> "", o -> o instanceof String s && !s.isBlank());

	public static final ModConfigSpec.ConfigValue<List<? extends String>> BOSS_CHEST_EXCLUDED = B
			.comment("Bosses that already leave their own chest (or shouldn't get one).")
			.defineListAllowEmpty("bossChest.excluded", List.of("#twilightforest:bosses", "bosses_of_mass_destruction:gauntlet",
					"bosses_of_mass_destruction:obsidilith"), () -> "", o -> o instanceof String s && !s.isBlank());

	public static final ModConfigSpec.IntValue BOSS_CHEST_MINUTES = B
			.comment("Minutes before a boss chest vanishes, opened or not.")
			.defineInRange("bossChest.minutes", 20, 1, 1440);

	public static final ModConfigSpec.BooleanValue CYBER_ARMOR_FIT = B
			.comment("Chromeware: cyber limbs don't fit inside armour. Each cyber torso/arm takes 1/3 off the chestplate, each cyber leg 1/2 off",
					"leggings and boots (armour, toughness, knockback resistance and enchantment levels); the piece isn't drawn on replaced limbs,",
					"and with every covered part replaced it comes off and can't be worn (Curse of Binding or not).")
			.define("chromeware.armorFit", true);

	public static final ModConfigSpec.BooleanValue DOPPEL_CHROME = B
			.comment("Dark Doppelganger x Chromeware: the boss (and its mirror clones and shadow echoes) copies the summoner's cyber limbs and",
					"Shell colours and draws them, without Forever Fixes' cyberware magic penalties on the spell power it copies. If the summoner",
					"has a Sandevistan, the boss gets its own, with the timings below. Nothing else about the fight changes.")
			.define("chromeware.doppelganger.copyChrome", true);

	public static final ModConfigSpec.IntValue DOPPEL_SANDY_DURATION = B
			.comment("Dark Doppelganger: ticks its Sandevistan stays on (20 = 1 second).")
			.defineInRange("chromeware.doppelganger.sandevistanDuration", 100, 20, 1200);

	public static final ModConfigSpec.IntValue DOPPEL_SANDY_COOLDOWN = B
			.comment("Dark Doppelganger: ticks after its Sandevistan ends before it can use it again. The first use waits this long too,",
					"counted from when the boss appears.")
			.defineInRange("chromeware.doppelganger.sandevistanCooldown", 600, 0, 12000);

	public static final ModConfigSpec.DoubleValue DOPPEL_SANDY_RADIUS = B
			.comment("Dark Doppelganger: half-width in blocks of its Sandevistan field (Chromeware's player default is 48).")
			.defineInRange("chromeware.doppelganger.sandevistanRadius", 48.0D, 4.0D, 128.0D);

	public static final ModConfigSpec.IntValue DOPPEL_SANDY_SLOWDOWN = B
			.comment("Dark Doppelganger: how much its field slows everything but the boss, in percent (Chromeware's player default is 90).")
			.defineInRange("chromeware.doppelganger.sandevistanSlowdown", 90, 10, 95);

	public static final ModConfigSpec.DoubleValue DOPPEL_SANDY_RANGE = B
			.comment("Dark Doppelganger: it only turns its Sandevistan on when its target is within this many blocks.")
			.defineInRange("chromeware.doppelganger.sandevistanRange", 24.0D, 2.0D, 128.0D);

	public static final ModConfigSpec.IntValue NETWORK_TIMEOUT_SECONDS = B
			.comment("Seconds of silence before a connection is dropped as \"Timed out\" (vanilla: 30 s of no packets, 15 s for an unanswered",
					"keep-alive). A client that stalls for half a minute (shader compile, big Sable assembly, long GC) is otherwise thrown off the",
					"server. Applies to the server's view of each player and to the client's view of the server. 0 disables both limits;",
					"a dead connection then lasts until TCP gives up on it.")
			.defineInRange("network.timeoutSeconds", 600, 0, 86400);

	public static final ModConfigSpec.ConfigValue<String> SABLE_COM_ITEM = B
			.comment("Sable (client): while this item is held (either hand), every nearby sub-level shows its centre of mass as a marker visible",
					"through blocks, with its total mass above it. Measured on the client from the sub-level's blocks with Sable's own mass",
					"rules, once a second. Empty disables.")
			.define("sable.centerOfMass.item", "simulated:creative_physics_staff");

	public static final ModConfigSpec.BooleanValue SABLE_COM_BOUNDS = B
			.comment("Sable (client): also outline each sub-level's block bounds while the centre-of-mass item is held.")
			.define("sable.centerOfMass.bounds", false);

	public static final ModConfigSpec.BooleanValue PACK_OWNS_ARMS = B
			.comment("Chromeware x Mo' Bends: while a Bends pack node poses an arm, the pack owns that arm and Chromeware's blade / wire / launcher",
					"hold pose isn't copied over it. Off = Chromeware's arm pose always wins while an implant is out.")
			.define("chromeware.packOwnsArms", true);

	public static final ModConfigSpec.DoubleValue DOPPEL_CLOT_CHANCE = B
			.comment("Dark Doppelganger: chance (0-1) that the boss drops one of the item below when it dies after being summoned by a",
					"Sandevistan user. One roll, one item, Looting ignored; clones and echoes never drop it. 0 disables.")
			.defineInRange("chromeware.doppelganger.sandevistanDropChance", 0.1D, 0.0D, 1.0D);

	public static final ModConfigSpec.ConfigValue<String> DOPPEL_CLOT_ITEM = B
			.comment("Dark Doppelganger: the item for the drop above.")
			.define("chromeware.doppelganger.sandevistanDropItem", "relics:clot_of_time");

	public static final ModConfigSpec.BooleanValue VAMPIRE_PLAYERS_UNDEAD = B
			.comment("Vampirism: vampire players match the undead and sensitive_to_smite entity tags in predicates, so Smite (and anything else",
					"that checks those tags through a predicate) hits them like undead mobs. Vampire mobs are undead through the pack's tag data.")
			.define("holy.vampirePlayersUndead", true);

	public static final ModConfigSpec.DoubleValue VAMPIRE_HOLY_DAMAGE = B
			.comment("Vampirism: holy damage per second a vampire takes for each holy source on them (worn holy armor piece, held holy weapon,",
					"held enchanted book carrying one of those enchantments). 0 = off.")
			.defineInRange("holy.vampireDamagePerSource", 2.0D, 0.0D, 100.0D);

	public static final ModConfigSpec.IntValue VAMPIRE_HOLY_FIRE = B
			.comment("Seconds of fire a vampire is set to while touching a holy source.")
			.defineInRange("holy.vampireFireSeconds", 3, 0, 60);

	public static final ModConfigSpec.ConfigValue<List<? extends String>> HOLY_WORN_ENCHANTMENTS = B
			.comment("Enchantments that make worn armor holy.")
			.defineListAllowEmpty("holy.wornEnchantments", List.of("tombstone:blessing"), () -> "", o -> o instanceof String s && ResourceLocation.tryParse(s) != null);

	public static final ModConfigSpec.ConfigValue<List<? extends String>> HOLY_HELD_ENCHANTMENTS = B
			.comment("Enchantments that make a held item holy.")
			.defineListAllowEmpty("holy.heldEnchantments", List.of("minecraft:smite", "tombstone:sanctified"), () -> "", o -> o instanceof String s && ResourceLocation.tryParse(s) != null);

	public static final ModConfigSpec.BooleanValue HOLY_BOOKS = B
			.comment("A held enchanted book carrying any of the holy enchantments above burns a vampire too.")
			.define("holy.booksCount", true);

	public static final ModConfigSpec.BooleanValue CLOUD_WEATHER = B
			.comment("Simple Clouds + Petrichor / Hoarfrost: when Simple Clouds runs its own weather (DEFAULT mode, or SINGLE mode with a",
					"storm cloud type), each player's Petrichor rain type and Hoarfrost snow kind come from the cloud over that player",
					"instead of the vanilla weather flags, which Simple Clouds keeps off. Their own level-wide sync is replaced by a",
					"per-player one, Petrichor's ground wetness is tracked per player, and Hoarfrost drifts grow for players under snow.")
			.define("simpleclouds.weatherFromClouds", true);

	public static final ModConfigSpec.ConfigValue<List<? extends String>> CLOUD_WEATHER_TYPES = B
			.comment("Which Petrichor rain type and Hoarfrost snow kind a Simple Clouds cloud type brings: \"<cloud type>=<rain>,<snow>\".",
					"Rain types: drizzle, rain, downpour, thunderstorm. Snow kinds: light_snow, snow, heavy_snow, heavy_large_flakes,",
					"light_blizzard, blizzard, heavy_blizzard. A rain cloud type not listed brings rain and snow, a thunderstorm type brings",
					"thunderstorm and blizzard. Rain versus snow is the biome's choice, as in both mods; intensity follows the cloud's edge fade.")
			.defineListAllowEmpty("simpleclouds.cloudWeatherTypes",
					List.of("simpleclouds:stratus=rain,snow", "simpleclouds:nimbostratus=downpour,heavy_snow",
							"simpleclouds:cumulonimbus=thunderstorm,blizzard"),
					() -> "", o -> o instanceof String s && s.contains("="));

	public static final ModConfigSpec.DoubleValue CLOUD_STRIKE_SHARE = B
			.comment("Simple Clouds + Petrichor: share of Simple Clouds' lightning decisions that become a real Petrichor strike near a player",
					"standing in a thunder cloud's core, in place of that distant visual bolt. 0 leaves all lightning to Simple Clouds.")
			.defineInRange("simpleclouds.petrichorStrikeShare", 0.15D, 0.0D, 1.0D);

	public static final ModConfigSpec.BooleanValue CLOUD_DRIFTS = B
			.comment("Simple Clouds + Hoarfrost: snowdrifts grow around players under a snow-bringing cloud (Hoarfrost's own drifts setting",
					"and limits still apply).")
			.define("simpleclouds.hoarfrostDrifts", true);

	public static final ModConfigSpec.BooleanValue CLOUD_WIND = B
			.comment("Simple Clouds + Living Wind: cloud formations move with Living Wind's direction, and the drifting cloud shapes scroll",
					"along it, so Simple Clouds' rain slant, storm fog and Better Simple Clouds' rain wind follow the same wind.")
			.define("simpleclouds.windFromLivingWind", true);

	public static final ModConfigSpec.BooleanValue CLOUD_WIND_SPEED = B
			.comment("Simple Clouds + Living Wind: cloud speed also scales with Living Wind's strength (0.5x in a calm, up to 2x in a gale).")
			.define("simpleclouds.cloudSpeedFromWind", true);

	public static final ModConfigSpec SPEC = B.build();

	/** Read from tick code, which can run before the config file is loaded; default to enabled then. */
	public static boolean enabled(ModConfigSpec.BooleanValue value)
	{
		return !SPEC.isLoaded() || value.get();
	}

	private ForeverFixesCommonConfig()
	{
	}
}
