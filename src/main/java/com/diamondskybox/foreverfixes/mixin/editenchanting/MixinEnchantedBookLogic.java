package com.diamondskybox.foreverfixes.mixin.editenchanting;

import com.diamondskybox.foreverfixes.ForeverFixesCommonConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

/**
 * Edit Enchanting's disenchantment table takes enchantments off an item but leaves its anvil prior-work penalty
 * (minecraft:repair_cost), which doubles with every anvil use: items that had been worked on stayed "Too Expensive" even with no
 * enchantments left. When the table removes enchantments, the penalty is recalculated the way the grindstone does it, as if only
 * the remaining enchantments had been applied one anvil use each (none left: no penalty). It is only ever lowered.
 */
@Mixin(targets = "com.jiggo.editenchanting.common.control.EnchantedBookLogic", remap = false)
public abstract class MixinEnchantedBookLogic
{
	@WrapOperation(method = "updateEnchantingStack", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lcom/jiggo/editenchanting/common/util/EnchantmentUtils;setEnchantedItemStack(Ljava/util/Map;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
	private ItemStack foreverfixes$resetRepairCost(Map<?, ?> enchantments, ItemStack stack, Operation<ItemStack> original)
	{
		final int before = EnchantmentHelper.getEnchantmentsForCrafting(stack).size();
		final ItemStack result = original.call(enchantments, stack);
		final int after = EnchantmentHelper.getEnchantmentsForCrafting(stack).size();
		if (after < before && ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.EDIT_ENCHANTING_RESET_REPAIR_COST))
		{
			int cost = 0;
			for (int i = 0; i < after; i++)
			{
				cost = AnvilMenu.calculateIncreasedRepairCost(cost);
			}
			if (cost < stack.getOrDefault(DataComponents.REPAIR_COST, 0))
			{
				if (cost == 0)
				{
					stack.remove(DataComponents.REPAIR_COST);
				}
				else
				{
					stack.set(DataComponents.REPAIR_COST, cost);
				}
			}
		}
		return result;
	}
}
