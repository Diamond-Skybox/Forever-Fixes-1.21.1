package com.diamondskybox.foreverfixes;

import by.deokma.stockmarket.neoforge.client.StockMarketScreen;
import net.minecraft.client.Minecraft;

/**
 * The Auction House tab on Create: Stock Market's screen (MixinStockMarketScreen) opens Create: Auction House through its /ah
 * command, which the server answers by opening the Auction House screen. An Auction House screen that opens within a few seconds
 * of the click remembers it (MixinMarketShellScreen) and, when closed to the game, brings the Stock Market screen back.
 */
public final class AuctionHouseTab
{
	private static final long OPEN_WINDOW_MS = 5000L;
	private static long requestedAt;

	private AuctionHouseTab()
	{
	}

	public static void open()
	{
		final Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null)
		{
			return;
		}
		requestedAt = System.currentTimeMillis();
		minecraft.player.connection.sendCommand("ah");
	}

	/** Called when an Auction House screen is created; true when it was opened from the tab. Consumes the request. */
	public static boolean claimRequest()
	{
		final boolean fromTab = System.currentTimeMillis() - requestedAt < OPEN_WINDOW_MS;
		requestedAt = 0L;
		return fromTab;
	}

	/** Called when an Auction House screen opened from the tab is removed; reopens Stock Market if it was closed to the game. */
	public static void returnToStockMarket()
	{
		final Minecraft minecraft = Minecraft.getInstance();
		minecraft.tell(() -> {
			if (minecraft.screen == null && minecraft.player != null)
			{
				minecraft.setScreen(new StockMarketScreen());
			}
		});
	}
}
