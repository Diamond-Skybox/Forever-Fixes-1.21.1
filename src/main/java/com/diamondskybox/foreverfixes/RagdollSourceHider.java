package com.diamondskybox.foreverfixes;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

/**
 * Sable: Ragdolls turns a player into a physics ragdoll (drawn by its own block-entity renderer) and hides the player by
 * seating them, invisible, on a ragdoll seat. Mo' Bends draws players from the render-player event, which ignores that, so the
 * normal (MCA) body showed next to the ragdoll. The render-player event of a player on a ragdoll seat is cancelled first.
 */
public final class RagdollSourceHider
{
	private static final String RAGDOLL_SEAT = "dev.leo.sableplayerragdoll.entity.RagdollSeatEntity";

	private RagdollSourceHider()
	{
	}

	public static void onRenderPlayer(RenderPlayerEvent.Pre event)
	{
		final Entity vehicle = event.getEntity().getVehicle();
		if (vehicle != null && RAGDOLL_SEAT.equals(vehicle.getClass().getName())
				&& ForeverFixesConfig.enabled(ForeverFixesConfig.RAGDOLL_HIDE_SOURCE))
		{
			event.setCanceled(true);
		}
	}
}
