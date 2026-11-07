package com.diamondskybox.foreverfixes.mixin.chromewaremobends;

import com.diamondskybox.foreverfixes.CyberArmorFit;
import com.diamondskybox.foreverfixes.CyberLimbBends;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.standard.client.model.armor.ArmorRenderContext;
import goblinbob.mobends.standard.client.model.armor.CapturingVertexConsumer;
import goblinbob.mobends.standard.client.model.armor.QuadSlicer;
import goblinbob.mobends.standard.client.model.armor.tier1.Tier1Renderer;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mo' Bends draws armour itself (it cancels the vanilla armour layer) and renders each piece's body / arms / legs separately, so the
 * vanilla part visibility the cyber armour-fit sets never reaches it: the pieces over replaced limbs are skipped here instead.
 */
@Mixin(value = Tier1Renderer.class, remap = false)
public abstract class MixinTier1ArmorCyberFit
{
	@Unique
	private static final ThreadLocal<Object[]> foreverfixes$fit = new ThreadLocal<>();

	@Inject(method = "renderInternal", remap = false, require = 1, at = @At("HEAD"))
	private void foreverfixes$begin(ArmorRenderContext<?> context, HumanoidModel<?> model, VertexConsumer consumer, CallbackInfo ci)
	{
		final boolean[] hide = CyberArmorFit.hiddenParts(CyberLimbBends.drawing(), context.getSlot());
		foreverfixes$fit.set(new Object[]{model, hide});
	}

	@Inject(method = "renderInternal", remap = false, require = 1, at = @At("RETURN"))
	private void foreverfixes$end(ArmorRenderContext<?> context, HumanoidModel<?> model, VertexConsumer consumer, CallbackInfo ci)
	{
		foreverfixes$fit.remove();
	}

	@Unique
	private static boolean foreverfixes$hidden(ModelPart part)
	{
		final Object[] fit = foreverfixes$fit.get();
		if (fit == null)
		{
			return false;
		}
		final HumanoidModel<?> model = (HumanoidModel<?>) fit[0];
		final boolean[] hide = (boolean[]) fit[1];
		return hide[0] && part == model.body || hide[1] && part == model.leftArm || hide[2] && part == model.rightArm
				|| hide[3] && part == model.leftLeg || hide[4] && part == model.rightLeg;
	}

	@WrapOperation(method = "*", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lgoblinbob/mobends/standard/client/model/armor/ArmorPoseHelper;renderSplitArm(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/model/geom/ModelPart;Lgoblinbob/mobends/standard/data/BipedEntityData;ZIIFILgoblinbob/mobends/standard/client/model/armor/CapturingVertexConsumer;Lgoblinbob/mobends/standard/client/model/armor/QuadSlicer;)V"))
	private void foreverfixes$arm(PoseStack pose, VertexConsumer consumer, ModelPart part, BipedEntityData<?> data, boolean b, int i1, int i2, float f, int i3,
			CapturingVertexConsumer capture, QuadSlicer slicer, Operation<Void> original)
	{
		if (!foreverfixes$hidden(part))
		{
			original.call(pose, consumer, part, data, b, i1, i2, f, i3, capture, slicer);
		}
	}

	@WrapOperation(method = "*", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lgoblinbob/mobends/standard/client/model/armor/ArmorPoseHelper;renderSplitLeg(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/model/geom/ModelPart;Lgoblinbob/mobends/standard/data/BipedEntityData;ZIIILgoblinbob/mobends/standard/client/model/armor/CapturingVertexConsumer;Lgoblinbob/mobends/standard/client/model/armor/QuadSlicer;)V"))
	private void foreverfixes$leg(PoseStack pose, VertexConsumer consumer, ModelPart part, BipedEntityData<?> data, boolean b, int i1, int i2, int i3,
			CapturingVertexConsumer capture, QuadSlicer slicer, Operation<Void> original)
	{
		if (!foreverfixes$hidden(part))
		{
			original.call(pose, consumer, part, data, b, i1, i2, i3, capture, slicer);
		}
	}

	@WrapOperation(method = "*", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lgoblinbob/mobends/standard/client/model/armor/tier1/Tier1Renderer;renderCapturedPart(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/model/geom/ModelPart;Lgoblinbob/mobends/standard/data/BipedEntityData;ZII)V"))
	private void foreverfixes$captured(Tier1Renderer self, PoseStack pose, VertexConsumer consumer, ModelPart part, BipedEntityData<?> data, boolean b, int i1, int i2,
			Operation<Void> original)
	{
		if (!foreverfixes$hidden(part))
		{
			original.call(self, pose, consumer, part, data, b, i1, i2);
		}
	}

	@WrapOperation(method = "*", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lgoblinbob/mobends/standard/client/model/armor/tier1/Tier1Renderer;renderBodyWithPivotRotation(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/model/geom/ModelPart;Lgoblinbob/mobends/standard/data/BipedEntityData;II)V"))
	private void foreverfixes$body(Tier1Renderer self, PoseStack pose, VertexConsumer consumer, ModelPart part, BipedEntityData<?> data, int i1, int i2,
			Operation<Void> original)
	{
		if (!foreverfixes$hidden(part))
		{
			original.call(self, pose, consumer, part, data, i1, i2);
		}
	}
}
