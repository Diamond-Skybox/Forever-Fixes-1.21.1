package com.diamondskybox.foreverfixes.mixin.punchy;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import com.diamondskybox.foreverfixes.McaArms;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Punchy draws the first-person arm with its own arm model in the player's Minecraft skin (the arm, then the sleeve), bypassing
 * the vanilla hand rendering MCA hooks, so MCA players saw Minecraft-skin arms. For players whose arms MCA draws, Punchy's posed
 * arm and sleeve are drawn with MCA's skin and clothing textures instead ({@link McaArms}).
 */
@Mixin(targets = "punchy.client.render.PunchyArmRenderer", remap = false)
public abstract class MixinPunchyArmRenderer
{
	@WrapOperation(method = "renderArm", remap = false, require = 1,
			at = @At(value = "INVOKE", ordinal = 0, remap = false, target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
	private static void foreverfixes$mcaArm(ModelPart arm, PoseStack pose, VertexConsumer buffer, int light, int overlay, Operation<Void> original,
			@Local(argsOnly = true) AbstractClientPlayer player, @Local(argsOnly = true) HumanoidArm side, @Local(argsOnly = true) MultiBufferSource buffers)
	{
		if (!ForeverFixesConfig.enabled(ForeverFixesConfig.PUNCHY_MCA_ARMS) || !McaArms.draw(player, side == HumanoidArm.RIGHT, arm, false, pose, buffers, light))
		{
			original.call(arm, pose, buffer, light, overlay);
		}
	}

	@WrapOperation(method = "renderArm", remap = false, require = 1,
			at = @At(value = "INVOKE", ordinal = 1, remap = false, target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
	private static void foreverfixes$mcaSleeve(ModelPart sleeve, PoseStack pose, VertexConsumer buffer, int light, int overlay, Operation<Void> original,
			@Local(argsOnly = true) AbstractClientPlayer player, @Local(argsOnly = true) HumanoidArm side, @Local(argsOnly = true) MultiBufferSource buffers)
	{
		if (!ForeverFixesConfig.enabled(ForeverFixesConfig.PUNCHY_MCA_ARMS) || !McaArms.draw(player, side == HumanoidArm.RIGHT, sleeve, true, pose, buffers, light))
		{
			original.call(sleeve, pose, buffer, light, overlay);
		}
	}
}
