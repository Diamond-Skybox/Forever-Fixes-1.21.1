package com.diamondskybox.foreverfixes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.event.LootTableLoadEvent;

/**
 * Every chest loot table (type minecraft:chest, or an untyped table under chests/) gets one extra pool with a few coins, so every
 * structure chest, barrel and chest cart (Lootr rolls the same tables per player) holds some money (loot.chestCoins.*).
 */
public final class ChestCoins
{
	private static final String POOL = "foreverfixes_chest_coins";

	private ChestCoins()
	{
	}

	public static void onLootTableLoad(LootTableLoadEvent event)
	{
		if (!ForeverFixesCommonConfig.SPEC.isLoaded())
		{
			return;
		}
		final int min = ForeverFixesCommonConfig.CHEST_COINS_MIN.get();
		final int max = Math.max(min, ForeverFixesCommonConfig.CHEST_COINS_MAX.get());
		final ResourceLocation itemId = ResourceLocation.tryParse(ForeverFixesCommonConfig.CHEST_COINS_ITEM.get());
		if (max <= 0 || itemId == null)
		{
			return;
		}
		final Item item = BuiltInRegistries.ITEM.get(itemId);
		final LootTable table = event.getTable();
		if (item == Items.AIR || !isChestTable(event.getName(), table))
		{
			return;
		}
		table.addPool(LootPool.lootPool()
				.name(POOL)
				.setRolls(ConstantValue.exactly(1))
				.add(LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(UniformGenerator.between(Math.max(1, min), max))))
				.build());
	}

	private static boolean isChestTable(ResourceLocation id, LootTable table)
	{
		if (table.getParamSet() == LootContextParamSets.CHEST)
		{
			return true;
		}
		final String path = id.getPath();
		return table.getParamSet() == LootTable.DEFAULT_PARAM_SET && (path.startsWith("chests/") || path.contains("/chests/"));
	}
}
