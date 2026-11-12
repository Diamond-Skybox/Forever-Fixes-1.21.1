package com.diamondskybox.foreverfixes.mixin.simpleclouds;

import dev.nonamecrackers2.simpleclouds.client.renderer.WorldEffects;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.Color;

/**
 * Simple Clouds: its storm tint of the sky and fog colour blends in RGB. It blended hue, saturation and brightness separately, with
 * the hue in a straight line from the sky's to its storm blue; from the orange of a sunrise or sunset that line runs through green,
 * so a partly stormy dawn or dusk sky (and the fog and LODs that take its colour) turned green. The same storm colour is blended
 * with the same weight, channel by channel.
 */
@Mixin(value = WorldEffects.class, remap = false)
public abstract class MixinWorldEffectsRgbStormTint
{
	@Inject(method = "hsbLerp(FFFFFFF)Ljava/awt/Color;", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private static void foreverfixes$rgbBlend(float r, float g, float b, float targetHue, float targetSaturation, float targetBrightness,
			float lerp, CallbackInfoReturnable<Color> cir)
	{
		final int target = Color.HSBtoRGB(targetHue, Mth.clamp(targetSaturation, 0F, 1F), Mth.clamp(targetBrightness, 0F, 1F));
		final float tr = (target >> 16 & 0xFF) / 255F;
		final float tg = (target >> 8 & 0xFF) / 255F;
		final float tb = (target & 0xFF) / 255F;
		cir.setReturnValue(new Color(Mth.clamp(Mth.lerp(lerp, tr, r), 0F, 1F), Mth.clamp(Mth.lerp(lerp, tg, g), 0F, 1F),
				Mth.clamp(Mth.lerp(lerp, tb, b), 0F, 1F)));
	}
}
