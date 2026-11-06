package com.diamondskybox.foreverfixes.mixin.printingpress;

import com.diamondskybox.foreverfixes.PrintingLimits;
import com.glowbrick.printingpress.block.entity.block.TypesetterBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The Typesetter refuses books carrying a printing.noTypesetEnchantments enchantment, so they can't become reusable type blocks. */
@Mixin(value = TypesetterBlockEntity.class, remap = false)
public abstract class MixinTypesetterDeny
{
	@Inject(method = "hasRecipe", remap = false, require = 1, at = @At("RETURN"), cancellable = true)
	private void foreverfixes$deny(CallbackInfoReturnable<Boolean> cir)
	{
		if (cir.getReturnValueZ() && PrintingLimits.untypesettable(((TypesetterBlockEntity) (Object) this).itemHandler.getStackInSlot(0)))
		{
			cir.setReturnValue(false);
		}
	}
}
