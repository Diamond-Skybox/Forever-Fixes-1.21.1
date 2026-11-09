package com.diamondskybox.foreverfixes.mixin.chromewaremobends;

import com.chromeware.body.BodyPart;
import com.chromeware.client.body.CyberLimbRenderer;
import com.diamondskybox.foreverfixes.CyberLimbBends;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Under Mo' Bends, a cyber arm or leg is drawn cut at Mo' Bends' joint: every mesh quad is split at the seam plane through the
 * forearm / foreleg pivot, the part above stays with the upper bone and the part below turns with the lower bone (see
 * ChromeJoints, mode "cut"). Forever Fixes draws the limb itself from its own copy of the mesh in that case.
 */
@Mixin(value = CyberLimbRenderer.class, remap = false)
public abstract class MixinCyberLimbRendererCut
{
	@Inject(method = "draw(Lcom/chromeware/body/BodyPart;Lnet/minecraft/client/model/geom/ModelPart;FZLcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IILcom/chromeware/client/body/CyberLimbRenderer$Fitting;)V",
			remap = false, require = 1, at = @At("HEAD"), cancellable = true)
	private static void foreverfixes$cut(BodyPart part, ModelPart bone, float center, boolean slim, PoseStack stack, VertexConsumer buffer, int light, int overlay,
			CyberLimbRenderer.Fitting fitting, CallbackInfo ci)
	{
		if (CyberLimbBends.cutDraw(part, bone, center, slim, stack, buffer, light, overlay, fitting))
		{
			ci.cancel();
		}
	}
}
