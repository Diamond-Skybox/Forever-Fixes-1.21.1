package com.diamondskybox.foreverfixes.mixin.schoarfrost;

import com.diamondskybox.foreverfixes.FlakesAfterStormFog;
import dev.nonamecrackers2.simpleclouds.client.renderer.SimpleCloudsRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Simple Clouds: Hoarfrost's held flakes are drawn once the after-level pass, storm fog included, is done (see {@link FlakesAfterStormFog}). */
@Mixin(value = SimpleCloudsRenderer.class, remap = false)
public abstract class MixinSimpleCloudsRendererFlakesAfter
{
	@Inject(method = "renderAfterLevel", remap = false, require = 1, at = @At("TAIL"))
	private void foreverfixes$flakesAfterStormFog(Matrix4f projMat, Matrix4f camMat, float partialTick, double camX, double camY, double camZ, CallbackInfo ci)
	{
		FlakesAfterStormFog.drawHeld();
	}
}
