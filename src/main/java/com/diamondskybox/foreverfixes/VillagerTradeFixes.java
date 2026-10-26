package com.diamondskybox.foreverfixes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Villager and wandering trader trades whose result is listed in trades.removedResults (foreverfixes-common.toml) are dropped after
 * every mod has added its trades. Default: AE2's Fluix Researcher selling charged certus quartz, which made certus available for
 * emeralds instead of only from the planets it is meant to come from.
 */
public final class VillagerTradeFixes
{
	private VillagerTradeFixes()
	{
	}

	public static void onVillagerTrades(VillagerTradesEvent event)
	{
		final Set<ResourceLocation> removed = removed();
		if (removed.isEmpty())
		{
			return;
		}
		event.getTrades().forEach((level, listings) -> strip(listings, removed, event.getType().toString() + " level " + level));
	}

	public static void onWandererTrades(WandererTradesEvent event)
	{
		final Set<ResourceLocation> removed = removed();
		if (!removed.isEmpty())
		{
			strip(event.getGenericTrades(), removed, "wandering trader");
			strip(event.getRareTrades(), removed, "wandering trader (rare)");
		}
	}

	private static void strip(List<ItemListing> listings, Set<ResourceLocation> removed, String where)
	{
		final RandomSource random = RandomSource.create(0L);
		listings.removeIf(listing -> {
			try
			{
				final MerchantOffer offer = listing.getOffer(null, random);
				if (offer != null && removed.contains(BuiltInRegistries.ITEM.getKey(offer.getResult().getItem())))
				{
					ForeverFixes.LOGGER.info("Removed the {} trade for {}", where, BuiltInRegistries.ITEM.getKey(offer.getResult().getItem()));
					return true;
				}
			}
			catch (RuntimeException ignored)
			{
				// a listing that needs a real trader to build its offer is left alone
			}
			return false;
		});
	}

	private static Set<ResourceLocation> removed()
	{
		if (!ForeverFixesCommonConfig.SPEC.isLoaded())
		{
			return Set.of();
		}
		return ForeverFixesCommonConfig.REMOVED_TRADE_RESULTS.get().stream().map(ResourceLocation::tryParse).filter(id -> id != null)
				.collect(Collectors.toUnmodifiableSet());
	}
}
