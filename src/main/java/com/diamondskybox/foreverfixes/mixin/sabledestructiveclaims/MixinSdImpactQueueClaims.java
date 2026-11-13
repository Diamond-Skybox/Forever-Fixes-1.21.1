package com.diamondskybox.foreverfixes.mixin.sabledestructiveclaims;

import com.destroynautics.sabledestructive.impact.BreakQueue;
import com.diamondskybox.foreverfixes.ClaimedLand;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sable: Destructive: a crash contact on a block in claimed land does nothing (no break, no detach, no penetration, no self-damage, no
 * dust), whether the block is terrain or part of a ship over claimed land (see {@link ClaimedLand}). Contacts are resolved here, one
 * per queued hit, on the server thread.
 */
@Mixin(targets = "com.destroynautics.sabledestructive.impact.UniversalImpactCallback", remap = false)
public abstract class MixinSdImpactQueueClaims
{
	@Inject(method = "processQueueEntry(Lcom/destroynautics/sabledestructive/impact/BreakQueue$Entry;)V", remap = false, require = 1,
			at = @At("HEAD"), cancellable = true)
	private static void foreverfixes$claims(BreakQueue.Entry e, CallbackInfo ci)
	{
		if (e != null && ClaimedLand.protects(e.level(), BlockPos.of(e.packedPos())))
		{
			ci.cancel();
		}
	}

	/** detaches are deferred a tick, so a claim made in between still counts */
	@Inject(method = "detachAsSubLevel(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;DDDDDDD)V",
			remap = false, require = 1, at = @At("HEAD"), cancellable = true)
	private static void foreverfixes$detach(net.minecraft.server.level.ServerLevel level, BlockPos pos, net.minecraft.world.level.block.state.BlockState state,
			double tx, double ty, double tz, double normalSpeed, double fxX, double fxY, double fxZ, CallbackInfo ci)
	{
		if (ClaimedLand.protects(level, pos))
		{
			ci.cancel();
		}
	}
}
