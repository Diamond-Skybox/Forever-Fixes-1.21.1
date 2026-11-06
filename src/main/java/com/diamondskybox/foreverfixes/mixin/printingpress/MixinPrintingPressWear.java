package com.diamondskybox.foreverfixes.mixin.printingpress;

import com.diamondskybox.foreverfixes.PrintingLimits;
import com.glowbrick.printingpress.block.entity.block.PrintingPressBlockEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A type block with durability (e.g. a single-use loot typesetting) loses one point per printed book and breaks at zero. */
@Mixin(value = PrintingPressBlockEntity.class, remap = false)
public abstract class MixinPrintingPressWear
{
	@Inject(method = "craftItem", remap = false, require = 1, at = @At("TAIL"))
	private void foreverfixes$wearTypeBlock(CallbackInfo ci)
	{
		final PrintingPressBlockEntity self = (PrintingPressBlockEntity) (Object) this;
		final ItemStack typeBlock = self.itemHandler.getStackInSlot(1);
		final ItemStack left = PrintingLimits.wear(typeBlock);
		if (left != typeBlock || left.isEmpty())
		{
			self.itemHandler.setStackInSlot(1, left);
		}
		self.setChanged();
	}
}
