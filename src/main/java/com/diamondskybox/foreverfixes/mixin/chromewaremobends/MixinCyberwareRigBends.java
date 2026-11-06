package com.diamondskybox.foreverfixes.mixin.chromewaremobends;

import com.chromeware.client.cyberware.CyberwareRig;
import com.diamondskybox.foreverfixes.CyberLimbBends;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Arm add-ons drawn from bone rigs (Mantis Blades, Projectile Launcher, Monowire, incl. their skin/sleeve copies of the forearm)
 * bend at Mo' Bends' elbow: the first bone at or below the elbow (by pivot, or a root whose geometry all lies below it) gets the
 * elbow transform, and its whole subtree follows.
 */
@Mixin(value = CyberwareRig.class, remap = false)
public abstract class MixinCyberwareRigBends
{
	@Unique
	private static final float foreverfixes$ELBOW = 0.2F;
	@Unique
	private static final ThreadLocal<Matrix4f> foreverfixes$elbow = new ThreadLocal<>();
	@Unique
	private static final ThreadLocal<Boolean> foreverfixes$bent = ThreadLocal.withInitial(() -> false);

	@Inject(method = "render(ZZLnet/minecraft/client/model/geom/ModelPart;FLcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/function/Function;Lcom/mojang/blaze3d/vertex/VertexConsumer;IILjava/util/function/Predicate;F)Ljava/util/Map;",
			remap = false, require = 1, at = @At("HEAD"))
	private void foreverfixes$begin(boolean left, boolean slim, ModelPart arm, float center, PoseStack stack,
			Function<String, CyberwareRig.Transform> animation, VertexConsumer buffer, int light, int overlay, Predicate<String> draw, float along,
			CallbackInfoReturnable<Map<String, Vec3>> cir)
	{
		foreverfixes$elbow.set(CyberLimbBends.rigElbow(left, slim, center, along));
		foreverfixes$bent.set(false);
	}

	@Inject(method = "render(ZZLnet/minecraft/client/model/geom/ModelPart;FLcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/function/Function;Lcom/mojang/blaze3d/vertex/VertexConsumer;IILjava/util/function/Predicate;F)Ljava/util/Map;",
			remap = false, require = 1, at = @At("RETURN"))
	private void foreverfixes$end(CallbackInfoReturnable<Map<String, Vec3>> cir)
	{
		foreverfixes$elbow.remove();
		foreverfixes$bent.set(false);
	}

	@WrapOperation(method = {"render(ZZLnet/minecraft/client/model/geom/ModelPart;FLcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/function/Function;Lcom/mojang/blaze3d/vertex/VertexConsumer;IILjava/util/function/Predicate;F)Ljava/util/Map;",
			"renderBone"}, remap = false, require = 2,
			at = @At(value = "INVOKE", remap = false,
					target = "Lcom/chromeware/client/cyberware/CyberwareRig;renderBone(Lcom/chromeware/client/cyberware/CyberwareRig$Bone;Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/function/Function;Lcom/mojang/blaze3d/vertex/VertexConsumer;IILjava/util/Map;Ljava/util/function/Predicate;)V"))
	private void foreverfixes$bendBone(CyberwareRig self, CyberwareRig.Bone bone, PoseStack stack, Function<String, CyberwareRig.Transform> animation,
			VertexConsumer buffer, int light, int overlay, Map<String, Vec3> result, Predicate<String> draw, Operation<Void> original)
	{
		final Matrix4f elbow = foreverfixes$elbow.get();
		if (elbow == null || foreverfixes$bent.get() || !foreverfixes$belowElbow(bone))
		{
			original.call(self, bone, stack, animation, buffer, light, overlay, result, draw);
			return;
		}
		foreverfixes$bent.set(true);
		stack.pushPose();
		try
		{
			stack.last().pose().mul(elbow);
			stack.last().normal().mul(new Matrix4f(elbow).normalize3x3(new Matrix3f()));
			original.call(self, bone, stack, animation, buffer, light, overlay, result, draw);
		}
		finally
		{
			stack.popPose();
			foreverfixes$bent.set(false);
		}
	}

	@Unique
	private static boolean foreverfixes$belowElbow(CyberwareRig.Bone bone)
	{
		if (bone.pivot().y >= foreverfixes$ELBOW)
		{
			return true;
		}
		if (bone.parent() != null || bone.quads().isEmpty())
		{
			return false;
		}
		for (CyberwareRig.Quad quad : bone.quads())
		{
			for (CyberwareRig.Vertex v : quad.vertices())
			{
				if (bone.pivot().y + v.point().y < foreverfixes$ELBOW)
				{
					return false;
				}
			}
		}
		return true;
	}
}
