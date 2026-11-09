package com.diamondskybox.foreverfixes;

import com.chromeware.client.cyberware.GorillaAnimation;
import com.chromeware.client.launcher.LauncherAnimation;
import com.chromeware.client.mantis.MantisAnimation;
import com.chromeware.client.monowire.MonowireAnimation;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;

/** Whether one of Chromeware's arm implant clips (Mantis Blades, Monowire, Projectile Launcher, Gorilla Arms) is posing this player's arms. */
public final class ChromeArms
{
	private ChromeArms()
	{
	}

	/** A Mantis Blades or Monowire clip is posing the arms and no Launcher / Gorilla clip (which also move arm positions) is. */
	public static boolean rotationOnly(LivingEntity entity)
	{
		if (!(entity instanceof AbstractClientPlayer player))
		{
			return false;
		}
		try
		{
			return (MantisAnimation.rendering(player) || MonowireAnimation.rendering(player)) && !LauncherAnimation.rendering(player) && !GorillaAnimation.rendering(player);
		}
		catch (RuntimeException e)
		{
			return false;
		}
	}

	public static boolean animating(LivingEntity entity)
	{
		if (!(entity instanceof AbstractClientPlayer player))
		{
			return false;
		}
		try
		{
			return MantisAnimation.rendering(player) || MonowireAnimation.rendering(player) || LauncherAnimation.rendering(player) || GorillaAnimation.rendering(player);
		}
		catch (RuntimeException e)
		{
			return false;
		}
	}
}
