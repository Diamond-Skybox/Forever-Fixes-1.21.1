package com.diamondskybox.foreverfixes.invbridge;

import doggytalents.client.screen.widget.DogInventoryButton;
import doggytalents.common.config.ConfigHandler;
import net.minecraft.client.Minecraft;

/** Doggy Talents Next: its inventory button (the inventories of nearby pack puppies), pressed through its own button class. */
final class DogInventoryBridge
{
	private DogInventoryBridge()
	{
	}

	static boolean buttonEnabled()
	{
		return ConfigHandler.CLIENT.DOG_INV_BUTTON_IN_INV.get();
	}

	static void open()
	{
		new DogInventoryButton(0, 0, Minecraft.getInstance().screen).onPress();
	}
}
