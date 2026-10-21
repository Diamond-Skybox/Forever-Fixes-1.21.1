package com.diamondskybox.foreverfixes.mixin.cosmonautics;

import com.diamondskybox.foreverfixes.ShipReseat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Create: Cosmonautics re-seats players on their seat right after moving a ship into deep space ({@code lambda$init$1}) and back
 * out of it ({@code lambda$exitDeepSpace$4}): {@code player.startRiding(level.getEntity(seat), true)}. Hands each re-seat to
 * {@link ShipReseat} for a short follow-up. Synthetic lambda names of Cosmonautics 26.08.307.
 */
@Mixin(targets = "dev.devce.rocketnautics.content.physics.SpaceTransitionHandler", remap = false)
public abstract class MixinSpaceTransitionHandler
{
	@Inject(method = { "lambda$init$1", "lambda$exitDeepSpace$4" }, remap = false, require = 2, at = @At("TAIL"))
	private static void foreverfixes$followUpReseat(ServerLevel level, ServerPlayer player, UUID vehicle, CallbackInfo ci)
	{
		ShipReseat.afterCosmonauticsReseat(level, player, vehicle);
	}
}
