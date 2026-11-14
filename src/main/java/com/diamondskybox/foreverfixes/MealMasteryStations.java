package com.diamondskybox.foreverfixes;

import com.xirc.mealmastery.config.ConfigManager;
import com.xirc.mealmastery.event.CulinaryHooks;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

/**
 * Meal Mastery counts cooking at Hands On Delight's stations. Meal Mastery credits a dish when the server sees a right-click on a
 * Farmer's Delight workstation (a short attribution window, then the dish turning up in the inventory or dropping at the block) and
 * applies its cooking-speed bonus during that window. Hands On Delight's stations run on Diegesis, which cancels the click on the
 * client and talks to the server with its own packets, so the server never saw a click: no credit, no discovery, no speed bonus.
 * Here every station open and action counts as that click, and an open station keeps the window going (like the pot's menu does).
 * Meal Mastery restarts the window, inventory baseline included, on every click, so a new one only starts once the last one is over:
 * restarting it early could swallow a dish handed over in the same tick, before Meal Mastery's own tick credited it.
 */
public final class MealMasteryStations
{
	private static final Map<UUID, Window> WINDOWS = new HashMap<>();
	private static boolean failed;

	private record Window(BlockPos pos, long tick)
	{
	}

	private MealMasteryStations()
	{
	}

	public static void used(ServerPlayer player, BlockPos pos)
	{
		if (failed || player == null || pos == null
				|| !ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.MEALMASTERY_STATIONS))
		{
			return;
		}
		try
		{
			final long now = player.server.getTickCount();
			final Window last = WINDOWS.get(player.getUUID());
			if (last != null && last.pos().equals(pos) && now >= last.tick()
					&& now <= last.tick() + ConfigManager.server().automation.attributionWindowTicks)
			{
				return; // Meal Mastery's window for this station is still open
			}
			WINDOWS.put(player.getUUID(), new Window(pos.immutable(), now));
			CulinaryHooks.onBlockUsed(player, pos, player.serverLevel().getBlockState(pos));
		}
		catch (RuntimeException | LinkageError e)
		{
			failed = true;
			ForeverFixes.LOGGER.warn("Meal Mastery station bridge failed and is off until restart", e);
		}
	}

	public static void forget(UUID player)
	{
		WINDOWS.remove(player);
	}
}
