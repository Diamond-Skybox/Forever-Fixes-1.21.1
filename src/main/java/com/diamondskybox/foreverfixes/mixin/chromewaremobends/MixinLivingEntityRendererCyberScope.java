package com.diamondskybox.foreverfixes.mixin.chromewaremobends;

import com.diamondskybox.foreverfixes.CyberLimbBends;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Deque;

/** Marks a third-person player render (model and all its layers), so cyberware there bends with Mo' Bends; first-person hands do not. */
@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRendererCyberScope
{
	/** Outer render's player per nesting level (an entry holding null when there was none). */
	@Unique
	private static final ThreadLocal<Deque<Player[]>> foreverfixes$outer = ThreadLocal.withInitial(ArrayDeque::new);

	@Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			require = 1, at = @At("HEAD"))
	private void foreverfixes$enter(LivingEntity entity, float yaw, float partialTick, PoseStack stack, MultiBufferSource buffers, int light, CallbackInfo ci)
	{
		foreverfixes$outer.get().push(new Player[]{CyberLimbBends.enterEntity(entity instanceof Player p ? p : null)});
	}

	@Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			require = 1, at = @At("RETURN"))
	private void foreverfixes$leave(LivingEntity entity, float yaw, float partialTick, PoseStack stack, MultiBufferSource buffers, int light, CallbackInfo ci)
	{
		final Deque<Player[]> outer = foreverfixes$outer.get();
		CyberLimbBends.leaveEntity(outer.isEmpty() ? null : outer.pop()[0]);
	}
}
