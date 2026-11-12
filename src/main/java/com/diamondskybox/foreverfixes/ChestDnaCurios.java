package com.diamondskybox.foreverfixes;

import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

/** Kept apart from {@link ChestDnaGear} so Curios classes only load when Curios is installed. */
final class ChestDnaCurios
{
	private ChestDnaCurios()
	{
	}

	static boolean isCurio(ItemStack stack)
	{
		return !CuriosApi.getItemStackSlots(stack, false).isEmpty();
	}
}
