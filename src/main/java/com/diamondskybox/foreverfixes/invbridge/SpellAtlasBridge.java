package com.diamondskybox.foreverfixes.invbridge;

import dev.kubas.spellatlas.client.AtlasScreen;
import dev.kubas.spellatlas.client.InventoryPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import java.lang.reflect.Method;

/** Spell Atlas: an inventory button for its atlas, and its inventory panel (drawn and clicked against a stand-in inventory screen). */
final class SpellAtlasBridge
{
	private static Method render;
	private static Method renderBottom;

	private SpellAtlasBridge()
	{
	}

	/** Spell Atlas 2.0 has no inventory button of its own (nor a setting for one), only its keybind: the gutter button stays. */
	static boolean buttonEnabled()
	{
		return true;
	}

	static void open()
	{
		final Minecraft minecraft = Minecraft.getInstance();
		minecraft.setScreen(new AtlasScreen(minecraft.screen));
	}

	/** Draws the panel the way Spell Atlas does after an inventory screen's render; its own methods are package-private. */
	static void renderPanel(InventoryScreen standIn, GuiGraphics graphics, int mouseX, int mouseY) throws ReflectiveOperationException
	{
		if (render == null)
		{
			render = InventoryPanel.class.getDeclaredMethod("render", InventoryScreen.class, GuiGraphics.class, int.class, int.class);
			renderBottom = InventoryPanel.class.getDeclaredMethod("renderBottom", InventoryScreen.class, GuiGraphics.class, int.class, int.class);
			render.setAccessible(true);
			renderBottom.setAccessible(true);
		}
		render.invoke(null, standIn, graphics, mouseX, mouseY);
		renderBottom.invoke(null, standIn, graphics, mouseX, mouseY);
	}

	static boolean clickPanel(InventoryScreen standIn, double mouseX, double mouseY)
	{
		return InventoryPanel.click(standIn, mouseX, mouseY);
	}
}
