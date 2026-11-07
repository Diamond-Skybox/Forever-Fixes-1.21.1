package com.diamondskybox.foreverfixes.mixin.chromeware;

import com.diamondskybox.foreverfixes.CyberArmorFit;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Armour isn't drawn over limbs Chromeware replaced: the left sleeve goes with a cyber left arm, a pant leg and boot with a cyber leg. */
@Mixin(HumanoidArmorLayer.class)
public abstract class MixinHumanoidArmorLayerCyberFit
{
	@WrapOperation(method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V",
			require = 1, at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;setPartVisibility(Lnet/minecraft/client/model/HumanoidModel;Lnet/minecraft/world/entity/EquipmentSlot;)V"))
	private void foreverfixes$cyberFit(HumanoidArmorLayer<?, ?, ?> self, HumanoidModel<?> model, EquipmentSlot slot, Operation<Void> original,
			@Local(argsOnly = true) LivingEntity entity)
	{
		original.call(self, model, slot);
		final boolean[] hide = CyberArmorFit.hiddenParts(entity, slot);
		if (hide[0])
		{
			model.body.visible = false;
		}
		if (hide[1])
		{
			model.leftArm.visible = false;
		}
		if (hide[2])
		{
			model.rightArm.visible = false;
		}
		if (hide[3])
		{
			model.leftLeg.visible = false;
		}
		if (hide[4])
		{
			model.rightLeg.visible = false;
		}
	}
}
