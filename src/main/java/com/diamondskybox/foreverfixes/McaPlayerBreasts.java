package com.diamondskybox.foreverfixes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.compat.McaCompat;
import goblinbob.mobends.core.client.model.BendsModelPart;
import net.minecraft.client.model.HumanoidModel;

/**
 * MCA gives players its villager body (PlayerEntityExtendedModel, with breast parts sized from the player's MCA data). Mo' Bends
 * replaces that model's render with its own bent model and only adds the breasts back for its villager mutators
 * (McaMutatorBase); the player mutator never does, so with Mo' Bends on, an MCA-bodied player has none. This draws them the way
 * Mo' Bends' villager path does: Mo' Bends' own McaCompat.renderBreasts, from the bent torso's transform. Called after the
 * main model render (skin texture) and after each overlay model (clothing textures). Loaded only through the mobends mixins.
 */
public final class McaPlayerBreasts
{
	private McaPlayerBreasts()
	{
	}

	public static void render(BendsModelPart body, HumanoidModel<?> model, PoseStack poseStack, VertexConsumer vertexConsumer,
			int packedLight, int packedOverlay, int color)
	{
		if (!ForeverFixesConfig.enabled(ForeverFixesConfig.MOBENDS_MCA_PLAYER_BREASTS)
				|| model == null || body == null || !body.isShowing() || !McaCompat.isMcaModel(model))
		{
			return;
		}
		poseStack.pushPose();
		try
		{
			body.applyCharacterTransformPoseStack(poseStack);
			poseStack.translate(-body.position.x / 16.0F, -body.position.y / 16.0F, -body.position.z / 16.0F);
			McaCompat.renderBreasts(model, poseStack, vertexConsumer, packedLight, packedOverlay, color);
		}
		finally
		{
			poseStack.popPose();
		}
	}
}
