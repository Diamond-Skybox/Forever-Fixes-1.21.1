package com.diamondskybox.foreverfixes;

import net.neoforged.neoforge.common.ModConfigSpec;

/** config/foreverfixes-common.toml: settings that act on the server (or the single-player world). */
public final class ForeverFixesCommonConfig
{
	private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

	public static final ModConfigSpec.BooleanValue REMOVE_OPENCOMPUTERS_CHEST_LOOT = B
			.comment("OpenComputers: leave out the loot disks and components it adds to vanilla dungeon, temple, stronghold library and village",
					"chests. Its own structures' chests keep their loot.")
			.define("loot.removeOpenComputersChestLoot", false);

	public static final ModConfigSpec SPEC = B.build();

	private ForeverFixesCommonConfig()
	{
	}
}
