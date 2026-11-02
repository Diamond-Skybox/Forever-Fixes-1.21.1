package com.diamondskybox.foreverfixes.mixin.bscglass;

import com.diamondskybox.foreverfixes.StormFogFixes;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Better Simple Clouds: while its rain haze pass runs, the main depth it reads is the snapshot after the translucent blocks,
 * with the beacon glow in it: particles take the haze of what is behind them instead of counting as glass, and a beacon beam's
 * glow the haze of its own distance (see {@link StormFogFixes}).
 */
@Mixin(targets = "dev.bettersimpleclouds.rain.RainHaze", remap = false)
public abstract class MixinRainHazeDepth
{
	@Inject(method = "process", remap = false, require = 1, at = @At("HEAD"))
	private static void foreverfixes$begin(RenderTarget main, float partialTick, CallbackInfo ci)
	{
		StormFogFixes.setInRainHaze(true);
	}

	@Inject(method = "process", remap = false, require = 1, at = @At("RETURN"))
	private static void foreverfixes$end(RenderTarget main, float partialTick, CallbackInfo ci)
	{
		StormFogFixes.setInRainHaze(false);
	}
}
