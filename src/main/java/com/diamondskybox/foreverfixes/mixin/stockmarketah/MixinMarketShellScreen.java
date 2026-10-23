package com.diamondskybox.foreverfixes.mixin.stockmarketah;

import com.diamondskybox.foreverfixes.AuctionHouseTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** An Auction House screen opened from the Stock Market tab returns to Stock Market when it is closed (see AuctionHouseTab). */
@Mixin(targets = "com.dtmpro621.createauctionhouse.market.ui.MarketShellScreen", remap = false)
public abstract class MixinMarketShellScreen
{
	@Unique
	private boolean foreverfixes$fromStockMarket;

	@Inject(method = "<init>", at = @At("TAIL"), remap = false, require = 1)
	private void foreverfixes$rememberOrigin(CallbackInfo ci)
	{
		this.foreverfixes$fromStockMarket = AuctionHouseTab.claimRequest();
	}

	@Inject(method = "removed", at = @At("TAIL"), remap = false, require = 1)
	private void foreverfixes$backToStockMarket(CallbackInfo ci)
	{
		if (this.foreverfixes$fromStockMarket)
		{
			this.foreverfixes$fromStockMarket = false;
			AuctionHouseTab.returnToStockMarket();
		}
	}
}
