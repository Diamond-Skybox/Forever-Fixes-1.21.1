package com.diamondskybox.foreverfixes.mixin.mobends;

import com.diamondskybox.foreverfixes.McaHairDiagnostics;
import com.diamondskybox.foreverfixes.McaPlayerBreasts;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.standard.client.model.adaptive.AdaptiveHumanoidGeometry;
import goblinbob.mobends.standard.mutators.McaMutatorBase;
import net.minecraft.client.model.HumanoidModel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Mo' Bends' biped mutator: after the main model render and after each overlay model, draw MCA's breast parts for players
 * (see {@link McaPlayerBreasts}); the villager mutators (McaMutatorBase) already do this themselves and are left alone.
 * Also feeds {@link McaHairDiagnostics} from the overlay path.
 */
@Mixin(targets = "goblinbob.mobends.standard.mutators.BipedMutator", remap = false)
public abstract class MixinBipedMutator
{
	@Shadow
	protected BendsModelPart body;

	@Shadow
	@Final
	private static Map<HumanoidModel<?>, AdaptiveHumanoidGeometry> overlayGeometry;

	@Inject(method = "renderMutated", at = @At("TAIL"), remap = false, require = 1)
	private void foreverfixes$playerBreasts(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			int color, CallbackInfo ci)
	{
		if ((Object) this instanceof McaMutatorBase)
		{
			return;
		}
		McaPlayerBreasts.render(this.body, MoBendsRenderContext.getCurrentVanillaModel(), poseStack, vertexConsumer,
				packedLight, packedOverlay, color);
	}

	@Inject(method = "drawOverlayExtras", at = @At("TAIL"), remap = false, require = 1)
	private void foreverfixes$playerBreastsOverlay(HumanoidModel<?> model, PoseStack poseStack, VertexConsumer vertexConsumer,
			int packedLight, int packedOverlay, int color, CallbackInfo ci)
	{
		if ((Object) this instanceof McaMutatorBase)
		{
			return;
		}
		McaPlayerBreasts.render(this.body, model, poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Inject(method = "renderOverlayGeometry", at = @At("HEAD"), remap = false, require = 1)
	private void foreverfixes$overlayDiagnostics(HumanoidModel<?> model, PoseStack poseStack, VertexConsumer vertexConsumer,
			int packedLight, int packedOverlay, int color, CallbackInfo ci)
	{
		McaHairDiagnostics.overlay(model, overlayGeometry.get(model), model == MoBendsRenderContext.getCurrentVanillaModel());
	}
}
