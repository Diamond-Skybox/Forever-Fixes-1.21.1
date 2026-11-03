package com.diamondskybox.foreverfixes.mixin.bscglass;

import com.diamondskybox.foreverfixes.StormFogFixes;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Camera;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.resources.ResourceLocation;
import com.diamondskybox.foreverfixes.ForeverFixesConfig;

/**
 * Better Simple Clouds' rain haze: while it is set up, the depth it asks Better Simple Clouds for (what lies behind glass) has
 * the translucent particles in it, and while its pass runs the main depth it reads has the beacon glow in it. Particles then
 * take the haze of their own distance instead of counting as glass or being hazed by what is behind them, and a beacon beam's
 * glow the haze of its own distance (see {@link StormFogFixes}). Its chain is Forever Fixes' copy, whose wet ground has no dry
 * dashed lines along block edges (assets/foreverfixes/shaders/program/bsc_rain_haze.fsh).
 */
@Mixin(targets = "dev.bettersimpleclouds.rain.RainHaze", remap = false)
public abstract class MixinRainHazeDepth
{
	private static final ResourceLocation FOREVERFIXES$CHAIN = ResourceLocation.fromNamespaceAndPath("foreverfixes", "shaders/post/bsc_rain_haze.json");

	@ModifyArg(method = "ensureChain", remap = false, require = 1, index = 3,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/client/renderer/PostChain;<init>(Lnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/server/packs/resources/ResourceProvider;Lcom/mojang/blaze3d/pipeline/RenderTarget;Lnet/minecraft/resources/ResourceLocation;)V"))
	private static ResourceLocation foreverfixes$seamlessWetGround(ResourceLocation chain)
	{
		return ForeverFixesConfig.enabled(ForeverFixesConfig.BSC_WET_GROUND_SEAMS) ? FOREVERFIXES$CHAIN : chain;
	}

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
