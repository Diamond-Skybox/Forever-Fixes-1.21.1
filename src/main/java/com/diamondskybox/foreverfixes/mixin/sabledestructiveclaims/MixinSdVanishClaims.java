package com.diamondskybox.foreverfixes.mixin.sabledestructiveclaims;

import com.diamondskybox.foreverfixes.ClaimedLand;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Sable: Destructive: no "block shattered" dust and sound for a block in claimed land, since it stays put (shockwave craters play it before breaking). */
@Mixin(targets = "com.destroynautics.sabledestructive.impact.VanishEffect", remap = false)
public abstract class MixinSdVanishClaims
{
	@Inject(method = "play(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;D)V", remap = false,
			require = 1, at = @At("HEAD"), cancellable = true)
	private static void foreverfixes$claims(ServerLevel level, BlockPos pos, BlockState state, double impactSpeed, CallbackInfo ci)
	{
		if (ClaimedLand.protects(level, pos))
		{
			ci.cancel();
		}
	}
}
