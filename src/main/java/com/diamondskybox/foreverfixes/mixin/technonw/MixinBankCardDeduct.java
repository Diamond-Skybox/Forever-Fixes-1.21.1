package com.diamondskybox.foreverfixes.mixin.technonw;

import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Numismatified Waystones 1.1.0 charges a bank card with BankAccount.deduct(int), which Create: Numismatics 1.1.0 replaced with
 * deduct(int, boolean force), so a card payment failed with a NoSuchMethodError. It now calls deduct(amount, false), the old behaviour.
 */
@Mixin(targets = "com.technophobia.technonw.payment.BankCardPaymentService", remap = false)
public abstract class MixinBankCardDeduct
{
	@Redirect(method = "deduct", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Ldev/ithundxr/createnumismatics/content/backend/BankAccount;deduct(I)Z"))
	private boolean foreverfixes$deduct(BankAccount account, int amount)
	{
		return account.deduct(amount, false);
	}
}
