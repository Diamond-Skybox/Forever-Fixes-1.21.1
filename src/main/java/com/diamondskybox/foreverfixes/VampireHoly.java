package com.diamondskybox.foreverfixes;

import de.teamlapen.vampirism.util.Helper;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

/**
 * Vampirism: holy things burn vampires. Every second a vampire takes holy.vampireDamagePerSource (holy-water damage) and is set alight
 * for each worn armor piece with a holy enchantment (Blessing), each held item with a holy weapon enchantment (Smite, Sanctified), and
 * each held enchanted book carrying one of them.
 */
public final class VampireHoly
{
	private static final ResourceKey<net.minecraft.world.damagesource.DamageType> HOLY_WATER =
			ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("vampirism", "holy_water"));
	private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

	private VampireHoly()
	{
	}

	public static void onPlayerTick(PlayerTickEvent.Post event)
	{
		if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0 || !player.isAlive() || player.isCreative() || player.isSpectator())
		{
			return;
		}
		final double perSource = ForeverFixesCommonConfig.VAMPIRE_HOLY_DAMAGE.get();
		if (perSource <= 0.0D || !Helper.isVampire(player))
		{
			return;
		}
		final List<? extends String> worn = ForeverFixesCommonConfig.HOLY_WORN_ENCHANTMENTS.get();
		final List<? extends String> held = ForeverFixesCommonConfig.HOLY_HELD_ENCHANTMENTS.get();
		final boolean books = ForeverFixesCommonConfig.HOLY_BOOKS.get();
		int sources = 0;
		for (EquipmentSlot slot : ARMOR)
		{
			if (has(player, player.getItemBySlot(slot), worn, false))
			{
				sources++;
			}
		}
		for (ItemStack stack : new ItemStack[]{player.getMainHandItem(), player.getOffhandItem()})
		{
			if (has(player, stack, held, false) || books && (has(player, stack, held, true) || has(player, stack, worn, true)))
			{
				sources++;
			}
		}
		if (sources == 0)
		{
			return;
		}
		final DamageSource source = player.level().registryAccess().registry(Registries.DAMAGE_TYPE).flatMap(r -> r.getHolder(HOLY_WATER))
				.map(h -> new DamageSource(h)).orElse(player.damageSources().magic());
		player.hurt(source, (float) (perSource * sources));
		final int fire = ForeverFixesCommonConfig.VAMPIRE_HOLY_FIRE.get();
		if (fire > 0)
		{
			player.igniteForSeconds(fire);
		}
	}

	/** Does this stack carry one of the enchantments (on the item itself, or stored in an enchanted book)? */
	private static boolean has(ServerPlayer player, ItemStack stack, List<? extends String> ids, boolean stored)
	{
		if (stack.isEmpty() || ids.isEmpty())
		{
			return false;
		}
		final ItemEnchantments enchantments = stored ? stack.get(DataComponents.STORED_ENCHANTMENTS) : stack.get(DataComponents.ENCHANTMENTS);
		if (enchantments == null || enchantments.isEmpty())
		{
			return false;
		}
		for (Holder<Enchantment> holder : enchantments.keySet())
		{
			if (holder.unwrapKey().map(k -> ids.contains(k.location().toString())).orElse(false))
			{
				return true;
			}
		}
		return false;
	}
}
