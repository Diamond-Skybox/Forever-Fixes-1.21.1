package com.diamondskybox.foreverfixes.mixin.chromewaremobends;

import com.chromeware.body.BodyPart;
import com.chromeware.client.body.CyberLimbRenderer;
import com.diamondskybox.foreverfixes.CyberLimbBends;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Chromeware's cyber limbs bend at Mo' Bends' elbows and knees. */
@Mixin(value = CyberLimbRenderer.class, remap = false)
public abstract class MixinCyberLimbRendererBends
{
	@ModifyVariable(method = "draw", remap = false, require = 1, argsOnly = true, at = @At("HEAD"))
	private static CyberLimbRenderer.Fitting foreverfixes$bend(CyberLimbRenderer.Fitting fitting, @Local(argsOnly = true) BodyPart part,
			@Local(argsOnly = true, ordinal = 0) float center, @Local(argsOnly = true) boolean slim)
	{
		return CyberLimbBends.bend(part, center, slim, fitting);
	}
}
