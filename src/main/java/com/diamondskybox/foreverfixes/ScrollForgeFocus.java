package com.diamondskybox.foreverfixes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** magic.scrollForgeFocus: spells forged with a specific focus item instead of their school's focus. */
public final class ScrollForgeFocus
{
	private static List<? extends String> parsedFrom;
	private static Map<ResourceLocation, ResourceLocation> bySpell = Map.of();

	private ScrollForgeFocus()
	{
	}

	private static Map<ResourceLocation, ResourceLocation> overrides()
	{
		final List<? extends String> current = ForeverFixesCommonConfig.SPEC.isLoaded() ? ForeverFixesCommonConfig.SCROLL_FORGE_FOCUS.get()
				: List.of("chromeware:none=minecraft:air");
		if (current != parsedFrom)
		{
			final Map<ResourceLocation, ResourceLocation> map = new HashMap<>();
			for (String entry : current)
			{
				final int eq = entry.indexOf('=');
				final ResourceLocation spell = ResourceLocation.tryParse(entry.substring(0, eq).trim());
				final ResourceLocation item = ResourceLocation.tryParse(entry.substring(eq + 1).trim());
				if (spell != null && item != null)
				{
					map.put(spell, item);
				}
			}
			bySpell = map;
			parsedFrom = current;
		}
		return bySpell;
	}

	private static ResourceLocation itemOf(ItemStack focus)
	{
		return focus.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(focus.getItem());
	}

	/** May this focus forge this spell? null = no override involved, decide normally. */
	public static Boolean allows(ResourceLocation spell, ItemStack focus)
	{
		final Map<ResourceLocation, ResourceLocation> map = overrides();
		final ResourceLocation required = map.get(spell);
		final ResourceLocation item = itemOf(focus);
		if (required != null)
		{
			return required.equals(item);
		}
		if (item != null && map.containsValue(item))
		{
			return false; // an override focus only makes its own spells
		}
		return null;
	}
}
