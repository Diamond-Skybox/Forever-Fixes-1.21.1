package com.diamondskybox.foreverfixes.mixin.hoarfrost;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hoarfrost: its "ungrey" of the sky colour keeps the hue. With its bright snowy sky on, Hoarfrost undoes the greying vanilla
 * applies to the sky in rain and thunder by subtracting a luma-based amount from each channel and dividing by what is left (a
 * quarter, in a thunderstorm), clamping each channel to 0..1. At dawn and dusk the sky's red and blue fall below that amount
 * while green does not, so the sky, and the fog and LODs that take their colour from it, turned green. The same brightening is
 * applied to the colour's luma instead and the colour scaled uniformly, so it only gets lighter.
 */
@Mixin(targets = "com.pockyl.hoarfrost.client.render.SnowSky", remap = false)
public abstract class MixinSnowSkyUngreyKeepsHue
{
	@Inject(method = "ungrey(Lnet/minecraft/world/phys/Vec3;FFF)Lnet/minecraft/world/phys/Vec3;", remap = false, require = 1,
			cancellable = true, at = @At("HEAD"))
	private static void foreverfixes$ungreyKeepsHue(Vec3 c, float level, float keep, float f, CallbackInfoReturnable<Vec3> cir)
	{
		final float m = 1F - level * keep;
		if (level <= 0F || m <= 0F)
		{
			cir.setReturnValue(c);
			return;
		}
		final float scale = m + f * (1F - m);
		final float luma = (float) (c.x * 0.3D + c.y * 0.59D + c.z * 0.11D);
		final float l = luma / scale * f * (1F - m);
		final float lifted = Mth.clamp((luma - l) / m, 0F, 1F);
		final double factor = luma > 1.0E-4F ? lifted / luma : 1D;
		cir.setReturnValue(new Vec3(Math.min(1D, c.x * factor), Math.min(1D, c.y * factor), Math.min(1D, c.z * factor)));
	}
}
