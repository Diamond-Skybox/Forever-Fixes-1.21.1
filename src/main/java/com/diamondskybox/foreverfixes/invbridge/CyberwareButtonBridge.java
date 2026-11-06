package com.diamondskybox.foreverfixes.invbridge;

import com.chromeware.client.cyberware.CyberwareScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Chromeware: its inventory button (opens the Cyberware screen), as a Surgery Chair icon in Better Inventory's gutter. */
public final class CyberwareButtonBridge
{
	private static ItemStack icon;

	private CyberwareButtonBridge()
	{
	}

	static ItemStack icon()
	{
		if (icon == null)
		{
			icon = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("chromeware", "surgery_chair")));
		}
		return icon;
	}

	static void open()
	{
		final Minecraft minecraft = Minecraft.getInstance();
		minecraft.setScreen(new CyberwareScreen(minecraft.screen));
	}

	/** Our gutter button replaces Chromeware's own text button on Better Inventory's screen. */
	public static boolean replacesOwnButton(net.minecraft.client.gui.screens.Screen screen)
	{
		return screen != null && "dev.fallingcloud.betterinventory.client.screen.BetterInventoryScreen".equals(screen.getClass().getName())
				&& com.diamondskybox.foreverfixes.ForeverFixesConfig.enabled(com.diamondskybox.foreverfixes.ForeverFixesConfig.BI_MOD_BUTTONS)
				&& com.diamondskybox.foreverfixes.ForeverFixesConfig.BI_MOD_BUTTON_ORDER.get().contains("chromeware");
	}
}
