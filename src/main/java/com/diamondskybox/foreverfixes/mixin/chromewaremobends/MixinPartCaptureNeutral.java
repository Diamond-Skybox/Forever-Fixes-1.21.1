package com.diamondskybox.foreverfixes.mixin.chromewaremobends;

import com.diamondskybox.foreverfixes.GeometryCapture;
import goblinbob.mobends.standard.client.model.adaptive.PartCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mo' Bends measures a model part by rendering it into a capture buffer, once per model, and keeps the result for the session
 * (elbow and knee pivots, the upper / lower split meshes). The measurement is marked so Chromeware's limb rules stand aside
 * for it; see {@link GeometryCapture}. Without this, a player model first measured inside a Chromeware render scope (the
 * first-person blade pass is the usual case: blades deployed in first person before any third-person look) got a 6 px "arm"
 * from the Mantis forearm cutout and an empty cyber limb, so the forearm pivot sat at or just under the shoulder: the forearm
 * drew over the bicep, the hand (and anything held) ended at the elbow, and a relog didn't help because the geometry cache is static.
 */
@Mixin(value = PartCapture.class, remap = false)
public abstract class MixinPartCaptureNeutral
{
	@Inject(method = "capture", remap = false, require = 1, at = @At("HEAD"))
	private static void foreverfixes$beginCapture(CallbackInfoReturnable<PartCapture> cir)
	{
		GeometryCapture.begin();
	}

	@Inject(method = "capture", remap = false, require = 1, at = @At("RETURN"))
	private static void foreverfixes$endCapture(CallbackInfoReturnable<PartCapture> cir)
	{
		GeometryCapture.end();
	}
}
