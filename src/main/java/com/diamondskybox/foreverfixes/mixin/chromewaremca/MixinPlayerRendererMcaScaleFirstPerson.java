package com.diamondskybox.foreverfixes.mixin.chromewaremca;

import com.chromeware.client.cyberware.ArmFirstPerson;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;

/**
 * MCA resizes the player body for its genetics (PlayerRenderer.scale), after Chromeware has placed the camera at the eyes of a
 * vanilla-sized body for its first-person implant view; the resize pivots at the feet and moved the arms and implants away from the
 * camera (too low). In that pass only, the body keeps the vanilla size.
 */
@Mixin(PlayerRenderer.class)
public abstract class MixinPlayerRendererMcaScaleFirstPerson
{
	@WrapMethod(method = "scale(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;F)V")
	private void foreverfixes$vanillaSizeInImplantView(AbstractClientPlayer player, PoseStack stack, float partialTick, Operation<Void> original)
	{
		if (!ArmFirstPerson.applies(player))
		{
			original.call(player, stack, partialTick);
			return;
		}
		final Matrix4f pose = new Matrix4f(stack.last().pose());
		final Matrix3f normal = new Matrix3f(stack.last().normal());
		original.call(player, stack, partialTick);
		stack.last().pose().set(pose);
		stack.last().normal().set(normal);
		stack.scale(0.9375F, 0.9375F, 0.9375F);
	}
}
