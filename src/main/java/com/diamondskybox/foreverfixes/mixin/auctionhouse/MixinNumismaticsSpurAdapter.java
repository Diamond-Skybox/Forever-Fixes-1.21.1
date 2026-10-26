package com.diamondskybox.foreverfixes.mixin.auctionhouse;

import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Create: Auction House 3.2.1 takes money from a player's Numismatics account with BankAccount.deduct(int), which Create: Numismatics
 * 1.1.0 replaced with deduct(int, boolean force): every spend (savings deposits, minigame bets, purchases) failed on the server
 * with a NoSuchMethodError and did nothing. It now calls deduct(amount, false), the old behaviour.
 */
@Mixin(targets = "com.dtmpro621.createauctionhouse.market.platform.NumismaticsSpurAdapter", remap = false)
public abstract class MixinNumismaticsSpurAdapter
{
	@Redirect(method = "withdraw", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Ldev/ithundxr/createnumismatics/content/backend/BankAccount;deduct(I)Z"))
	private boolean foreverfixes$deduct(BankAccount account, int amount)
	{
		return account.deduct(amount, false);
	}
}
