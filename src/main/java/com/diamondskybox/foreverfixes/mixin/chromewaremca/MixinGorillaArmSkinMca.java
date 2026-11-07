package com.diamondskybox.foreverfixes.mixin.chromewaremca;

import com.diamondskybox.foreverfixes.McaArms;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Gorilla Arms' flesh material (the player skin) wears the MCA skin and clothing when the player has an MCA body. */
@Mixin(targets = "com.chromeware.client.cyberware.GorillaArmRenderer", remap = false)
public abstract class MixinGorillaArmSkinMca
{
	@WrapOperation(method = "lambda$drawArm$3", remap = false, require = 1,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
	private static VertexConsumer foreverfixes$mcaSkin(MultiBufferSource buffers, RenderType type, Operation<VertexConsumer> original,
			@Local(argsOnly = true) AbstractClientPlayer player)
	{
		if (type == RenderType.entityCutoutNoCull(player.getSkin().texture()) && McaArms.usesMcaLook(player))
		{
			final VertexConsumer mca = McaArms.layeredSkin(player, buffers);
			if (mca != null)
			{
				return mca;
			}
		}
		return original.call(buffers, type);
	}

	@Inject(method = "drawArm", remap = false, require = 1, at = @At("RETURN"))
	private static void foreverfixes$flushMcaSkin(CallbackInfo ci)
	{
		McaArms.flushLayered();
	}
}
