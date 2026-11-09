package com.diamondskybox.foreverfixes.mixin.chromewaremobends;

import com.chromeware.client.body.LimbRenderContext;
import com.diamondskybox.foreverfixes.GeometryCapture;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Chromeware's per-part draw rules (cutouts, hidden cyber limbs) don't apply while Mo' Bends measures a part; see {@link GeometryCapture}. */
@Mixin(value = LimbRenderContext.class, remap = false)
public abstract class MixinLimbRenderContextNeutralCapture
{
	@Inject(method = "ruleFor", remap = false, require = 1, at = @At("HEAD"), cancellable = true)
	private static void foreverfixes$noRulesWhileMeasuring(ModelPart part, CallbackInfoReturnable<LimbRenderContext.Rule> cir)
	{
		if (GeometryCapture.active())
		{
			cir.setReturnValue(null);
		}
	}
}
