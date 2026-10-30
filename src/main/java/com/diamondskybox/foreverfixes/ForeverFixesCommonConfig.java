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

	public static final ModConfigSpec.ConfigValue<List<? extends String>> FLOATING_STRUCTURES = B
			.comment("Structures that float in the air (sky islands, airships): when one is placed, the terrain under its whole footprint is",
					"checked and the structure is raised until it clears the ground by worldgen.floatingStructures.clearance blocks; one that",
					"cannot fit under the build limit is not placed. Structure ids, or \"namespace:*\" for all of a mod's structures.")
			.defineListAllowEmpty("worldgen.floatingStructures.ids", List.of("create_sky_village:*", "skyvillages:*", "shellbound_for_airship:*"),
					() -> "", o -> o instanceof String s && !s.isBlank());

	public static final ModConfigSpec.IntValue FLOATING_STRUCTURES_CLEARANCE = B
			.comment("Fewest blocks of air between a floating structure's lowest block and the terrain (or water surface) below it.")
			.defineInRange("worldgen.floatingStructures.clearance", 8, 0, 256);

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
