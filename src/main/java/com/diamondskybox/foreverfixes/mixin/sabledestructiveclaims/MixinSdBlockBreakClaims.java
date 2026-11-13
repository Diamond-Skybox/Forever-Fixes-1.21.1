package com.diamondskybox.foreverfixes.mixin.sabledestructiveclaims;

import com.diamondskybox.foreverfixes.ClaimedLand;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.plot.EmbeddedPlotLevelAccessor;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sable: Destructive: its two block removers leave claimed land alone, which also covers what doesn't go through the impact queue:
 * landing shockwave craters, explosions tearing ship blocks out, penetration into the next blocks, and other mods using its API.
 */
@Mixin(targets = "com.destroynautics.sabledestructive.impact.ImpactBlockBreak", remap = false)
public abstract class MixinSdBlockBreakClaims
{
	@Inject(method = "destroyNoBlockDrop(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)V", remap = false, require = 1,
			at = @At("HEAD"), cancellable = true)
	private static void foreverfixes$world(ServerLevel level, BlockPos pos, CallbackInfo ci)
	{
		if (ClaimedLand.protects(level, pos))
		{
			ci.cancel();
		}
	}

	@Inject(method = "destroyPlotNoBlockDrop(Lnet/minecraft/server/level/ServerLevel;Ldev/ryanhcode/sable/sublevel/ServerSubLevel;Ldev/ryanhcode/sable/sublevel/plot/EmbeddedPlotLevelAccessor;Lnet/minecraft/core/BlockPos;)V",
			remap = false, require = 1, at = @At("HEAD"), cancellable = true)
	private static void foreverfixes$ship(ServerLevel level, ServerSubLevel ship, EmbeddedPlotLevelAccessor accessor, BlockPos plotPos, CallbackInfo ci)
	{
		if (ClaimedLand.protects(level, plotPos))
		{
			ci.cancel();
		}
	}
}
