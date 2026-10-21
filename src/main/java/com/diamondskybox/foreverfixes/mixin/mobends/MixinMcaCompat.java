package com.diamondskybox.foreverfixes.mixin.mobends;

import com.diamondskybox.foreverfixes.McaHairDiagnostics;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Logs which way Mo' Bends sends each MCA model it intercepts (see {@link McaHairDiagnostics}). */
@Mixin(targets = "goblinbob.mobends.compat.McaCompat", remap = false)
public abstract class MixinMcaCompat
{
	@Inject(method = "renderMutatedInstead", at = @At("HEAD"), remap = false, require = 1)
	private static void foreverfixes$before(Object model, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
			int packedOverlay, int color, CallbackInfoReturnable<Boolean> cir)
	{
		McaHairDiagnostics.intercepting(MoBendsRenderContext.isInMainModelRender());
	}

	@Inject(method = "renderMutatedInstead", at = @At("RETURN"), remap = false, require = 1)
	private static void foreverfixes$after(Object model, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
			int packedOverlay, int color, CallbackInfoReturnable<Boolean> cir)
	{
		McaHairDiagnostics.intercepted(model, cir.getReturnValueZ());
	}
}
