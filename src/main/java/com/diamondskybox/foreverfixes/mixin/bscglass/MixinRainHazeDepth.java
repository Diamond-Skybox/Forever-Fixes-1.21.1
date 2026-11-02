package com.diamondskybox.foreverfixes.mixin.bscglass;

import com.diamondskybox.foreverfixes.StormFogFixes;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Camera;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Better Simple Clouds' rain haze: while it is set up, the depth it asks Better Simple Clouds for (what lies behind glass) has
 * the translucent particles in it, and while its pass runs the main depth it reads has the beacon glow in it. Particles then
 * take the haze of their own distance instead of counting as glass or being hazed by what is behind them, and a beacon beam's
 * glow the haze of its own distance (see {@link StormFogFixes}).
 */
@Mixin(targets = "dev.bettersimpleclouds.rain.RainHaze", remap = false)
public abstract class MixinRainHazeDepth
{
	@Inject(method = "drawChecked", remap = false, require = 1, at = @At("HEAD"))
	private static void foreverfixes$setupBegin(Matrix4f modelView, Matrix4f projMat, Camera camera, float partialTick, float strength, float wetness,
			CallbackInfo ci)
	{
		StormFogFixes.setInRainHazeSetup(true);
	}

	@Inject(method = "drawChecked", remap = false, require = 1, at = @At("RETURN"))
	private static void foreverfixes$setupEnd(Matrix4f modelView, Matrix4f projMat, Camera camera, float partialTick, float strength, float wetness,
			CallbackInfo ci)
	{
		StormFogFixes.setInRainHazeSetup(false);
	}

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
