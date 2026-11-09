package com.diamondskybox.foreverfixes.mixin.chromewaredoppel;

import com.diamondskybox.foreverfixes.DoppelChrome;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * Iron's humanoid mob armour layer (an anonymous ItemArmorGeoLayer in HumanoidRenderer). On a Dark Doppelganger that copied cyber
 * limbs, the armour pieces over those limbs aren't drawn, the same as on the player (chromeware.armorFit).
 */
@Mixin(targets = "io.redspace.ironsspellbooks.entity.mobs.HumanoidRenderer$1", remap = false)
public abstract class MixinHumanoidArmorLayerDoppelFit
{
	@Inject(method = "getArmorItemForBone(Lsoftware/bernie/geckolib/cache/object/GeoBone;Lnet/minecraft/world/entity/Mob;)Lnet/minecraft/world/item/ItemStack;",
			remap = false, require = 1, at = @At("RETURN"), cancellable = true)
	private void foreverfixes$hideOverChrome(GeoBone bone, Mob animatable, CallbackInfoReturnable<ItemStack> cir)
	{
		if (cir.getReturnValue() == null)
		{
			return;
		}
		final String name = bone.getName();
		final EquipmentSlot slot;
		final int part;
		switch (name)
		{
			case "armorBipedBody" -> { slot = EquipmentSlot.CHEST; part = 0; }
			case "armorBipedLeftArm" -> { slot = EquipmentSlot.CHEST; part = 1; }
			case "armorBipedRightArm" -> { slot = EquipmentSlot.CHEST; part = 2; }
			case "armorBipedLeftLeg", "armorBipedLeftLeg2" -> { slot = EquipmentSlot.LEGS; part = 3; }
			case "armorBipedRightLeg", "armorBipedRightLeg2" -> { slot = EquipmentSlot.LEGS; part = 4; }
			case "armorBipedLeftFoot", "armorBipedLeftFoot2" -> { slot = EquipmentSlot.FEET; part = 3; }
			case "armorBipedRightFoot", "armorBipedRightFoot2" -> { slot = EquipmentSlot.FEET; part = 4; }
			default -> { return; }
		}
		if (DoppelChrome.hiddenArmor(animatable, slot)[part])
		{
			cir.setReturnValue(null);
		}
	}
}
