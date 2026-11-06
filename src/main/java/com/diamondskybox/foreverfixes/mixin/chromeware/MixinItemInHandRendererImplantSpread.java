package com.diamondskybox.foreverfixes.mixin.chromeware;

import com.chromeware.body.CyberLimbs;
import com.chromeware.cyberware.ArmImplants;
import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** First-person arms carrying Chromeware hardware sit further apart (chromeware.firstPersonArmSpread). */
@Mixin(ItemInHandRenderer.class)
public abstract class MixinItemInHandRendererImplantSpread
{
	@Inject(method = "renderPlayerArm", require = 1, at = @At("HEAD"))
	private void foreverfixes$spread(PoseStack stack, MultiBufferSource buffers, int light, float equip, float swing, HumanoidArm side, CallbackInfo ci)
	{
		final double spread = ForeverFixesConfig.SPEC.isLoaded() ? ForeverFixesConfig.CHROMEWARE_FIRST_PERSON_SPREAD.get() : 0.18D;
		final LocalPlayer player = Minecraft.getInstance().player;
		if (spread <= 0.0D || player == null)
		{
			return;
		}
		boolean hardware;
		try
		{
			hardware = !ArmImplants.get(player, side).item().isEmpty() || CyberLimbs.has(player, CyberLimbs.part(side));
		}
		catch (RuntimeException e)
		{
			hardware = false;
		}
		if (hardware)
		{
			stack.translate((side == HumanoidArm.RIGHT ? 1.0D : -1.0D) * spread, 0.0D, 0.0D);
		}
	}
}
