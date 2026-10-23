package com.diamondskybox.foreverfixes;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enchantments listed in enchantments.blocked (foreverfixes-common.toml). Loot functions that enchant from the whole registry
 * (enchant_randomly without "options", which on a book allows every enchantment) skip them (MixinEnchantRandomlyFunction), and
 * every two seconds they are taken off the items in each player's inventory; an enchanted book left without enchantments
 * becomes a plain book.
 */
public final class BlockedEnchantments
{
	private static final int SCAN_INTERVAL = 40;
	private static List<? extends String> cachedList;
	private static Set<ResourceLocation> cachedIds = Set.of();

	private BlockedEnchantments()
	{
	}

	public static boolean isBlocked(Holder<Enchantment> enchantment)
	{
		final Set<ResourceLocation> ids = ids();
		return !ids.isEmpty() && enchantment.unwrapKey().map(key -> ids.contains(key.location())).orElse(false);
	}

	private static Set<ResourceLocation> ids()
	{
		if (!ForeverFixesCommonConfig.SPEC.isLoaded())
		{
			return Set.of();
		}
		final List<? extends String> list = ForeverFixesCommonConfig.BLOCKED_ENCHANTMENTS.get();
		if (list != cachedList)
		{
			cachedIds = list.stream().map(ResourceLocation::tryParse).filter(id -> id != null).collect(Collectors.toUnmodifiableSet());
			cachedList = list;
		}
		return cachedIds;
	}

	public static void onPlayerTick(PlayerTickEvent.Post event)
	{
		if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % SCAN_INTERVAL != 0 || ids().isEmpty())
		{
			return;
		}
		final Inventory inventory = player.getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++)
		{
			final ItemStack stack = inventory.getItem(slot);
			final ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
			if (enchantments.isEmpty() || enchantments.keySet().stream().noneMatch(BlockedEnchantments::isBlocked))
			{
				continue;
			}
			final ItemEnchantments left = EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.removeIf(BlockedEnchantments::isBlocked));
			if (left.isEmpty() && stack.is(Items.ENCHANTED_BOOK))
			{
				inventory.setItem(slot, stack.transmuteCopy(Items.BOOK, stack.getCount()));
			}
			ForeverFixes.LOGGER.info("Removed blocked enchantments from {} in {}'s inventory", stack.getItem(), player.getGameProfile().getName());
		}
	}
}
