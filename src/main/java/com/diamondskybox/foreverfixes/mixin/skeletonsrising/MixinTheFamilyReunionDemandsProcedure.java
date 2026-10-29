package com.diamondskybox.foreverfixes.mixin.skeletonsrising;

import com.diamondskybox.foreverfixes.ForeverFixesCommonConfig;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skeletons Rising grants "The Family Reunion" from an MCreator procedure subscribed to every entity's tick, on client and server:
 * each tick of each entity searched a 16-block box around it for decayed skeletons and then for players (spark on the server:
 * ~1.8% of the server thread, growing with entity count times local crowding). Only players receive the advancement, so the
 * procedure now runs on players' ticks only; the searches then start from the player instead of from every mob near them.
 */
@Mixin(targets = "net.mcreator.skeletonsrising.procedures.TheFamilyReunionDemandsProcedure", remap = false)
public abstract class MixinTheFamilyReunionDemandsProcedure
{
	@Inject(method = "onEntityTick", remap = false, require = 1, at = @At("HEAD"), cancellable = true)
	private static void foreverfixes$playersOnly(EntityTickEvent.Pre event, CallbackInfo ci)
	{
		if (!(event.getEntity() instanceof Player) && ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.SKELETONS_RISING_PLAYERS_ONLY))
		{
			ci.cancel();
		}
	}
}
