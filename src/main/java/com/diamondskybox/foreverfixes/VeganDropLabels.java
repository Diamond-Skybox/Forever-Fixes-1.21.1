package com.diamondskybox.foreverfixes;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Vegan Delight: the "not vegan" stamps it put on items dropped by entities (see {@code MixinVeganLabelsNoDropStamp}) are cleared
 * from players' inventories every two seconds, so those stacks merge with the rest again. The stamp was its only one, so nothing
 * else is lost; the labels from its tags and recipes do not live on the item.
 */
public final class VeganDropLabels
{
	private static final ResourceLocation IS_VEGAN = ResourceLocation.fromNamespaceAndPath("vegandelight", "is_vegan");
	private static DataComponentType<?> component;

	private VeganDropLabels()
	{
	}

	public static void init()
	{
		NeoForge.EVENT_BUS.addListener(VeganDropLabels::onPlayerTick);
	}

	private static void onPlayerTick(PlayerTickEvent.Post event)
	{
		if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 40 != 20
				|| !ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.VEGANDELIGHT_NO_DROP_LABELS))
		{
			return;
		}
		if (component == null)
		{
			component = BuiltInRegistries.DATA_COMPONENT_TYPE.get(IS_VEGAN);
			if (component == null)
			{
				return;
			}
		}
		final Inventory inventory = player.getInventory();
		for (int i = 0; i < inventory.getContainerSize(); i++)
		{
			final ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty() && stack.has(component))
			{
				stack.remove(component);
			}
		}
	}
}
