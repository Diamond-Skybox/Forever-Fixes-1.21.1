package com.diamondskybox.foreverfixes;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;

/**
 * Better Simple Clouds' wet ground only where it rains. Its rain haze finds open sky from a heightmap around the camera and wets
 * every surface at the top of its column while a storm is near, whatever the biome: under storm clouds, deserts, savannas and
 * badlands (no rain falls there) and snowy ground came out wet. A column whose biome would not rain at its surface, or is listed
 * in bettersimpleclouds.dryBiomes, reports a height above the build limit, which reads as covered: dry. Answers are kept per 4x4x4
 * biome cell (the biome grid), so the heightmap rebuild about once a second looks up a few thousand cells at most once.
 */
public final class WetGround
{
	private static final int MAX_CACHED = 65536;
	private static final Long2ByteOpenHashMap dry = new Long2ByteOpenHashMap();
	private static Level cacheLevel;
	private static List<? extends String> cacheList;
	private static boolean cacheOnlyRain;
	private static final List<ResourceKey<Biome>> dryIds = new ArrayList<>();
	private static final List<TagKey<Biome>> dryTags = new ArrayList<>();

	private WetGround()
	{
	}

	/** the heightmap height BSC's rain haze uses for this column: above the build limit where the ground stays dry */
	public static int height(Level level, int x, int z, int original)
	{
		final boolean onlyRain = ForeverFixesConfig.enabled(ForeverFixesConfig.BSC_WET_ONLY_WHERE_IT_RAINS);
		final List<? extends String> list = ForeverFixesConfig.SPEC.isLoaded() ? ForeverFixesConfig.BSC_DRY_BIOMES.get() : List.of();
		if (!onlyRain && list.isEmpty())
		{
			return original;
		}
		if (level != cacheLevel || list != cacheList || onlyRain != cacheOnlyRain || dry.size() > MAX_CACHED)
		{
			dry.clear();
			cacheLevel = level;
			cacheOnlyRain = onlyRain;
			if (list != cacheList)
			{
				cacheList = list;
				parse(list);
			}
		}
		final int qx = x >> 2;
		final int qy = original >> 2;
		final int qz = z >> 2;
		final long key = BlockPos.asLong(qx, qy, qz);
		byte answer = dry.getOrDefault(key, (byte) -1);
		if (answer < 0)
		{
			answer = isDry(level, qx, qy, qz, onlyRain) ? (byte) 1 : (byte) 0;
			dry.put(key, answer);
		}
		return answer == 1 ? level.getMaxBuildHeight() + 16 : original;
	}

	private static boolean isDry(Level level, int qx, int qy, int qz, boolean onlyRain)
	{
		final Holder<Biome> biome = level.getNoiseBiome(qx, qy, qz);
		for (ResourceKey<Biome> id : dryIds)
		{
			if (biome.is(id))
			{
				return true;
			}
		}
		for (TagKey<Biome> tag : dryTags)
		{
			if (biome.is(tag))
			{
				return true;
			}
		}
		return onlyRain && biome.value().getPrecipitationAt(new BlockPos((qx << 2) + 2, qy << 2, (qz << 2) + 2)) != Biome.Precipitation.RAIN;
	}

	private static void parse(List<? extends String> list)
	{
		dryIds.clear();
		dryTags.clear();
		for (String entry : list)
		{
			final boolean tag = entry.startsWith("#");
			final ResourceLocation id = ResourceLocation.tryParse(tag ? entry.substring(1) : entry);
			if (id == null)
			{
				continue;
			}
			if (tag)
			{
				dryTags.add(TagKey.create(Registries.BIOME, id));
			}
			else
			{
				dryIds.add(ResourceKey.create(Registries.BIOME, id));
			}
		}
	}
}
