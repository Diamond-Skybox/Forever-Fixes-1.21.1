package com.diamondskybox.foreverfixes.mixin.hoarfrost;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Hoarfrost: its snow fog only comes with snow from real weather. Its fog distances (and the haze it hands Distant Horizons), sky
 * greying and fog colour all scale with its snow level, which a fair-weather flurry raises as much as a snowstorm does; a light
 * flurry under a small cloud then closed the view to a pale wall at its light-snow visibility (about 240 blocks). Real snow sets
 * the level's rain level (Simple Clouds from the cloud at the camera, or vanilla snowfall), flurries do not, so for the fog the
 * snow level is scaled by the rain level. The flakes, cover and sounds keep the full snow level.
 */
@Mixin(targets = { "com.pockyl.hoarfrost.client.SnowClient", "com.pockyl.hoarfrost.client.render.SnowSky" }, remap = false)
public abstract class MixinHoarfrostFogOnlyUnderSnowClouds
{
	@WrapOperation(method = { "onRenderFog", "fogColor", "skyColor", "cloudColor" }, remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lcom/pockyl/hoarfrost/client/ClientWeather;snow()F"))
	private static float foreverfixes$fogOnlyUnderSnowClouds(Operation<Float> original)
	{
		final float snow = original.call();
		if (snow <= 0F || !ForeverFixesConfig.enabled(ForeverFixesConfig.HOARFROST_FOG_ONLY_UNDER_SNOW_CLOUDS))
		{
			return snow;
		}
		final Minecraft mc = Minecraft.getInstance();
		final float rain = mc.level == null ? 0F : mc.level.getRainLevel(1.0F);
		return snow * Mth.clamp(rain, 0F, 1F);
	}
}
