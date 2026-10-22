package com.diamondskybox.foreverfixes;

import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Applies each mixin package only when every mod it patches is in the mod list, so a missing mod never produces mixin errors. */
public final class ForeverFixesMixinPlugin implements IMixinConfigPlugin
{
	private static final Logger LOGGER = LoggerFactory.getLogger("ForeverFixes");
	private static final String PACKAGE = "com.diamondskybox.foreverfixes.mixin.";
	/** mixin sub-package -> mods that must be present */
	private static final Map<String, List<String>> REQUIRED = Map.ofEntries(
			Map.entry("mobends", List.of("mobends")),
			Map.entry("waveycapes", List.of("waveycapes", "mobends")),
			Map.entry("sodium", List.of("sodium")),
			Map.entry("bettercombat", List.of("bettercombat")),
			Map.entry("travelerstitles", List.of("travelerstitles")),
			Map.entry("cosmonautics", List.of("dimensional_sable")),
			Map.entry("gyro", List.of("gyro")),
			Map.entry("sable", List.of("sable")),
			Map.entry("ragdoll", List.of("sable_player_ragdoll", "mca")),
			Map.entry("ragdollcape", List.of("sable_player_ragdoll", "waveycapes")),
			Map.entry("punchy", List.of("punchy", "mca")),
			Map.entry("genderarmor", List.of("mcabp", "armor_model_api")),
			Map.entry("scalablelux", List.of("scalablelux")));

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
	{
		final String rest = mixinClassName.startsWith(PACKAGE) ? mixinClassName.substring(PACKAGE.length()) : "";
		final String group = rest.contains(".") ? rest.substring(0, rest.indexOf('.')) : "";
		final List<String> mods = REQUIRED.getOrDefault(group, List.of());
		for (String mod : mods)
		{
			if (!isModPresent(mod))
			{
				LOGGER.info("Skipping {} ({} is not installed)", mixinClassName, mod);
				return false;
			}
		}
		return true;
	}

	private static boolean isModPresent(String modId)
	{
		try
		{
			final LoadingModList list = FMLLoader.getLoadingModList();
			return list != null && list.getModFileById(modId) != null;
		}
		catch (Throwable t)
		{
			return false;
		}
	}

	@Override
	public void onLoad(String mixinPackage)
	{
	}

	@Override
	public String getRefMapperConfig()
	{
		return null;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets)
	{
	}

	@Override
	public List<String> getMixins()
	{
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
	{
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
	{
	}
}
