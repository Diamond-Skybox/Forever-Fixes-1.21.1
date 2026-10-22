package com.diamondskybox.foreverfixes.mixin.betterinventory;

import com.diamondskybox.foreverfixes.invbridge.InventoryButtons;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Better Inventory's compat cells (beside the character, up to four) hold the inventory buttons of the mods it knows (Curios,
 * Cosmetic Armor, Aether, Accessories). Other mods add their inventory button to the vanilla inventory screen only, which Better
 * Inventory replaces, so those buttons were gone; they are added to the cells after Better Inventory's own (see
 * {@link InventoryButtons}).
 */
@Mixin(targets = "dev.fallingcloud.betterinventory.client.CompatButtons", remap = false)
public abstract class MixinCompatButtons
{
	@Inject(method = "entries", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Ljava/util/List;subList(II)Ljava/util/List;"))
	private static void foreverfixes$modButtons(CallbackInfoReturnable<List<?>> cir, @Local List<Object> list)
	{
		InventoryButtons.addTo(list);
	}
}
