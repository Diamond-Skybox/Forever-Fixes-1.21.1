package com.diamondskybox.foreverfixes.mixin.schoarfrost;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import dev.nonamecrackers2.simpleclouds.client.renderer.SimpleCloudsRenderer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hoarfrost + Simple Clouds: Hoarfrost's snow fog colour takes Simple Clouds' storm darkening. Simple Clouds darkens the world and
 * the sky by a factor of the storm overhead (down to a tenth); Hoarfrost lightens the fog colour for its snowy look, and the same
 * colour goes to the Distant Horizons fog, so a blizzard's haze glowed pale against the darkened world. The colour Hoarfrost returns
 * (used for the vanilla fog and the LOD fog alike) is scaled by Simple Clouds' darken factor.
 */
@Mixin(targets = "com.pockyl.hoarfrost.client.render.SnowSky", remap = false)
public abstract class MixinSnowSkyFogDarkensWithStorm
{
	@Inject(method = "fogColor", remap = false, require = 1, at = @At("RETURN"))
	private static void foreverfixes$darkenWithStorm(float r, float g, float b, float partialTick, CallbackInfoReturnable<float[]> cir)
	{
		final float[] color = cir.getReturnValue();
		if (color == null || color.length < 3 || !ForeverFixesConfig.enabled(ForeverFixesConfig.HOARFROST_FOG_DARKENS_WITH_STORM)
				|| !SimpleCloudsRenderer.canRenderInDimension(Minecraft.getInstance().level))
		{
			return;
		}
		SimpleCloudsRenderer.getOptionalInstance().ifPresent(renderer -> {
			final float factor = renderer.getWorldEffectsManager().getDarkenFactor(partialTick);
			if (factor < 1F)
			{
				color[0] *= factor;
				color[1] *= factor;
				color[2] *= factor;
			}
		});
	}
}
