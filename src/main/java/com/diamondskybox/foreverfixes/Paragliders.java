package com.diamondskybox.foreverfixes;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.contents.ParagliderTags;

/**
 * Paraglider 21.1: whether a stack is an open paraglider, the same test Paraglider's own "paraglider:paragliding" item model
 * property makes (paraglider item tag, then its item capability). The open state is a data component, so it is synced and
 * also true on other players' stacks. Only loaded from the mixins of the paraglider groups, which need Paraglider installed.
 */
public final class Paragliders
{
	private Paragliders()
	{
	}

	public static boolean isParagliding(ItemStack stack)
	{
		return !stack.isEmpty() && stack.is(ParagliderTags.PARAGLIDERS) && ParagliderUtils.getCaps(stack).isParagliding(stack);
	}

	public static boolean isParagliding(LivingEntity entity)
	{
		return isParagliding(entity.getItemInHand(InteractionHand.MAIN_HAND)) || isParagliding(entity.getItemInHand(InteractionHand.OFF_HAND));
	}
}
