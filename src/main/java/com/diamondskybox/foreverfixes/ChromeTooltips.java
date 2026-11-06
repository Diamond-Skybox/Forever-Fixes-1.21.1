package com.diamondskybox.foreverfixes;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.Locale;

/** Client: the magic trade-off of each cyberware part and of the personal shield, on their tooltips (values from magic.chrome.*). */
public final class ChromeTooltips
{
	private static final boolean CHROMEWARE = ModList.get().isLoaded("chromeware");

	private ChromeTooltips()
	{
	}

	public static void onTooltip(ItemTooltipEvent event)
	{
		if (!ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.CHROME_MAGIC))
		{
			return;
		}
		final ItemStack stack = event.getItemStack();
		final List<Component> lines = event.getToolTip();
		final int points = CHROMEWARE ? ChromewareHook.itemCapacity(stack) : 0;
		if (points > 0)
		{
			lines.add(Component.literal("Magic (" + points + " capacity, after all other bonuses):").withStyle(ChatFormatting.DARK_PURPLE));
			if ("LOST_POTENTIAL".equals(ForeverFixesCommonConfig.CHROME_RESIST_MODE.get()))
			{
				lines.add(Component.literal(String.format(Locale.ROOT, " Spell Resistance: %.0f%% of the magic lost", ForeverFixesCommonConfig.CHROME_LOST_POTENTIAL_SHARE.get() * 100))
						.withStyle(ChatFormatting.BLUE));
			}
			else
			{
				lines.add(line("+", points * ForeverFixesCommonConfig.CHROME_RESIST_PER_POINT.get(), "Spell Resistance", ChatFormatting.BLUE));
			}
			lines.add(line("x -", ChromeMagic.share(points, ForeverFixesCommonConfig.CHROME_SPELL_POWER_PER_POINT.get()), "Spell Power", ChatFormatting.RED));
			lines.add(line("x -", ChromeMagic.share(points, ForeverFixesCommonConfig.CHROME_MAX_MANA_PER_POINT.get()), "Max Mana", ChatFormatting.RED));
			lines.add(line("x -", ChromeMagic.share(points, ForeverFixesCommonConfig.CHROME_MANA_REGEN_PER_POINT.get()), "Mana Regen", ChatFormatting.RED));
			if (!"LOST_POTENTIAL".equals(ForeverFixesCommonConfig.CHROME_RESIST_MODE.get()))
			{
				lines.add(Component.literal(String.format(Locale.ROOT, " Each part multiplies; resistance caps at +%.0f%%",
						ForeverFixesCommonConfig.CHROME_RESIST_CAP.get() * 100)).withStyle(ChatFormatting.DARK_GRAY));
			}
		}
		final ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
		for (String s : ForeverFixesCommonConfig.SHIELD_ITEMS.get())
		{
			if (id.toString().equals(s))
			{
				lines.add(Component.literal("Magic (while worn):").withStyle(ChatFormatting.DARK_PURPLE));
				lines.add(line("-", ForeverFixesCommonConfig.SHIELD_MANA_REGEN_PENALTY.get(), "Mana Regen", ChatFormatting.RED));
				break;
			}
		}
	}

	private static Component line(String sign, double fraction, String what, ChatFormatting colour)
	{
		return Component.literal(String.format(Locale.ROOT, " %s%.1f%% %s", sign, fraction * 100, what)).withStyle(colour);
	}
}
