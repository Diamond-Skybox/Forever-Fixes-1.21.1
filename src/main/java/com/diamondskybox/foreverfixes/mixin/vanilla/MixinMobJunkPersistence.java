package com.diamondskybox.foreverfixes.mixin.vanilla;

import com.diamondskybox.foreverfixes.ForeverFixes;
import com.diamondskybox.foreverfixes.ForeverFixesCommonConfig;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Hostile mobs don't turn permanent by picking up junk. Vanilla marks any mob that picks up an item as never despawning (so it can't
 * vanish with a player's gear); mobs that keep dying in one place leave rotten flesh and food that the survivors pick up, so they
 * pile up past the mob cap (63 lush zombies under one player, 41 of them permanent). A hostile mob now only turns permanent for an
 * unstackable item (armor, tools, weapons and other gear, the things a player would want back); stackable items it still holds and
 * drops when killed, but it despawns like any other.
 */
@Mixin(Mob.class)
public abstract class MixinMobJunkPersistence
{
	@Shadow private boolean persistenceRequired;

	@WrapMethod(method = "setItemSlotAndDropWhenKilled(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V", require = 1)
	private void foreverfixes$noPersistenceFromJunk(EquipmentSlot slot, ItemStack stack, Operation<Void> original)
	{
		final boolean before = this.persistenceRequired;
		original.call(slot, stack);
		if ((Object) this instanceof Enemy && ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.DEBUG_PERSISTENCE_TRACE))
		{
			ForeverFixes.LOGGER.info("[persistence-trace] equip {} {} on {}: before={} after={} maxStack={} enabled={}", slot, stack, ((Mob) (Object) this).getType(), before,
					this.persistenceRequired, stack.getMaxStackSize(), ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.NO_PERSISTENCE_FROM_JUNK));
		}
		if (!before && (Object) this instanceof Enemy && stack.getMaxStackSize() > 1
				&& ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.NO_PERSISTENCE_FROM_JUNK))
		{
			this.persistenceRequired = false;
		}
	}
}
