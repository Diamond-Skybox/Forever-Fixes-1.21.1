package com.diamondskybox.foreverfixes;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Iron's Spells x Chromeware / Energy Field: chrome trades magic for magic resistance.
 *
 * Every point of Chromeware cyberware capacity in use gives spell resistance and takes spell power, max mana and mana regen
 * (all "multiply total", capped); a worn Energy Field personal shield generator takes a further share of mana regen. The
 * modifiers are transient and refreshed every second, so they follow surgery, limb loss and unequipping without saving anything.
 */
public final class ChromeMagic
{
	private static final ResourceLocation RESIST_ID = id("chrome_spell_resist");
	private static final ResourceLocation POWER_ID = id("chrome_spell_power");
	private static final ResourceLocation MANA_ID = id("chrome_max_mana");
	private static final ResourceLocation REGEN_ID = id("chrome_mana_regen");
	private static final ResourceLocation SHIELD_ID = id("shield_mana_regen");

	private static final boolean CHROMEWARE = ModList.get().isLoaded("chromeware");
	private static final boolean CURIOS = ModList.get().isLoaded("curios");

	private static Holder<Attribute> spellResist, spellPower, maxMana, manaRegen;
	private static List<? extends String> shieldConfig;
	private static Set<ResourceLocation> shieldItems = Set.of();

	private ChromeMagic()
	{
	}

	private static ResourceLocation id(String path)
	{
		return ResourceLocation.fromNamespaceAndPath("foreverfixes", path);
	}

	public static void onPlayerTick(PlayerTickEvent.Post event)
	{
		if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0)
		{
			return;
		}
		if (spellResist == null && !resolve())
		{
			return;
		}
		final boolean on = ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.CHROME_MAGIC);
		final int[] parts = on && CHROMEWARE ? ChromewareHook.partCosts(player) : new int[0];
		// one "multiply total" modifier per part: Minecraft applies those one after another, after every other bonus
		double keptMana = 1.0D;
		for (int i = 0; i < MAX_PARTS; i++)
		{
			final int cost = i < parts.length ? parts[i] : 0;
			final double power = share(cost, ForeverFixesCommonConfig.CHROME_SPELL_POWER_PER_POINT.get());
			final double mana = share(cost, ForeverFixesCommonConfig.CHROME_MAX_MANA_PER_POINT.get());
			final double regen = share(cost, ForeverFixesCommonConfig.CHROME_MANA_REGEN_PER_POINT.get());
			set(player, spellPower, partId("power", i), -power);
			set(player, maxMana, partId("mana", i), -mana);
			set(player, manaRegen, partId("regen", i), -regen);
			keptMana *= 1.0D - mana;
		}
		// earlier versions used one flat modifier per stat
		set(player, spellPower, POWER_ID, 0.0D);
		set(player, maxMana, MANA_ID, 0.0D);
		set(player, manaRegen, REGEN_ID, 0.0D);
		double resist = 0.0D;
		if (parts.length > 0)
		{
			if ("LOST_POTENTIAL".equals(ForeverFixesCommonConfig.CHROME_RESIST_MODE.get()))
			{
				resist = ForeverFixesCommonConfig.CHROME_LOST_POTENTIAL_SHARE.get() * (1.0D - keptMana);
			}
			else
			{
				int used = 0;
				for (int cost : parts)
				{
					used += cost;
				}
				resist = Math.min(ForeverFixesCommonConfig.CHROME_RESIST_CAP.get(), used * ForeverFixesCommonConfig.CHROME_RESIST_PER_POINT.get());
			}
		}
		set(player, spellResist, RESIST_ID, resist);
		final boolean shield = on && CURIOS && ChromeCuriosHook.wearsAny(player, shieldItems());
		set(player, manaRegen, SHIELD_ID, shield ? -ForeverFixesCommonConfig.SHIELD_MANA_REGEN_PENALTY.get() : 0.0D);
	}

	private static final int MAX_PARTS = 32;

	private static ResourceLocation partId(String stat, int index)
	{
		return id("chrome_" + stat + "_" + index);
	}

	/** One part's share of a stat: capacity x rate, rounded to magic.chrome.rounding, never all of it. */
	public static double share(int cost, double perPoint)
	{
		if (cost <= 0)
		{
			return 0.0D;
		}
		final double step = ForeverFixesCommonConfig.CHROME_ROUNDING.get();
		double value = cost * perPoint;
		if (step > 0.0D)
		{
			value = Math.round(value / step) * step;
		}
		return Math.min(0.95D, Math.max(0.0D, value));
	}

	private static boolean resolve()
	{
		spellResist = attribute("spell_resist");
		spellPower = attribute("spell_power");
		maxMana = attribute("max_mana");
		manaRegen = attribute("mana_regen");
		return spellResist != null && spellPower != null && maxMana != null && manaRegen != null;
	}

	private static Holder<Attribute> attribute(String path)
	{
		final Optional<Holder.Reference<Attribute>> holder = BuiltInRegistries.ATTRIBUTE
				.getHolder(ResourceLocation.fromNamespaceAndPath("irons_spellbooks", path));
		return holder.<Holder<Attribute>>map(h -> h).orElse(null);
	}

	private static Set<ResourceLocation> shieldItems()
	{
		final List<? extends String> current = ForeverFixesCommonConfig.SHIELD_ITEMS.get();
		if (current != shieldConfig)
		{
			final Set<ResourceLocation> parsed = new HashSet<>();
			for (String s : current)
			{
				final ResourceLocation rl = ResourceLocation.tryParse(s);
				if (rl != null)
				{
					parsed.add(rl);
				}
			}
			shieldItems = parsed;
			shieldConfig = current;
		}
		return shieldItems;
	}

	private static void set(ServerPlayer player, Holder<Attribute> attribute, ResourceLocation id, double amount)
	{
		final AttributeInstance instance = player.getAttribute(attribute);
		if (instance == null)
		{
			return;
		}
		final AttributeModifier existing = instance.getModifier(id);
		if (Math.abs(amount) < 1.0E-6D)
		{
			if (existing != null)
			{
				instance.removeModifier(id);
			}
			return;
		}
		if (existing != null && Math.abs(existing.amount() - amount) < 1.0E-9D)
		{
			return;
		}
		instance.removeModifier(id);
		instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	}
}
