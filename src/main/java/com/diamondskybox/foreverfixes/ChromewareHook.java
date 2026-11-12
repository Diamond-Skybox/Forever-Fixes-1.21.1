package com.diamondskybox.foreverfixes;

import com.chromeware.cyberware.CyberwareCapacity;
import com.chromeware.cyberware.CyberwareStats;
import net.minecraft.world.item.ItemStack;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.cyberware.SandevistanImplant;
import net.minecraft.world.entity.player.Player;

/** Chromeware calls for {@link ChromeMagic}; only loaded when Chromeware is installed. */
public final class ChromewareHook
{
	private ChromewareHook()
	{
	}

	/** Whether this player's Sandevistan puts them outside stopped time (per magic.chrome.sandevistanTimeStopImmunity). */
	public static boolean outsideStoppedTime(Player player)
	{
		final String mode = ForeverFixesCommonConfig.SPEC.isLoaded() ? ForeverFixesCommonConfig.SANDEVISTAN_TIME_STOP.get() : "ACTIVE";
		try
		{
			return switch (mode)
			{
				// SandevistanImplant.active() only means "installed with a torso"; running = active ticks left in its state
				case "ACTIVE" -> SandevistanImplant.active(player) && player.getData(PlayerData.SANDEVISTAN_STATE.get()).activeTicks() > 0;
				case "INSTALLED" -> SandevistanImplant.get(player).installed();
				default -> false;
			};
		}
		catch (RuntimeException e)
		{
			return false;
		}
	}

	/** Cyberware capacity this item takes up when installed (0 for anything that is not cyberware). */
	static int itemCapacity(ItemStack stack)
	{
		try
		{
			return Math.max(0, CyberwareStats.get(stack).capacity());
		}
		catch (RuntimeException e)
		{
			return 0;
		}
	}

	/** Capacity costs of the player's installed parts (cyberware with a cost), costliest first. */
	static int[] partCosts(Player player)
	{
		try
		{
			return CyberwareCapacity.installed(player).stream().mapToInt(ChromewareHook::itemCapacity).filter(c -> c > 0)
					.boxed().sorted(java.util.Comparator.reverseOrder()).mapToInt(Integer::intValue).toArray();
		}
		catch (RuntimeException e)
		{
			return new int[0];
		}
	}
}
