package com.diamondskybox.foreverfixes.mixin.rocketnautics;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Create: Cosmonautics rebuilds the sun's 64x64 "star plasma" texture on the render thread every game tick, running warped fractal
 * noise (7 fbm passes) per pixel. It is drawn wherever the universe sky is, the overworld included, at any altitude; a spark profile
 * on the ground put it at a quarter of all render-thread time. The texture is now rebuilt every starTextureIntervalTicks instead.
 */
@Mixin(targets = "dev.devce.rocketnautics.client.SkyHandler", remap = false)
public abstract class MixinSkyHandlerStarTexture
{
	@Shadow(remap = false)
	public static ResourceLocation STAR_PLASMA_TEXTURE_ID;

	@Shadow(remap = false)
	private static long lastPlasmaUpdateTick;

	@Inject(method = "ensureStarPlasmaTexture", remap = false, require = 1, at = @At("HEAD"), cancellable = true)
	private static void foreverfixes$throttleStarTexture(CallbackInfo ci)
	{
		final ClientLevel level = Minecraft.getInstance().level;
		if (STAR_PLASMA_TEXTURE_ID == null || level == null || !ForeverFixesConfig.enabled(ForeverFixesConfig.THROTTLE_STAR_TEXTURE))
		{
			return;
		}
		final long interval = Math.round(ForeverFixesConfig.value(ForeverFixesConfig.STAR_TEXTURE_INTERVAL, 20D));
		final long elapsed = level.getGameTime() - lastPlasmaUpdateTick;
		// elapsed < 0: game time went back (another world); rebuild
		if (interval == 0L || elapsed >= 0L && elapsed < interval)
		{
			ci.cancel();
		}
	}
}
