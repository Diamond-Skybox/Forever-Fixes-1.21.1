package com.diamondskybox.foreverfixes.mixin.bscglass;

import com.diamondskybox.foreverfixes.StormFogFixes;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Simple Clouds stretches its quarter-resolution storm fog into the blur target with this blit, from every render pipeline
 * (Better Simple Clouds' included); the storm fog is stretched by depth instead (see {@link StormFogFixes}).
 */
@Mixin(targets = "dev.nonamecrackers2.simpleclouds.client.framebuffer.FrameBufferUtils", remap = false)
public abstract class MixinFrameBufferUtilsUpsample
{
	@Inject(method = "blitTargetPreservingAlpha", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private static void foreverfixes$depthAwareStormFog(RenderTarget target, int width, int height, CallbackInfo ci)
	{
		if (StormFogFixes.upsampleStormFog(target))
		{
			ci.cancel();
		}
	}
}
