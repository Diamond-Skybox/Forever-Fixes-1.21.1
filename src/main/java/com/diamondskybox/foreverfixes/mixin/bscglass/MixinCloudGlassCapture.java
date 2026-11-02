package com.diamondskybox.foreverfixes.mixin.bscglass;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import com.diamondskybox.foreverfixes.StormFogFixes;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Better Simple Clouds copies the depth buffer at the after-block-entities stage to tell glass from what is behind it, so
 * clouds and storm fog can show through windows. Entities, block entities (chests) and beacon beams are batched and may still
 * be waiting in the buffer source at that point (ImmediatelyFast batches them further), so they were missing from the copy and
 * the clouds were drawn over them wherever glass was behind them. The batched geometry is drawn before the copy, and the
 * beacon beams' glow (no depth of its own) is drawn into the copy after it. Under Fast/Fancy graphics its glass depth, the
 * finished depth, is replaced by the snapshot after the translucent blocks (see {@link StormFogFixes}).
 */
@Mixin(targets = "dev.bettersimpleclouds.immersion.CloudGlass", remap = false)
public abstract class MixinCloudGlassCapture
{
	@Shadow(remap = false)
	private static RenderTarget opaque;

	@Inject(method = "capture", remap = false, require = 1, at = @At("HEAD"))
	private static void foreverfixes$flushBatched(RenderTarget main, CallbackInfo ci)
	{
		if (ForeverFixesConfig.enabled(ForeverFixesConfig.BSC_GLASS_FLUSH))
		{
			Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
		}
	}

	@Inject(method = "capture", remap = false, require = 1, at = @At("TAIL"))
	private static void foreverfixes$afterCapture(RenderTarget main, CallbackInfo ci)
	{
		StormFogFixes.afterCapture(opaque);
	}

	@Inject(method = "glassDepthTexture", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private static void foreverfixes$glassBeforeParticles(RenderTarget main, CallbackInfoReturnable<Integer> cir)
	{
		final int depth = StormFogFixes.glassDepthTexture();
		if (depth > 0)
		{
			cir.setReturnValue(depth);
		}
	}
}
