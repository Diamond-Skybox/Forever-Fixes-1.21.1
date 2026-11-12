package com.diamondskybox.foreverfixes.mixin.simplecloudsdh;

import com.diamondskybox.foreverfixes.DhRenderWatch;
import dev.nonamecrackers2.simpleclouds.client.event.impl.DetermineCloudRenderPipelineEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Simple Clouds: its Distant Horizons pipeline only while Distant Horizons is drawing. It picked that pipeline every frame whenever
 * Distant Horizons was installed, and that pipeline renders the clouds, storm fog and their composite from Distant Horizons'
 * render events; with Distant Horizons' rendering turned off (or in a dimension it does not draw) those events stop, and the
 * composite of the last frame drawn with Distant Horizons stayed on screen as a haze with the old entities and skyline cut out
 * of it. Simple Clouds' own pipeline is used then (DhRenderWatch: rendering on and an opaque pass within a quarter second).
 */
@Mixin(targets = "dev.nonamecrackers2.simpleclouds.client.dh.event.SimpleCloudsDhForgeEvents", remap = false)
public abstract class MixinDhPipelineOnlyWhileDhRenders
{
	@Inject(method = "determineRenderPipeline", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private static void foreverfixes$onlyWhileDhRenders(DetermineCloudRenderPipelineEvent event, CallbackInfo ci)
	{
		if (!DhRenderWatch.rendering())
		{
			ci.cancel();
		}
	}
}
