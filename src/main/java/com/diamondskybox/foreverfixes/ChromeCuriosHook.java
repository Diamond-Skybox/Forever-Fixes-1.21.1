package com.diamondskybox.foreverfixes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Set;

/** Curios calls for {@link ChromeMagic}; only loaded when Curios is installed. */
final class ChromeCuriosHook
{
	private ChromeCuriosHook()
	{
	}

	static boolean wearsAny(Player player, Set<ResourceLocation> items)
	{
		if (items.isEmpty())
		{
			return false;
		}
		return CuriosApi.getCuriosInventory(player)
				.map(inv -> inv.isEquipped(stack -> items.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()))))
				.orElse(false);
	}
}
