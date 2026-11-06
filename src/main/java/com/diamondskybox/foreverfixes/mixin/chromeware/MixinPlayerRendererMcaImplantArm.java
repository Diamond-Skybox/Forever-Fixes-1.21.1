package com.diamondskybox.foreverfixes.mixin.chromeware;

import com.diamondskybox.foreverfixes.McaArms;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * First person with MCA: MCA shows its own arm (its villager-animation model's arm, whose pose is not reset like the vanilla
 * first-person arm), while Chromeware draws implant hardware on the vanilla arm right after renderHand. The hardware then sits
 * apart from the arm you see; the arm handed to Chromeware takes MCA's shown pose.
 */
@Mixin(PlayerRenderer.class)
public abstract class MixinPlayerRendererMcaImplantArm
{
	@Inject(method = "renderHand", require = 1, at = @At("RETURN"))
	private void foreverfixes$followMcaArm(PoseStack stack, MultiBufferSource buffers, int light, AbstractClientPlayer player, ModelPart arm,
			ModelPart sleeve, CallbackInfo ci)
	{
		final boolean right = arm == ((PlayerRenderer) (Object) this).getModel().rightArm;
		if (!McaArms.replaces(player, right))
		{
			return;
		}
		final ModelPart shown = McaArms.shownArm(this, right);
		if (shown != null && shown != arm)
		{
			arm.copyFrom(shown);
			sleeve.copyFrom(shown);
		}
	}
}
