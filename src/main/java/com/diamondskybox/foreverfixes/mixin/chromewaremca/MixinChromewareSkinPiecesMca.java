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

/**
 * Chromeware's Mantis Blades, Projectile Launcher and Extendo Grip draw skin copies of the forearm with the player's Minecraft skin; with
 * an MCA body they wear the MCA skin (tinted) and clothing instead, first and third person.
 */
@Mixin(targets = {"com.chromeware.client.mantis.MantisRenderer", "com.chromeware.client.launcher.LauncherRenderer",
		"com.chromeware.client.extendo.ExtendoRenderer"}, remap = false)
public abstract class MixinChromewareSkinPiecesMca
{
	@WrapOperation(method = "drawArm", remap = false, require = 1,
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
