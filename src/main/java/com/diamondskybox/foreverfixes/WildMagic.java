package com.diamondskybox.foreverfixes;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Wild Magic (the enchantment wildMagic.enchantment, a plain data-pack enchantment with no effects of its own): when the wearer is hit by
 * something, each enchanted armor piece has level x 1% to give the wearer one random effect from every registered effect, good, bad or
 * neutral, minus wildMagic.excludedEffects, for 2 s per level at amplifier (level - 1) / 2. Harmful ones are capped at
 * wildMagic.maxHarmfulAmplifier. Runs on the server only, so clients need nothing beyond the synced enchantment.
 */
public final class WildMagic
{
	private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

	private static List<? extends String> poolConfig;
	private static int poolRegistrySize = -1;
	private static List<Holder<MobEffect>> pool = List.of();

	private WildMagic()
	{
	}

	public static void onDamage(LivingDamageEvent.Post event)
	{
		final LivingEntity target = event.getEntity();
		if (!(target.level() instanceof ServerLevel level) || !target.isAlive() || event.getSource().getEntity() == null)
		{
			return;
		}
		final ResourceLocation id = ResourceLocation.tryParse(ForeverFixesCommonConfig.WILD_MAGIC_ENCHANTMENT.get());
		if (id == null)
		{
			return;
		}
		final Optional<Holder.Reference<Enchantment>> enchantment = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
				.getHolder(ResourceKey.create(Registries.ENCHANTMENT, id));
		if (enchantment.isEmpty())
		{
			return;
		}
		for (EquipmentSlot slot : ARMOR)
		{
			final ItemStack stack = target.getItemBySlot(slot);
			final int lvl = stack.isEmpty() ? 0 : stack.getEnchantments().getLevel(enchantment.get());
			if (lvl > 0 && target.getRandom().nextFloat() < 0.01F * lvl)
			{
				apply(target, lvl);
			}
		}
	}

	/** Wild Magic only comes from its boss typesetting: random-enchant loot never rolls it. */
	public static boolean is(Holder<Enchantment> enchantment)
	{
		return enchantment.unwrapKey().map(k -> k.location().toString().equals(ForeverFixesCommonConfig.WILD_MAGIC_ENCHANTMENT.get())).orElse(false);
	}

	private static void apply(LivingEntity target, int lvl)
	{
		final List<Holder<MobEffect>> effects = pool();
		if (effects.isEmpty())
		{
			return;
		}
		final Holder<MobEffect> effect = effects.get(target.getRandom().nextInt(effects.size()));
		final int duration = effect.value().isInstantenous() ? 1 : 40 * lvl;
		int amplifier = Math.max(0, Math.round(0.5F * (lvl - 1)));
		if (effect.value().getCategory() == MobEffectCategory.HARMFUL)
		{
			amplifier = Math.min(amplifier, ForeverFixesCommonConfig.WILD_MAGIC_MAX_HARMFUL_AMPLIFIER.get());
		}
		target.addEffect(new MobEffectInstance(effect, duration, amplifier), target);
	}

	private static List<Holder<MobEffect>> pool()
	{
		final List<? extends String> excluded = ForeverFixesCommonConfig.WILD_MAGIC_EXCLUDED.get();
		if (excluded == poolConfig && poolRegistrySize == BuiltInRegistries.MOB_EFFECT.size())
		{
			return pool;
		}
		final List<Pattern> patterns = new ArrayList<>();
		for (String glob : excluded)
		{
			patterns.add(Pattern.compile(Pattern.quote(glob).replace("*", "\\E.*\\Q")));
		}
		final List<Holder<MobEffect>> built = new ArrayList<>();
		BuiltInRegistries.MOB_EFFECT.holders().forEach(h -> {
			final String key = h.key().location().toString();
			for (Pattern p : patterns)
			{
				if (p.matcher(key).matches())
				{
					return;
				}
			}
			built.add(h);
		});
		pool = List.copyOf(built);
		poolConfig = excluded;
		poolRegistrySize = BuiltInRegistries.MOB_EFFECT.size();
		return pool;
	}
}
