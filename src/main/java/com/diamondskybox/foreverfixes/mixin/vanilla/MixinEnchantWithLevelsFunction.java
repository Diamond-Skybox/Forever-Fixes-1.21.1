package com.diamondskybox.foreverfixes.mixin.vanilla;

import com.diamondskybox.foreverfixes.BlockedEnchantments;
import com.diamondskybox.foreverfixes.ForeverFixesCommonConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

/**
 * enchant_with_levels turns a book into an enchanted book before it picks enchantments, so when none of its options fits the rolled
 * enchanting power the loot is a blank enchanted book (Dungeons and Taverns' desert ruin and nether keep books: level 1 with only
 * Outreach or Ghasted, whose level I needs exactly power 1, while a level-1 roll on a book is always 2). Such a result gets one
 * enchantment picked from the same options at a random level, as enchant_randomly does; with nothing to pick it stays a plain book.
 */
@Mixin(value = EnchantWithLevelsFunction.class, remap = false)
public abstract class MixinEnchantWithLevelsFunction
{
	@Shadow(remap = false)
	@Final
	private Optional<HolderSet<Enchantment>> options;

	@Inject(method = "run", at = @At("RETURN"), cancellable = true, remap = false, require = 1)
	private void foreverfixes$noBlankBook(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir)
	{
		final ItemStack result = cir.getReturnValue();
		if (!result.is(Items.ENCHANTED_BOOK) || !EnchantmentHelper.getEnchantmentsForCrafting(result).isEmpty()
				|| !ForeverFixesCommonConfig.SPEC.isLoaded() || !ForeverFixesCommonConfig.FIX_BLANK_BOOKS.get())
		{
			return;
		}
		final List<Holder<Enchantment>> candidates = this.options
				.or(() -> context.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getTag(EnchantmentTags.IN_ENCHANTING_TABLE))
				.map(set -> set.stream().filter(enchantment -> !BlockedEnchantments.isBlocked(enchantment)).toList())
				.orElse(List.of());
		if (candidates.isEmpty())
		{
			cir.setReturnValue(result.transmuteCopy(Items.BOOK, result.getCount()));
			return;
		}
		final RandomSource random = context.getRandom();
		final Holder<Enchantment> enchantment = candidates.get(random.nextInt(candidates.size()));
		final int level = Mth.nextInt(random, enchantment.value().getMinLevel(), enchantment.value().getMaxLevel());
		EnchantmentHelper.updateEnchantments(result, mutable -> mutable.upgrade(enchantment, level));
	}
}
