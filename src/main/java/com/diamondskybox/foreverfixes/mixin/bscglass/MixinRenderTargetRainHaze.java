package com.diamondskybox.foreverfixes.mixin.bscglass;

import com.diamondskybox.foreverfixes.StormFogFixes;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The rain haze chain reads the main depth through "minecraft:main:depth", which asks the main target (see {@link MixinRainHazeDepth}). */
@Mixin(value = RenderTarget.class, remap = false)
public abstract class MixinRenderTargetRainHaze
{
	@Inject(method = "getDepthTextureId", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private void foreverfixes$rainHazeDepth(CallbackInfoReturnable<Integer> cir)
	{
		final int depth = StormFogFixes.rainHazeDepth((RenderTarget) (Object) this);
		if (depth > 0)
		{
			cir.setReturnValue(depth);
		}
	}
}
