package com.diamondskybox.foreverfixes.mixin.mcawerewolf;

import com.diamondskybox.foreverfixes.McaWerewolfForm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/**
 * Werewolves + MCA: a player in a transformed werewolf form is not drawn by MCA, so the werewolf model shows (see
 * {@link McaWerewolfForm}). Covers both MCA looks: villager body and skin genetics.
 */
@Mixin(targets = "net.conczin.mca.MCAClient", remap = false)
public abstract class MixinMcaClientWerewolf
{
	@Inject(method = "useVillagerRenderer", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private static void foreverfixes$werewolfVillagerLook(UUID uuid, CallbackInfoReturnable<Boolean> cir)
	{
		if (McaWerewolfForm.transformed(uuid))
		{
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "useGeneticsRenderer", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private static void foreverfixes$werewolfGeneticsLook(UUID uuid, CallbackInfoReturnable<Boolean> cir)
	{
		if (McaWerewolfForm.transformed(uuid))
		{
			cir.setReturnValue(false);
		}
	}
}
