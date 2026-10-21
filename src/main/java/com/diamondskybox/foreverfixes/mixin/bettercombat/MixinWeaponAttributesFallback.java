package com.diamondskybox.foreverfixes.mixin.bettercombat;

import java.util.List;

import net.minecraft.world.item.component.ItemAttributeModifiers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Better Combat 2.4.0's fallback compatibility walks every registered item at server start and reads its
 * {@code ATTRIBUTE_MODIFIERS} component in {@code WeaponAttributesFallback.hasAttributeModifier} without a null
 * check. An item registered without that default component (some mods do this) crashes the server before the
 * world loads. An item with no component has no attribute modifiers, so it is answered with an empty list.
 * Common side: Better Combat runs this on the dedicated server as well as in single player.
 */
@Mixin(targets = "net.bettercombat.logic.WeaponAttributesFallback", remap = false)
public abstract class MixinWeaponAttributesFallback
{
	@Redirect(method = "hasAttributeModifier", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/world/item/component/ItemAttributeModifiers;modifiers()Ljava/util/List;"))
	private static List<ItemAttributeModifiers.Entry> foreverfixes$modifiersOrEmpty(ItemAttributeModifiers modifiers)
	{
		return modifiers == null ? List.of() : modifiers.modifiers();
	}
}
