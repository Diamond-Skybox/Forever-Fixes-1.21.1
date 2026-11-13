package com.diamondskybox.foreverfixes.mixin.letitwalk;

import com.diamondskybox.foreverfixes.ForeverFixesCommonConfig;
import com.diamondskybox.foreverfixes.WalkerFuels;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Let it Walk!: the Walker Core's tank takes every listed fuel, not only lava (see {@link WalkerFuels}). Its filter is the lambda lambda$new$0. */
@Mixin(targets = "dev.letitwalk.content.WalkerCoreBlockEntity", remap = false)
public abstract class MixinWalkerCoreFuelTank
{
	@Inject(method = "lambda$new$0(Lnet/neoforged/neoforge/fluids/FluidStack;)Z", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private static void foreverfixes$anyFuel(FluidStack stack, CallbackInfoReturnable<Boolean> cir)
	{
		if (ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.LETITWALK_OTHER_FUELS))
		{
			cir.setReturnValue(WalkerFuels.efficiency(stack) > 0);
		}
	}
}
