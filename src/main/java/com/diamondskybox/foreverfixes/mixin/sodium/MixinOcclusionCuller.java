package com.diamondskybox.foreverfixes.mixin.sodium;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Sodium's occlusion culler only considers a chunk section when it lies within the render distance horizontally AND
 * {@code |dy| < render distance} vertically (OcclusionCuller.isWithinRenderDistance). Vanilla has no vertical limit. In a
 * 2096-block-tall world with an 8-chunk render distance that culls every section more than ~128 blocks above or below the
 * camera: ground vanishes under you when you fly high, flips as you cross section boundaries, and valleys disappear from
 * peaks. This makes the vertical test always pass (the horizontal one is untouched), so Sodium renders the same column of
 * sections vanilla would. Config: render.noVerticalSectionCull.
 */
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.occlusion.OcclusionCuller", remap = false)
public abstract class MixinOcclusionCuller
{
	@Redirect(method = "isWithinRenderDistance", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Ljava/lang/Math;abs(F)F"))
	private static float foreverfixes$verticalDistance(float dy)
	{
		return ForeverFixesConfig.enabled(ForeverFixesConfig.NO_VERTICAL_SECTION_CULL) ? 0.0F : Math.abs(dy);
	}
}
