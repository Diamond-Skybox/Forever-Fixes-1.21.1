package com.diamondskybox.foreverfixes.mixin.bscglass;

import com.diamondskybox.foreverfixes.StormFogFixes;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Better Simple Clouds: remembers where the frame's beacon beams are, for their glow's depth (see {@link StormFogFixes}). */
@Mixin(value = BeaconRenderer.class, remap = false)
public abstract class MixinBeaconRendererGlow
{
	@Inject(method = "renderBeaconBeam(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/resources/ResourceLocation;FFJIIIFF)V",
			remap = false, require = 1, at = @At("HEAD"))
	private static void foreverfixes$recordBeam(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation beamLocation, float partialTick,
			float textureScale, long gameTime, int yOffset, int height, int color, float beamRadius, float glowRadius, CallbackInfo ci)
	{
		StormFogFixes.recordBeam(poseStack, yOffset, height, glowRadius);
	}
}
