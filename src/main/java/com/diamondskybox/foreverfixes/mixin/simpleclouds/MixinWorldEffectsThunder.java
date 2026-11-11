package com.diamondskybox.foreverfixes.mixin.simpleclouds;

import com.diamondskybox.foreverfixes.CloudThunder;
import dev.nonamecrackers2.simpleclouds.client.renderer.WorldEffects;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Simple Clouds: the client thunder level follows the cloud at the camera, as the rain level does (see {@link CloudThunder}). */
@Mixin(value = WorldEffects.class, remap = false)
public abstract class MixinWorldEffectsThunder
{
	@Inject(method = "renderPost", remap = false, require = 1, at = @At("TAIL"))
	private void foreverfixes$thunderFromClouds(Matrix4f camMat, float partialTick, double camX, double camY, double camZ, float scale,
			CallbackInfo ci)
	{
		CloudThunder.afterRenderPost((WorldEffects) (Object) this, camX, camY, camZ);
	}
}
