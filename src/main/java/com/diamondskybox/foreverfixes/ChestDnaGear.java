package com.diamondskybox.foreverfixes;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import xsoras.chestdna.api.ChestDNAApi;
import xsoras.chestdna.api.ChestDNAEvents;

/**
 * ChestDNA: an origin only for player gear. ChestDNA stamped where every item came from (loot chest, mined, mob drop, crafted...),
 * so the same block from three sources sat in three stacks. Its stamps all pass its assign event, which here refuses anything that is
 * not gear: an unstackable item that is armor or worn (elytra, Equipable), a weapon or tool (tiered, bow, crossbow, trident, mace,
 * shield, or attack damage or armor in its attribute modifiers), or a Curios bauble. Origins already stamped on other items are
 * cleared from players' inventories, so their stacks merge again.
 */
public final class ChestDnaGear
{
	private ChestDnaGear()
	{
	}

	public static void init()
	{
		ChestDNAEvents.ASSIGN.register((stack, origin) -> origin == null || isGear(stack)
				|| !ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.CHESTDNA_GEAR_ONLY) ? origin : null);
		NeoForge.EVENT_BUS.addListener(ChestDnaGear::onPlayerTick);
	}

	public static boolean isGear(ItemStack stack)
	{
		if (stack.isEmpty() || stack.getMaxStackSize() > 1)
		{
			return false;
		}
		final Item item = stack.getItem();
		if (item instanceof ArmorItem || item instanceof ElytraItem || item instanceof TieredItem || item instanceof ProjectileWeaponItem
				|| item instanceof TridentItem || item instanceof MaceItem || item instanceof ShieldItem || Equipable.get(stack) != null)
		{
			return true;
		}
		final ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		for (ItemAttributeModifiers.Entry entry : modifiers.modifiers())
		{
			if (entry.attribute().value() == Attributes.ATTACK_DAMAGE.value() || entry.attribute().value() == Attributes.ARMOR.value())
			{
				return true;
			}
		}
		return ModList.get().isLoaded("curios") && ChestDnaCurios.isCurio(stack);
	}

	private static void onPlayerTick(PlayerTickEvent.Post event)
	{
		if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 40 != 0
				|| !ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.CHESTDNA_GEAR_ONLY))
		{
			return;
		}
		final Inventory inventory = player.getInventory();
		for (int i = 0; i < inventory.getContainerSize(); i++)
		{
			final ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty() && ChestDNAApi.hasOrigin(stack) && !isGear(stack))
			{
				ChestDNAApi.clearOrigin(stack);
			}
		}
	}
}
