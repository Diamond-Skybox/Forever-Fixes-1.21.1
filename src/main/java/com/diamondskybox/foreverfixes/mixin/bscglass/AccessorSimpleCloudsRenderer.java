package com.diamondskybox.foreverfixes.mixin.bscglass;

import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Simple Clouds' storm fog pass, for the depth and projection it was given (see {@link com.diamondskybox.foreverfixes.StormFogFixes}). */
@Mixin(targets = "dev.nonamecrackers2.simpleclouds.client.renderer.SimpleCloudsRenderer", remap = false)
public interface AccessorSimpleCloudsRenderer
{
	@Accessor(value = "stormPostProcessing", remap = false)
	PostChain foreverfixes$stormPostProcessing();
}
