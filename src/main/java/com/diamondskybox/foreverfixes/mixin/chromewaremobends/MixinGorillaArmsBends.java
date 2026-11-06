package com.diamondskybox.foreverfixes.mixin.chromewaremobends;

import com.chromeware.client.cyberware.GorillaGeometry;
import com.diamondskybox.foreverfixes.CyberLimbBends;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Function;

/** Gorilla Arms bend at Mo' Bends' elbow: the forearm, hand and frame sections follow the lower arm. */
@Mixin(value = GorillaGeometry.class, remap = false)
public abstract class MixinGorillaArmsBends
{
	@WrapOperation(method = {"renderArm", "renderFrameHardware"}, remap = false, require = 2,
			at = @At(value = "INVOKE", remap = false,
					target = "Lcom/chromeware/client/cyberware/GorillaGeometry;renderPart(ZZLjava/lang/String;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Ljava/util/function/Function;ZII)V"))
	private void foreverfixes$bend(GorillaGeometry self, boolean left, boolean slim, String part, PoseStack.Pose pose,
			Function<String, VertexConsumer> buffers, boolean sleeve, int light, int overlay, Operation<Void> original,
			@Local(argsOnly = true) float center)
	{
		final Matrix4f elbow = part.equals("upper") ? null : CyberLimbBends.elbow(left, center);
		if (elbow == null)
		{
			original.call(self, left, slim, part, pose, buffers, sleeve, light, overlay);
			return;
		}
		final PoseStack bent = new PoseStack();
		bent.last().pose().set(pose.pose()).mul(elbow);
		bent.last().normal().set(pose.normal()).mul(new Matrix4f(elbow).normalize3x3(new Matrix3f()));
		original.call(self, left, slim, part, bent.last(), buffers, sleeve, light, overlay);
	}
}
