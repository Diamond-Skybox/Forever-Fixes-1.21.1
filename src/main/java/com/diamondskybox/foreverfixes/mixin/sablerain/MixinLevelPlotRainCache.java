package com.diamondskybox.foreverfixes.mixin.sablerain;

import com.diamondskybox.foreverfixes.ShipRainCover;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Sable: a block on a ship changed, so its rain cover reads the ship's column tops again (see {@link ShipRainCover}). */
@Mixin(value = LevelPlot.class, remap = false)
public abstract class MixinLevelPlotRainCache
{
	@Inject(method = "onBlockChange", remap = false, require = 1, at = @At("HEAD"))
	private void foreverfixes$shipBlocksChanged(BlockPos pos, BlockState state, CallbackInfo ci)
	{
		ShipRainCover.onPlotBlockChange((LevelPlot) (Object) this);
	}
}
