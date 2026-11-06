package com.diamondskybox.foreverfixes;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * Chromeware raises the cyberware capacity limit for 68 of Create's own advancements. This adds magic.chrome.addonAdvancementPoints
 * for each completed advancement of any other mod that requires Create (recipe-unlock advancements excluded), up to
 * magic.chrome.addonAdvancementCap.
 */
public final class CreateAddonAdvancements
{
	private static Set<String> namespaces;

	private CreateAddonAdvancements()
	{
	}

	private static Set<String> namespaces()
	{
		if (namespaces == null)
		{
			final Set<String> set = new HashSet<>();
			for (IModInfo mod : ModList.get().getMods())
			{
				for (IModInfo.ModVersion dependency : mod.getDependencies())
				{
					if ("create".equals(dependency.getModId()) && dependency.getType() == IModInfo.DependencyType.REQUIRED)
					{
						set.add(mod.getModId());
						break;
					}
				}
			}
			namespaces = set;
		}
		return namespaces;
	}

	public static boolean counts(ResourceLocation id)
	{
		if (ForeverFixesCommonConfig.CHROME_ADDON_ADVANCEMENT_POINTS.get() <= 0 || id.getPath().startsWith("recipes/"))
		{
			return false;
		}
		return namespaces().contains(id.getNamespace()) && !ForeverFixesCommonConfig.CHROME_ADDON_EXCLUDED.get().contains(id.getNamespace());
	}

	/** Extra capacity from this player's completed Create-addon advancements. */
	public static int bonus(ServerPlayer player)
	{
		final int points = ForeverFixesCommonConfig.CHROME_ADDON_ADVANCEMENT_POINTS.get();
		if (points <= 0)
		{
			return 0;
		}
		int done = 0;
		for (AdvancementHolder holder : player.server.getAdvancements().getAllAdvancements())
		{
			if (counts(holder.id()) && player.getAdvancements().getOrStartProgress(holder).isDone())
			{
				done++;
			}
		}
		return Math.min(ForeverFixesCommonConfig.CHROME_ADDON_ADVANCEMENT_CAP.get(), done * points);
	}
}
