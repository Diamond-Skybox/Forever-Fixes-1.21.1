package com.diamondskybox.foreverfixes.mixin.chromewaregender;

import com.diamondskybox.foreverfixes.ChromeBreasts;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mcabp.wildfire.client.render.WildfireModelRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Breasts on a bare Chromeware torso use the cyber body's pectoral plate texture in the player's Shell colour; no skin jacket over it. */
@Mixin(targets = "com.mcabp.wildfire.client.render.GenderLayer", remap = false)
public abstract class MixinGenderLayerChromeTorso
{
	@Shadow
	private WildfireModelRenderer.BreastModelBox lBreast;
	@Shadow
	private WildfireModelRenderer.BreastModelBox rBreast;
	@Shadow
	private WildfireModelRenderer.BreastModelBox lBoobArmor;
	@Shadow
	private WildfireModelRenderer.BreastModelBox rBoobArmor;

	@Unique
	private static final ThreadLocal<Boolean> foreverfixes$chrome = ThreadLocal.withInitial(() -> false);

	@Inject(method = "renderBreast", remap = false, require = 1, at = @At("HEAD"))
	private void foreverfixes$begin(LivingEntity entity, ItemStack armor, PoseStack stack, MultiBufferSource buffers, RenderType type, int light,
			int overlay, float alpha, boolean left, boolean jacket, CallbackInfo ci)
	{
		foreverfixes$chrome.set(type != null && ChromeBreasts.chromeTorso(entity));
	}

	@Inject(method = "renderBreast", remap = false, require = 1, at = @At("RETURN"))
	private void foreverfixes$end(CallbackInfo ci)
	{
		foreverfixes$chrome.set(false);
	}

	@WrapOperation(method = "renderBreast", remap = false, require = 1,
			at = @At(value = "INVOKE", ordinal = 0, remap = false,
					target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
	private VertexConsumer foreverfixes$chromeBuffer(MultiBufferSource buffers, RenderType type, Operation<VertexConsumer> original,
			@Local(argsOnly = true) LivingEntity entity)
	{
		return foreverfixes$chrome.get() && entity instanceof Player player ? ChromeBreasts.buffer(player, buffers) : original.call(buffers, type);
	}

	@WrapOperation(method = "renderBreast", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lcom/mcabp/wildfire/client/render/GenderLayer;renderBox(Lcom/mcabp/wildfire/client/render/WildfireModelRenderer$ModelBox;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"))
	private void foreverfixes$chromeBox(WildfireModelRenderer.ModelBox model, PoseStack stack, VertexConsumer buffer, int light, int overlay, int color,
			Operation<Void> original, @Local(argsOnly = true) LivingEntity entity)
	{
		if ((model == lBoobArmor || model == rBoobArmor)
				&& com.diamondskybox.foreverfixes.CyberArmorFit.hiddenParts(entity, net.minecraft.world.entity.EquipmentSlot.CHEST)[0])
		{
			return; // the chestplate isn't worn over a cyber torso, so neither is its chest piece
		}
		if (!foreverfixes$chrome.get())
		{
			original.call(model, stack, buffer, light, overlay, color);
		}
		else if (model == lBreast || model == rBreast)
		{
			ChromeBreasts.render(model, stack, buffer, light, overlay, color);
		}
		else if (!(model instanceof WildfireModelRenderer.OverlayModelBox))
		{
			original.call(model, stack, buffer, light, overlay, color); // armor over the chrome breasts stays as it is
		}
		// the skin's jacket overlay is not drawn over chrome
	}

	@WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
			remap = false, require = 1, at = {
					@At(value = "INVOKE", remap = false, target = "Lcom/mcabp/wildfire/physics/BreastPhysics;getPrePositionY()F"),
					@At(value = "INVOKE", remap = false, target = "Lcom/mcabp/wildfire/physics/BreastPhysics;getPositionY()F"),
					@At(value = "INVOKE", remap = false, target = "Lcom/mcabp/wildfire/physics/BreastPhysics;getPrePositionX()F"),
					@At(value = "INVOKE", remap = false, target = "Lcom/mcabp/wildfire/physics/BreastPhysics;getPositionX()F"),
					@At(value = "INVOKE", remap = false, target = "Lcom/mcabp/wildfire/physics/BreastPhysics;getPreBounceRotation()F"),
					@At(value = "INVOKE", remap = false, target = "Lcom/mcabp/wildfire/physics/BreastPhysics;getBounceRotation()F"),
					@At(value = "INVOKE", remap = false, target = "Lcom/mcabp/wildfire/physics/BreastPhysics;getPreBounceRoll()F"),
					@At(value = "INVOKE", remap = false, target = "Lcom/mcabp/wildfire/physics/BreastPhysics;getBounceRoll()F")})
	private float foreverfixes$rigidTorso(com.mcabp.wildfire.physics.BreastPhysics physics, Operation<Float> original, @Local(argsOnly = true) LivingEntity entity)
	{
		return ChromeBreasts.rigidTorso(entity) ? 0.0F : original.call(physics);
	}
}
