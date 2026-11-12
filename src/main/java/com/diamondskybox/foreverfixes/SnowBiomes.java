package com.diamondskybox.foreverfixes;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Biomes whose precipitation is snow whatever their temperature (see {@link ForeverFixesCommonConfig#SNOW_INSTEAD_OF_RAIN}). Vanilla
 * decides rain or snow per block from the biome temperature, and the deep frozen ocean (0.5, "frozen" noise) rains at sea level;
 * vanilla's weather, snow layers, Simple Clouds, Petrichor and Hoarfrost all ask that one answer, so the biome's answer is changed
 * (see {@code MixinBiomePrecipitationSnow}). Water freezing is left to the temperature. The set is rebuilt whenever tags are
 * (re)loaded, on both sides.
 */
public final class SnowBiomes
{
	private static volatile Set<Biome> snowy = Collections.emptySet();

	private SnowBiomes()
	{
	}

	public static boolean snows(Biome biome)
	{
		return snowy.contains(biome);
	}

	public static void onTagsUpdated(TagsUpdatedEvent event)
	{
		final Registry<Biome> biomes = event.getRegistryAccess().registryOrThrow(Registries.BIOME);
		final Set<Biome> set = Collections.newSetFromMap(new IdentityHashMap<>());
		final List<? extends String> list = ForeverFixesCommonConfig.SNOW_INSTEAD_OF_RAIN.get();
		for (String entry : list)
		{
			if (entry.startsWith("#"))
			{
				final ResourceLocation id = ResourceLocation.tryParse(entry.substring(1));
				if (id != null)
				{
					biomes.getTag(TagKey.create(Registries.BIOME, id)).ifPresent(tag -> tag.forEach(holder -> set.add(holder.value())));
				}
			}
			else
			{
				final ResourceLocation id = ResourceLocation.tryParse(entry);
				if (id != null)
				{
					biomes.getHolder(ResourceKey.create(Registries.BIOME, id)).map(Holder::value).ifPresent(set::add);
				}
			}
		}
		snowy = set;
	}
}
