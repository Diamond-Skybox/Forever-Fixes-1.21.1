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

	public static final ModConfigSpec SPEC = B.build();

	private ForeverFixesCommonConfig()
	{
	}
}
