package com.diamondskybox.foreverfixes.mixin.schoarfrost;

import com.diamondskybox.foreverfixes.FlakesAfterStormFog;
import com.pockyl.hoarfrost.client.SnowClient;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hoarfrost: the weather-pass flake draw is held until after Simple Clouds' storm fog (see {@link FlakesAfterStormFog}). */
@Mixin(value = SnowClient.class, remap = false)
public abstract class MixinSnowClientHoldSnowfall
{
	@Inject(method = "renderSnowfall", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private static void foreverfixes$holdForStormFog(LightTexture lightTexture, float partialTick, double camX, double camY, double camZ, CallbackInfo ci)
	{
		if (FlakesAfterStormFog.hold(lightTexture, partialTick, camX, camY, camZ))
		{
			ci.cancel();
		}
	}
}
