package com.diamondskybox.foreverfixes.mixin.vanilla;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The lightmap's CPU copy (block light x, sky light y), rebuilt each frame (see {@code WeatherFogLight}). */
@Mixin(value = LightTexture.class, remap = false)
public interface AccessorLightTexturePixels
{
	@Accessor(value = "lightPixels", remap = false)
	NativeImage foreverfixes$lightPixels();
}
