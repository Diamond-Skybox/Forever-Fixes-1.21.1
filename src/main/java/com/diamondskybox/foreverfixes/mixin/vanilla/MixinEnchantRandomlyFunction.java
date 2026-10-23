package com.diamondskybox.foreverfixes.mixin.vanilla;

import com.diamondskybox.foreverfixes.BlockedEnchantments;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.stream.Stream;

/**
 * enchant_randomly without "options" picks from every registered enchantment, and on a book it skips the compatibility check, so
 * books from such loot tables (Medieval Nether castles, Towns and Towers barrels, Lich tower...) could get internal enchantments
 * such as Dungeons and Taverns' boss markers. The candidates are filtered by enchantments.blocked before the pick.
 */
@Mixin(value = EnchantRandomlyFunction.class, remap = false)
public abstract class MixinEnchantRandomlyFunction
{
	@WrapOperation(method = "run", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Ljava/util/stream/Stream;toList()Ljava/util/List;"))
	private List<Holder<Enchantment>> foreverfixes$skipBlocked(Stream<Holder<Enchantment>> candidates, Operation<List<Holder<Enchantment>>> original)
	{
		return original.call(candidates.filter(enchantment -> !BlockedEnchantments.isBlocked(enchantment)));
	}
}
