package com.diamondskybox.foreverfixes.mixin.astralenchdesc;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import net.darkhax.enchdesc.common.api.ContextProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Enchantment Descriptions remembers the tooltip's item on the item's own enchantment list (ENCHANTMENTS / STORED_ENCHANTMENTS
 * component) and adds a description under each enchantment line written from that list. Astral Sorcery's ItemStack mixin hands
 * the tooltip a copy of the list instead, with its constellation enchantments added, which it builds every time even when nothing
 * is added; the copy has no item, so no description was shown at all. An unchanged copy is replaced by the original list, and a
 * changed one gets the original's item.
 */
@Mixin(targets = "hellfirepvp.astralsorcery.common.event.helper.EnchantmentModifierHelper", remap = false)
public abstract class MixinEnchantmentModifierHelper
{
	@Inject(method = "addEnchantments(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/enchantment/ItemEnchantments;)Lnet/minecraft/world/item/enchantment/ItemEnchantments;",
			at = @At("RETURN"), cancellable = true, remap = false, require = 1)
	private static void foreverfixes$keepDescriptionContext(ItemStack stack, ItemEnchantments original, CallbackInfoReturnable<ItemEnchantments> cir)
	{
		final ItemEnchantments copy = cir.getReturnValue();
		if (copy == original || !ForeverFixesConfig.enabled(ForeverFixesConfig.ASTRAL_ENCHANTMENT_DESCRIPTIONS))
		{
			return;
		}
		if (copy.equals(original))
		{
			cir.setReturnValue(original);
		}
		else if (original instanceof ContextProvider from && copy instanceof ContextProvider to)
		{
			to.enchdesc$setStack(from.enchdesc$getStack());
		}
	}
}
