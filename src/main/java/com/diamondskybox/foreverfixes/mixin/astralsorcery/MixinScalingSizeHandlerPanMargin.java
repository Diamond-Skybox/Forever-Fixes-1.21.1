package com.diamondskybox.foreverfixes.mixin.astralsorcery;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Astral Sorcery's tome maps (research tiers, perk tree, lumen overview): the view centre was clamped to the map's width, which is
 * counted as (2w - 1) node widths while nodes sit two node widths apart, so the far edge of the last column or row could never be
 * centred (about three quarters of a tier short). With Resonance the right-most tier reached, the right half of its cloud stayed at
 * the edge of the book and could not be zoomed into. The centre may now go one node step past each edge.
 */
@Mixin(targets = "hellfirepvp.astralsorcery.client.screen.base.ScalingSizeHandler", remap = false)
public abstract class MixinScalingSizeHandlerPanMargin
{
	@Shadow(remap = false)
	public abstract float getScaledWidth();

	@Shadow(remap = false)
	public abstract float getScaledHeight();

	@Shadow(remap = false)
	public abstract float getScaledNodeSize();

	@Shadow(remap = false)
	public abstract float getScaledSpaceBetweenNodes();

	@Inject(method = "clampX(F)F", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private void foreverfixes$clampXWithMargin(float centerX, CallbackInfoReturnable<Float> cir)
	{
		if (ForeverFixesConfig.enabled(ForeverFixesConfig.ASTRAL_TOME_PAN_MARGIN))
		{
			final float step = this.getScaledNodeSize() + this.getScaledSpaceBetweenNodes();
			cir.setReturnValue(Mth.clamp(centerX, -step, this.getScaledWidth() + step));
		}
	}

	@Inject(method = "clampY(F)F", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private void foreverfixes$clampYWithMargin(float centerY, CallbackInfoReturnable<Float> cir)
	{
		if (ForeverFixesConfig.enabled(ForeverFixesConfig.ASTRAL_TOME_PAN_MARGIN))
		{
			final float step = this.getScaledNodeSize() + this.getScaledSpaceBetweenNodes();
			cir.setReturnValue(Mth.clamp(centerY, -step, this.getScaledHeight() + step));
		}
	}
}
