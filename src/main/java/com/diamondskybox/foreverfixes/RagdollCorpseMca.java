package com.diamondskybox.foreverfixes;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.UUID;

/**
 * Sable: Ragdoll Corpse draws a corpse on a stand-in doll entity, which carries the owner's UUID while a part of an MCA player's
 * corpse is drawn (see the ragdoll mixin). MCA: female gender physics only asks MCA for a look when the entity is an MCA villager
 * or a player, so on the doll it found none: breast size 0 and no texture, and the corpse had no breasts. For such a doll this
 * returns the owner's MCA look; for every other entity it returns null so the gender mod's own lookup runs. A doll is the only
 * non-player entity that carries an MCA player's UUID.
 */
public final class RagdollCorpseMca
{
	private static Class<?> villagerLike;
	private static MethodHandle useVillagerRenderer;
	private static MethodHandle getPlayerData;
	private static boolean lookedUp;

	private RagdollCorpseMca()
	{
	}

	/** the corpse owner's MCA VillagerLike for a corpse doll of an MCA player, otherwise null */
	public static Object ownerLook(Entity entity)
	{
		if (entity == null || entity instanceof Player || !ForeverFixesConfig.enabled(ForeverFixesConfig.RAGDOLL_MCA_BREASTS))
		{
			return null;
		}
		lookUp();
		if (villagerLike == null || villagerLike.isInstance(entity))
		{
			return null;
		}
		try
		{
			final UUID uuid = entity.getUUID();
			if (!(boolean) useVillagerRenderer.invokeExact(uuid))
			{
				return null;
			}
			return ((Optional<?>) getPlayerData.invokeExact(uuid)).orElse(null);
		}
		catch (Throwable t)
		{
			return null;
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
			final Class<?> client = Class.forName("net.conczin.mca.MCAClient");
			useVillagerRenderer = MethodHandles.publicLookup().findStatic(client, "useVillagerRenderer", MethodType.methodType(boolean.class, UUID.class));
			getPlayerData = MethodHandles.publicLookup().findStatic(client, "getPlayerData", MethodType.methodType(Optional.class, UUID.class));
			villagerLike = Class.forName("net.conczin.mca.entity.VillagerLike");
		}
		catch (ReflectiveOperationException e)
		{
			villagerLike = null;
		}
	}
}
