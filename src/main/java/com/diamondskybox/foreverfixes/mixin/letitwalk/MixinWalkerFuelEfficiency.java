package com.diamondskybox.foreverfixes.mixin.letitwalk;

import com.diamondskybox.foreverfixes.ForeverFixesCommonConfig;
import com.diamondskybox.foreverfixes.WalkerFuels;
import dev.letitwalk.content.WalkerCoreBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Let it Walk!: walkers count and burn every listed fuel, by its efficiency (see {@link WalkerFuels}); hasLava follows lava(). */
@Mixin(targets = "dev.letitwalk.walker.WalkerFuel", remap = false)
public abstract class MixinWalkerFuelEfficiency
{
	@Inject(method = "lava", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private static void foreverfixes$fuelAvailable(WalkerCoreBlockEntity be, CallbackInfoReturnable<Integer> cir)
	{
		if (ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.LETITWALK_OTHER_FUELS))
		{
			cir.setReturnValue(WalkerFuels.available(be));
		}
	}

	@Inject(method = "burn", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private static void foreverfixes$burnFuel(WalkerCoreBlockEntity be, double amount, CallbackInfoReturnable<Boolean> cir)
	{
		if (ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.LETITWALK_OTHER_FUELS))
		{
			cir.setReturnValue(WalkerFuels.burn(be, amount));
		}
	}
}
