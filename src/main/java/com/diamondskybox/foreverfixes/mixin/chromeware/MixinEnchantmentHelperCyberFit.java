package com.diamondskybox.foreverfixes.mixin.chromeware;

import com.diamondskybox.foreverfixes.CyberArmorFit;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Enchantments on a chestplate / leggings / boots partly displaced by cyber limbs act at a proportionally lower level. */
@Mixin(EnchantmentHelper.class)
public abstract class MixinEnchantmentHelperCyberFit
{
	@WrapOperation(method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentInSlotVisitor;)V",
			require = 1, at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentInSlotVisitor;accept(Lnet/minecraft/core/Holder;ILnet/minecraft/world/item/enchantment/EnchantedItemInUse;)V"))
	private static void foreverfixes$cyberFit(EnchantmentHelper.EnchantmentInSlotVisitor visitor, Holder<Enchantment> enchantment, int level, EnchantedItemInUse item,
			Operation<Void> original, @Local(argsOnly = true) EquipmentSlot slot, @Local(argsOnly = true) LivingEntity entity)
	{
		final int scaled = CyberArmorFit.level(entity, slot, level);
		if (scaled > 0)
		{
			original.call(visitor, enchantment, scaled, item);
		}
	}
}
