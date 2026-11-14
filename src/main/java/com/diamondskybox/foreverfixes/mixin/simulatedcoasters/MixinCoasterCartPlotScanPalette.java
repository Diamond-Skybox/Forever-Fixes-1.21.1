package com.diamondskybox.foreverfixes.mixin.simulatedcoasters;

import com.diamondskybox.foreverfixes.ForeverFixesCommonConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Simulated Coasters: the coaster-bearing scan skips chunk sections whose block palette can't hold a bearing. Every physics tick it
 * checks every Sable ship for coaster carts (rivets, track snapping, train links) by reading all 4096 blocks of every non-empty
 * section of the ship's plot; its cache only lasts one game tick. About 7% of the server thread with ships around and no coaster at
 * all. A section whose palette has no bearing block is now treated as empty: same result, and ships without carts cost a palette
 * look-up per section.
 */
@Mixin(targets = "dev.silvergold.simulatedcoasters.track.cart.CoasterCartPlotScan", remap = false)
public abstract class MixinCoasterCartPlotScanPalette
{
	@WrapOperation(method = "scanBearingCells(Ldev/ryanhcode/sable/sublevel/plot/LevelPlot;Lnet/minecraft/world/level/block/Block;)Ldev/silvergold/simulatedcoasters/track/cart/CoasterCartPlotScan$BearingScanResult;",
			remap = false, require = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;hasOnlyAir()Z"))
	private static boolean foreverfixes$skipSectionsWithoutBearing(LevelChunkSection section, Operation<Boolean> original, @Local(argsOnly = true) Block bearingBlock)
	{
		if (original.call(section))
		{
			return true;
		}
		return ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.COASTER_PALETTE_SCAN) && !section.maybeHas(state -> state.is(bearingBlock));
	}
}
