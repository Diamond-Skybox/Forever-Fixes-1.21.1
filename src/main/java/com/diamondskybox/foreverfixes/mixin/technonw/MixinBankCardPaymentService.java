package com.diamondskybox.foreverfixes.mixin.technonw;

import net.neoforged.fml.util.thread.EffectiveSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/**
 * The waystone screen (Waystones UI Plus) asks the fare requirement whether the player can pay, on the client. For a bound bank card
 * Numismatified Waystones then reads the card's account from Numismatics' bank, which only exists on the server: Numismatics logged
 * "Bank manager should not be accessed on the client" with a stack trace on every screen update and the balance read as 0, so the
 * teleport button stayed off whenever the fare had to come from the card. On the client a bound card counts as able to pay; the
 * server checks the real balance and charges the card when the teleport runs.
 */
@Mixin(targets = "com.technophobia.technonw.payment.BankCardPaymentService", remap = false)
public abstract class MixinBankCardPaymentService
{
	@Inject(method = "getAvailable", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
	private void foreverfixes$serverDecides(UUID accountId, CallbackInfoReturnable<Integer> cir)
	{
		if (accountId != null && EffectiveSide.get().isClient())
		{
			cir.setReturnValue(Integer.MAX_VALUE);
		}
	}
}
