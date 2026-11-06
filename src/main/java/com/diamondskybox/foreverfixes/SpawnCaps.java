package com.diamondskybox.foreverfixes;

import net.minecraft.world.entity.MobCategory;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** spawning.mobCapScale: "category=scale" entries (monster, creature, ambient, axolotls, underground_water_creature, water_creature, water_ambient). */
public final class SpawnCaps
{
	private static List<? extends String> parsedFrom;
	private static Map<MobCategory, Double> scales = new EnumMap<>(MobCategory.class);

	private SpawnCaps()
	{
	}

	public static int scale(MobCategory category, int cap)
	{
		if (!ForeverFixesCommonConfig.SPEC.isLoaded())
		{
			return cap;
		}
		final List<? extends String> current = ForeverFixesCommonConfig.MOB_CAP_SCALE.get();
		if (current != parsedFrom)
		{
			final Map<MobCategory, Double> parsed = new EnumMap<>(MobCategory.class);
			for (String entry : current)
			{
				final int eq = entry.indexOf('=');
				if (eq <= 0)
				{
					continue;
				}
				final String name = entry.substring(0, eq).trim();
				for (MobCategory c : MobCategory.values())
				{
					if (c.getName().equalsIgnoreCase(name))
					{
						try
						{
							parsed.put(c, Math.max(0.0D, Double.parseDouble(entry.substring(eq + 1).trim())));
						}
						catch (NumberFormatException ignored)
						{
						}
					}
				}
			}
			scales = parsed;
			parsedFrom = current;
		}
		final Double s = scales.get(category);
		return s == null ? cap : (int) Math.round(cap * s);
	}
}
