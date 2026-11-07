package com.diamondskybox.foreverfixes;

import com.chromeware.body.BodyPart;
import com.chromeware.body.CyberLimbs;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Chromeware: cyber limbs don't fit inside armour (chromeware.armorFit). The chestplate covers torso + both arms, leggings and boots
 * cover both legs. Every replaced part takes its share off that piece's armour, toughness, knockback resistance and enchantment
 * levels (1/3 per chest part, 1/2 per leg); with every part replaced the piece comes off and won't go back on, Curse of Binding or not.
 * The renderer hides the piece on the replaced limbs.
 */
public final class CyberArmorFit
{
	private static final ResourceLocation ARMOR_ID = ResourceLocation.fromNamespaceAndPath("foreverfixes", "cyber_armor_fit");
	private static final ResourceLocation TOUGHNESS_ID = ResourceLocation.fromNamespaceAndPath("foreverfixes", "cyber_armor_fit_toughness");
	private static final ResourceLocation KNOCKBACK_ID = ResourceLocation.fromNamespaceAndPath("foreverfixes", "cyber_armor_fit_knockback");
	private static final EquipmentSlot[] SLOTS = {EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

	private CyberArmorFit()
	{
	}

	private static boolean on()
	{
		return ForeverFixesCommonConfig.SPEC.isLoaded() && ForeverFixesCommonConfig.CYBER_ARMOR_FIT.get();
	}

	private static boolean has(Player player, BodyPart part)
	{
		try
		{
			return CyberLimbs.has(player, part);
		}
		catch (RuntimeException e)
		{
			return false;
		}
	}

	/** Share of a worn piece that still works: 1 = all of it, 0 = it can't be worn. */
	public static double keep(LivingEntity entity, EquipmentSlot slot)
	{
		if (!(entity instanceof Player player) || !on())
		{
			return 1.0D;
		}
		return switch (slot)
		{
			case CHEST -> 1.0D - ((has(player, BodyPart.BODY) ? 1 : 0) + (has(player, BodyPart.LEFT_ARM) ? 1 : 0) + (has(player, BodyPart.RIGHT_ARM) ? 1 : 0)) / 3.0D;
			case LEGS, FEET -> 1.0D - ((has(player, BodyPart.LEFT_LEG) ? 1 : 0) + (has(player, BodyPart.RIGHT_LEG) ? 1 : 0)) / 2.0D;
			default -> 1.0D;
		};
	}

	public static boolean isArmor(ItemStack stack)
	{
		return !stack.isEmpty() && stack.getItem() instanceof ArmorItem;
	}

	public static boolean blocked(LivingEntity entity, EquipmentSlot slot, ItemStack stack)
	{
		return isArmor(stack) && keep(entity, slot) <= 1.0E-6D;
	}

	/** Enchantment level that still applies on a partly covered piece (rounded; 0 = none). */
	public static int level(LivingEntity entity, EquipmentSlot slot, int level)
	{
		final double k = keep(entity, slot);
		return k >= 1.0D ? level : (int) Math.round(level * k);
	}

	/** Which model parts of the piece in this slot are hidden (body, left arm, right arm, left leg, right leg). */
	public static boolean[] hiddenParts(LivingEntity entity, EquipmentSlot slot)
	{
		final boolean[] hide = new boolean[5];
		if (entity instanceof Player player && on())
		{
			if (slot == EquipmentSlot.CHEST)
			{
				hide[0] = has(player, BodyPart.BODY);
				hide[1] = has(player, BodyPart.LEFT_ARM);
				hide[2] = has(player, BodyPart.RIGHT_ARM);
			}
			else if (slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET)
			{
				hide[3] = has(player, BodyPart.LEFT_LEG);
				hide[4] = has(player, BodyPart.RIGHT_LEG);
			}
		}
		return hide;
	}

	public static void onPlayerTick(PlayerTickEvent.Post event)
	{
		if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 10 != 0)
		{
			return;
		}
		double armor = 0.0D, toughness = 0.0D, knockback = 0.0D;
		for (EquipmentSlot slot : SLOTS)
		{
			final ItemStack worn = player.getItemBySlot(slot);
			if (!isArmor(worn))
			{
				continue;
			}
			if (blocked(player, slot, worn))
			{
				player.setItemSlot(slot, ItemStack.EMPTY);
				if (!player.getInventory().add(worn))
				{
					player.drop(worn, false);
				}
				player.displayClientMessage(Component.literal("Your cyberware leaves no room for ").append(worn.getHoverName()), true);
				continue;
			}
			final double lost = 1.0D - keep(player, slot);
			if (lost <= 0.0D)
			{
				continue;
			}
			final double[] sums = new double[3];
			worn.forEachModifier(slot, (attribute, modifier) -> {
				if (modifier.operation() != AttributeModifier.Operation.ADD_VALUE)
				{
					return;
				}
				if (attribute.is(Attributes.ARMOR))
				{
					sums[0] += modifier.amount();
				}
				else if (attribute.is(Attributes.ARMOR_TOUGHNESS))
				{
					sums[1] += modifier.amount();
				}
				else if (attribute.is(Attributes.KNOCKBACK_RESISTANCE))
				{
					sums[2] += modifier.amount();
				}
			});
			armor += sums[0] * lost;
			toughness += sums[1] * lost;
			knockback += sums[2] * lost;
		}
		set(player, Attributes.ARMOR, ARMOR_ID, -armor);
		set(player, Attributes.ARMOR_TOUGHNESS, TOUGHNESS_ID, -toughness);
		set(player, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_ID, -knockback);
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
		instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
	}
}
