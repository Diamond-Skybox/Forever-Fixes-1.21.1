package com.diamondskybox.foreverfixes;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.UUID;

/**
 * Werewolves draws a transformed player as its werewolf model through the living-entity renderer, with the player as the
 * entity. MCA hides the vanilla model of every player it draws as a villager in that same renderer, so the werewolf model was
 * hidden too and a transformed MCA player showed nothing (Werewolves issue #182). While a player is in a transformed werewolf
 * form, MCA is told not to draw them; in human form the MCA look is back. Client only.
 */
public final class McaWerewolfForm
{
	private static MethodHandle getOpt;
	private static MethodHandle getForm;
	private static MethodHandle isTransformed;
	private static boolean lookedUp;

	private McaWerewolfForm()
	{
	}

	/** true while the player with this UUID is in a transformed (beast) werewolf form */
	public static boolean transformed(UUID uuid)
	{
		final Minecraft minecraft = Minecraft.getInstance();
		if (uuid == null || minecraft.level == null || !ForeverFixesConfig.enabled(ForeverFixesConfig.MCA_WEREWOLF_FORM))
		{
			return false;
		}
		final Player player = minecraft.level.getPlayerByUUID(uuid);
		if (player == null)
		{
			return false;
		}
		lookUp();
		if (getOpt == null)
		{
			return false;
		}
		try
		{
			final Optional<?> werewolf = (Optional<?>) getOpt.invoke(player);
			if (werewolf.isEmpty())
			{
				return false;
			}
			final Object form = getForm.invoke(werewolf.get());
			return form != null && (boolean) isTransformed.invoke(form);
		}
		catch (Throwable t)
		{
			return false;
		}
	}

	private static void lookUp()
	{
		if (lookedUp)
		{
			return;
		}
		lookedUp = true;
		try
		{
			final Class<?> werewolfPlayer = Class.forName("de.teamlapen.werewolves.entities.player.werewolf.WerewolfPlayer");
			final Class<?> werewolfForm = Class.forName("de.teamlapen.werewolves.api.entities.werewolf.WerewolfForm");
			getOpt = MethodHandles.publicLookup().findStatic(werewolfPlayer, "getOpt", MethodType.methodType(Optional.class, Player.class));
			getForm = MethodHandles.publicLookup().findVirtual(werewolfPlayer, "getForm", MethodType.methodType(werewolfForm));
			isTransformed = MethodHandles.publicLookup().findVirtual(werewolfForm, "isTransformed", MethodType.methodType(boolean.class));
		}
		catch (ReflectiveOperationException e)
		{
			getOpt = null;
		}
	}
}
