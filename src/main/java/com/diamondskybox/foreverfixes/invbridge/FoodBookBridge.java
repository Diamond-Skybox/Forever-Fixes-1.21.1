package com.diamondskybox.foreverfixes.invbridge;

import net.minecraft.client.Minecraft;
import team.creative.solonion.client.gui.screen.FoodBookScreen;
import team.creative.solonion.common.SOLOnion;

/** Spice of Life: Onion: its inventory button (the food book). */
final class FoodBookBridge
{
	private FoodBookBridge()
	{
	}

	static boolean buttonEnabled()
	{
		return SOLOnion.CONFIG.showButtonInInventory;
	}

	static void open()
	{
		final Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player != null)
		{
			minecraft.setScreen(new FoodBookScreen(minecraft.player));
		}
	}
}
