package com.diamondskybox.foreverfixes.mixin.vanilla;

import com.diamondskybox.foreverfixes.ForeverFixesCommonConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Every change to a player inventory slot runs the inventory_changed advancement trigger, which tests the stack against every
 * unfinished "has item" criterion (hundreds of recipe unlocks in a large pack). Items that rewrite their own data every tick, like
 * Relics' Clot of Time (its rewind path and cooldown), ran that every tick for as long as they were carried (spark on the server:
 * ~15% of the server thread for one player). Changes that keep the item and count, i.e. only its data changed, now reach the slot
 * listeners at most once a second per slot. A held-back change is not lost: the slot stays "changed" and is reported with its
 * latest stack once the second has passed. Other changes (item or count) are reported at once, as before.
 */
@Mixin(value = AbstractContainerMenu.class, remap = false)
public abstract class MixinAbstractContainerMenuSlotListeners
{
	@Unique
	private static final int foreverfixes$INTERVAL_TICKS = 20;

	/** per slot: the owner's tickCount from which the next data-only change may be reported */
	@Unique
	private int[] foreverfixes$nextDataChange;

	@WrapOperation(method = "triggerSlotListeners", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/world/item/ItemStack;matches(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
	private boolean foreverfixes$throttleDataOnlyChanges(ItemStack last, ItemStack current, Operation<Boolean> original, int slotIndex)
	{
		if (original.call(last, current))
		{
			return true;
		}
		if (last.isEmpty() || last.getCount() != current.getCount() || !ItemStack.isSameItem(last, current)
				|| !ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.THROTTLE_DATA_ONLY_SLOT_CHANGES))
		{
			return false;
		}
		final AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
		if (slotIndex < 0 || slotIndex >= menu.slots.size())
		{
			return false;
		}
		final Slot slot = menu.slots.get(slotIndex);
		if (!(slot.container instanceof Inventory inventory))
		{
			return false;
		}
		if (foreverfixes$nextDataChange == null || foreverfixes$nextDataChange.length < menu.slots.size())
		{
			foreverfixes$nextDataChange = new int[menu.slots.size()];
		}
		final int now = inventory.player.tickCount;
		final int next = foreverfixes$nextDataChange[slotIndex];
		// tickCount restarts for a new player entity; a wait longer than the interval means the stored tick is from before that
		if (now >= next || next - now > foreverfixes$INTERVAL_TICKS)
		{
			foreverfixes$nextDataChange[slotIndex] = now + foreverfixes$INTERVAL_TICKS;
			return false;
		}
		return true;
	}
}
