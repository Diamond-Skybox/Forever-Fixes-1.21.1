package com.diamondskybox.foreverfixes.mixin.sablebscrain;

import com.diamondskybox.foreverfixes.ShipWetGround;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Better Simple Clouds' rain haze: the nearest Sable ships go to its shader, for wet decks in the ship's frame (see {@link ShipWetGround}). */
@Mixin(targets = "dev.bettersimpleclouds.rain.RainHaze", remap = false)
public abstract class MixinBscRainHazeShipSpace
{
	@Shadow(remap = false)
	private static PostChain chain;

	@Inject(method = "process", remap = false, require = 1, at = @At("HEAD"))
	private static void foreverfixes$shipsToShader(RenderTarget main, float partialTick, CallbackInfo ci)
	{
		ShipWetGround.beforeRainHaze(chain);
	}
}
