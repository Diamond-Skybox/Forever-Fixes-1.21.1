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

	public static final ModConfigSpec SPEC = B.build();

	private ForeverFixesCommonConfig()
	{
	}
}
