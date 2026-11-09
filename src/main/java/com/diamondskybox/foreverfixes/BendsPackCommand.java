package com.diamondskybox.foreverfixes;

import com.mojang.brigadier.arguments.StringArgumentType;
import goblinbob.mobends.core.pack.IBendsPack;
import goblinbob.mobends.core.pack.InvalidPackFormatException;
import goblinbob.mobends.core.pack.LocalBendsPack;
import goblinbob.mobends.core.pack.PackManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Applies Mo' Bends packs from chat. Mo' Bends 6.0.1's Packs tab is marked WIP and its toggles don't take clicks, so:
 * {@code /bendspack list}, {@code /bendspack apply <key>} (adds to the applied set), {@code /bendspack remove <key>},
 * {@code /bendspack clear}, {@code /bendspack reload} (rescans bendspacks/ and re-applies). A pack Mo' Bends rejects
 * is reported with its reason.
 */
public final class BendsPackCommand
{
	private BendsPackCommand()
	{
	}

	public static void register()
	{
		NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> event.getDispatcher().register(Commands.literal("bendspack")
				.then(Commands.literal("list").executes(c -> list(c.getSource())))
				.then(Commands.literal("apply").then(Commands.argument("key", StringArgumentType.word()).executes(c -> apply(c.getSource(), StringArgumentType.getString(c, "key"), true))))
				.then(Commands.literal("remove").then(Commands.argument("key", StringArgumentType.word()).executes(c -> apply(c.getSource(), StringArgumentType.getString(c, "key"), false))))
				.then(Commands.literal("clear").executes(c -> clear(c.getSource())))
				.then(Commands.literal("reload").executes(c -> reload(c.getSource())))));
	}

	private static List<String> appliedKeys()
	{
		return PackManager.INSTANCE.getAppliedPacks().stream().map(IBendsPack::getKey).collect(Collectors.toCollection(ArrayList::new));
	}

	private static int list(CommandSourceStack source)
	{
		final List<String> applied = appliedKeys();
		final StringBuilder text = new StringBuilder("Bends packs in bendspacks/:");
		for (LocalBendsPack pack : PackManager.INSTANCE.getLocalPacks())
		{
			text.append("\n  ").append(applied.contains(pack.getKey()) ? "[on]  " : "[off] ").append(pack.getKey()).append(" — ").append(pack.getDisplayName()).append(" by ").append(pack.getAuthor());
		}
		if (PackManager.INSTANCE.getLocalPacks().isEmpty())
		{
			text.append(" none (use /bendspack reload after adding files)");
		}
		source.sendSuccess(() -> Component.literal(text.toString()), false);
		return 1;
	}

	private static int apply(CommandSourceStack source, String key, boolean on)
	{
		final List<String> keys = appliedKeys();
		keys.remove(key);
		if (on)
		{
			if (PackManager.INSTANCE.getLocalPacks().stream().noneMatch(p -> p.getKey().equals(key)))
			{
				source.sendFailure(Component.literal("No pack \"" + key + "\" in bendspacks/ (try /bendspack reload, then /bendspack list)."));
				return 0;
			}
			keys.add(key);
		}
		return set(source, keys, (on ? "Applied " : "Removed ") + key);
	}

	private static int clear(CommandSourceStack source)
	{
		return set(source, List.of(), "No packs applied");
	}

	private static int reload(CommandSourceStack source)
	{
		final List<String> keys = appliedKeys();
		try
		{
			PackManager.INSTANCE.initLocalPacks();
		}
		catch (InvalidPackFormatException e)
		{
			source.sendFailure(Component.literal("Rescan failed: " + e.getMessage()));
			return 0;
		}
		final List<String> known = PackManager.INSTANCE.getLocalPacks().stream().map(LocalBendsPack::getKey).toList();
		keys.retainAll(known);
		return set(source, keys, "Rescanned bendspacks/: " + known.size() + " pack(s)");
	}

	private static int set(CommandSourceStack source, List<String> keys, String done)
	{
		try
		{
			PackManager.INSTANCE.setAppliedPacks(keys, true);
			source.sendSuccess(() -> Component.literal(done + ". Applied now: " + (keys.isEmpty() ? "none" : String.join(", ", keys))).withStyle(ChatFormatting.GREEN), false);
			return 1;
		}
		catch (InvalidPackFormatException e)
		{
			source.sendFailure(Component.literal("Mo' Bends rejected the pack: " + e.getMessage() + " (details in latest.log)"));
			return 0;
		}
	}
}
