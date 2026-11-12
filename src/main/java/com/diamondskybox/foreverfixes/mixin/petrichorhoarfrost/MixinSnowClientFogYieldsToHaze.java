package com.diamondskybox.foreverfixes.mixin.petrichorhoarfrost;

import com.diamondskybox.foreverfixes.SnowHazeState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Hoarfrost: its flat distance fog (and the haze distances it gives Distant Horizons) stays out while Petrichor's haze covers the snow
 * (see {@code SnowHaze}); its fog colour and sky tint stay. The haze does the visibility then, in layers by distance and height, and
 * reaches the LODs through their depth.
 */
@Mixin(targets = "com.pockyl.hoarfrost.client.SnowClient", remap = false)
public abstract class MixinSnowClientFogYieldsToHaze
{
	@WrapOperation(method = "onRenderFog", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lcom/pockyl/hoarfrost/client/ClientWeather;snow()F"))
	private static float foreverfixes$yieldToHaze(Operation<Float> original)
	{
		return SnowHazeState.hazeCoversSnow() ? 0F : original.call();
	}
}
