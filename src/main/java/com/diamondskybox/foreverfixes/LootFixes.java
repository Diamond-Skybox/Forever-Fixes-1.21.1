package com.diamondskybox.foreverfixes;

import net.neoforged.neoforge.event.LootTableLoadEvent;

/**
 * OpenComputers adds two named pools (loot disks, components) to vanilla chest tables as they load, with no setting to turn them
 * off. They are removed again after every other listener has run; mods that sample vanilla chest tables for their own chests
 * (Loot Integrations) then do not get them either.
 */
public final class LootFixes
{
	private static final String[] OPENCOMPUTERS_POOLS = { "opencomputers_disks", "opencomputers_components" };

	private LootFixes()
	{
	}

	public static void onLootTableLoad(LootTableLoadEvent event)
	{
		if (!ForeverFixesCommonConfig.SPEC.isLoaded() || !ForeverFixesCommonConfig.REMOVE_OPENCOMPUTERS_CHEST_LOOT.get())
		{
			return;
		}
		for (String pool : OPENCOMPUTERS_POOLS)
		{
			event.getTable().removePool(pool);
		}
	}
}
