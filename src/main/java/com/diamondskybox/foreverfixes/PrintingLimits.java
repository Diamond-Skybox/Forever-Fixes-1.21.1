package com.diamondskybox.foreverfixes;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;

/** Glowbrick's Printing Press: type blocks with durability wear out per print; some enchantments can't be typeset. */
public final class PrintingLimits
{
	private PrintingLimits()
	{
	}

	/** Damages a type block that carries max_damage by one print; returns the stack left in the slot (empty once used up). */
	public static ItemStack wear(ItemStack typeBlock)
	{
		if (typeBlock.isEmpty() || !typeBlock.has(DataComponents.MAX_DAMAGE))
		{
			return typeBlock;
		}
		final int damage = typeBlock.getDamageValue() + 1;
		if (damage >= typeBlock.getMaxDamage())
		{
			return ItemStack.EMPTY;
		}
		typeBlock.setDamageValue(damage);
		return typeBlock;
	}

	public static boolean untypesettable(ItemStack book)
	{
		final List<? extends String> denied = ForeverFixesCommonConfig.PRINTING_NO_TYPESET.get();
		if (book.isEmpty() || denied.isEmpty())
		{
			return false;
		}
		final ItemEnchantments stored = book.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
		for (Holder<Enchantment> holder : stored.keySet())
		{
			if (holder.unwrapKey().map(k -> denied.contains(k.location().toString())).orElse(false))
			{
				return true;
			}
		}
		return false;
	}
}
