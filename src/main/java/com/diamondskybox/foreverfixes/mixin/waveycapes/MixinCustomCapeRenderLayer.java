package com.diamondskybox.foreverfixes.mixin.waveycapes;

import com.diamondskybox.foreverfixes.CapeFlight;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Around Wavey Capes' cape layer: works out, for the player being drawn, how far Mo' Bends has tipped the torso, so that
 * {@link CapeFlight} can lay the cape along the back (creative flight). The descriptor is spelled out so the bridge method
 * Wavey Capes' layer also carries (Entity instead of AbstractClientPlayer) is left alone.
 */
@Mixin(targets = "dev.tr7zw.waveycapes.renderlayers.CustomCapeRenderLayer", remap = false)
public abstract class MixinCustomCapeRenderLayer
{
	@Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V",
			at = @At("HEAD"), remap = false, require = 1)
	private void foreverfixes$begin(PoseStack poseStack, MultiBufferSource buffers, int packedLight, AbstractClientPlayer player,
			float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci)
	{
		CapeFlight.begin(player);
	}

	@Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V",
			at = @At("RETURN"), remap = false, require = 1)
	private void foreverfixes$end(PoseStack poseStack, MultiBufferSource buffers, int packedLight, AbstractClientPlayer player,
			float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci)
	{
		CapeFlight.end();
	}
}
