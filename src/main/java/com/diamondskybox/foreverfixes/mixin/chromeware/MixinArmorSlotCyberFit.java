package com.diamondskybox.foreverfixes.mixin.chromeware;

import com.diamondskybox.foreverfixes.CyberArmorFit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The armour slot refuses a piece the player's cyber limbs leave no room for. */
@Mixin(targets = "net.minecraft.world.inventory.ArmorSlot")
public abstract class MixinArmorSlotCyberFit
{
	@Shadow
	@Final
	private LivingEntity owner;
	@Shadow
	@Final
	private EquipmentSlot slot;

	@Inject(method = "mayPlace", require = 1, at = @At("RETURN"), cancellable = true)
	private void foreverfixes$cyberFit(ItemStack stack, CallbackInfoReturnable<Boolean> cir)
	{
		if (cir.getReturnValueZ() && CyberArmorFit.blocked(owner, slot, stack))
		{
			cir.setReturnValue(false);
		}
	}
}
