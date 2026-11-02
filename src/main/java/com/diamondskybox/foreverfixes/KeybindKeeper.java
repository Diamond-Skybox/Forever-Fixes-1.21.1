package com.diamondskybox.foreverfixes;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.loading.ClientModLoader;
import net.neoforged.neoforge.client.settings.KeyModifier;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Keeps the player's key bindings. Client only.
 * <ul>
 * <li>Options read before mod loading know only vanilla's keys; NeoForge applies the saved bindings of mods' keys when mod loading
 * finishes, by reading options.txt again. A mod that saved the options in between wrote every mod key's default into the file,
 * so bindings the player had changed (or cleared) came back as the default on the next start. Saves during mod loading are held
 * back and done once the bindings are read.</li>
 * <li>Keys a mod adds later still (PneumaticCraft's armor upgrade keys, added in game) had no binding to read and were left out of
 * every save before they existed. Their saved lines stay in options.txt, and the bindings are applied when the keys appear.</li>
 * </ul>
 */
public final class KeybindKeeper
{
	/** "key_<name>" -> saved binding, as options.txt has them */
	private static final Map<String, String> saved = new LinkedHashMap<>();
	private static boolean pendingSave;
	private static int knownMappings = -1;

	private KeybindKeeper()
	{
	}

	/** true when Options.save should not run now: mods are still loading and their keys do not hold the saved bindings yet */
	public static boolean deferSave()
	{
		if (ForeverFixesConfig.enabled(ForeverFixesConfig.KEEP_KEYBINDS) && ClientModLoader.isLoading())
		{
			pendingSave = true;
			return true;
		}
		return false;
	}

	public static void afterLoad(Options options, File file, boolean limited)
	{
		readSaved(file);
		if (limited && pendingSave)
		{
			pendingSave = false;
			ForeverFixes.LOGGER.info("Options saved during mod loading were held back until the key bindings were read; saving them now");
			options.save();
		}
	}

	/** puts back the saved lines of keys that do not exist yet */
	public static void afterSave(Options options, File file)
	{
		if (!ForeverFixesConfig.enabled(ForeverFixesConfig.KEEP_KEYBINDS) || saved.isEmpty())
		{
			readSaved(file);
			return;
		}
		final Set<String> present = new HashSet<>();
		for (KeyMapping key : options.keyMappings)
		{
			present.add("key_" + key.getName());
		}
		final List<String> missing = new ArrayList<>();
		for (Map.Entry<String, String> entry : saved.entrySet())
		{
			if (!present.contains(entry.getKey()))
			{
				missing.add(entry.getKey() + ":" + entry.getValue());
			}
		}
		if (!missing.isEmpty())
		{
			try
			{
				Files.write(file.toPath(), missing, StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
			}
			catch (IOException e)
			{
				ForeverFixes.LOGGER.warn("Could not keep the saved bindings of {} keys that do not exist yet", missing.size(), e);
			}
		}
		readSaved(file);
	}

	/** applies saved bindings to keys added since the last look */
	public static void onClientTick(ClientTickEvent.Post event)
	{
		final Options options = Minecraft.getInstance().options;
		if (options == null || options.keyMappings.length == knownMappings || ClientModLoader.isLoading())
		{
			return;
		}
		final boolean first = knownMappings < 0;
		knownMappings = options.keyMappings.length;
		if (first || !ForeverFixesConfig.enabled(ForeverFixesConfig.KEEP_KEYBINDS))
		{
			return;
		}
		boolean changed = false;
		for (KeyMapping key : options.keyMappings)
		{
			final String value = saved.get("key_" + key.getName());
			final String current = key.saveString() + (key.getKeyModifier() != KeyModifier.NONE ? ":" + key.getKeyModifier() : "");
			if (value != null && !value.equals(current))
			{
				final String[] parts = value.split(":");
				key.setKeyModifierAndCode(parts.length > 1 ? KeyModifier.valueFromString(parts[1]) : KeyModifier.NONE, InputConstants.getKey(parts[0]));
				changed = true;
			}
		}
		if (changed)
		{
			KeyMapping.resetMapping();
		}
	}

	private static void readSaved(File file)
	{
		saved.clear();
		try
		{
			for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8))
			{
				final int colon = line.indexOf(':');
				if (line.startsWith("key_") && colon > 4)
				{
					saved.put(line.substring(0, colon), line.substring(colon + 1));
				}
			}
		}
		catch (IOException e)
		{
			// no options.txt yet
		}
	}
}
