package com.diamondskybox.foreverfixes.invbridge;

import com.diamondskybox.foreverfixes.ForeverFixes;
import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import com.diamondskybox.foreverfixes.mixin.vanilla.AccessorAbstractContainerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Inventory-screen elements of other mods that are drawn on the vanilla inventory screen itself, brought to Better Inventory's
 * screen, which replaces it: Spell Atlas's inventory panel and NTGL's chassis tabs. Spell Atlas places its panel from the
 * inventory screen's panel geometry, so it gets a stand-in vanilla inventory screen (never opened or initialised) that carries
 * Better Inventory's geometry. The inventory buttons go into Better Inventory's compat cells instead ({@link InventoryButtons}).
 */
public final class InventoryBridge
{
	private static final String SCREEN = "dev.fallingcloud.betterinventory.client.screen.BetterInventoryScreen";

	private static boolean spellAtlas;
	private static boolean chassis;
	private static boolean spellAtlasFailed;
	private static boolean chassisFailed;
	private static InventoryScreen standIn;

	private InventoryBridge()
	{
	}

	public static void register()
	{
		spellAtlas = ModList.get().isLoaded("spell_atlas");
		chassis = ModList.get().isLoaded("ntgl");
		if (spellAtlas || chassis)
		{
			NeoForge.EVENT_BUS.addListener(InventoryBridge::onBackground);
			NeoForge.EVENT_BUS.addListener(InventoryBridge::onRender);
			NeoForge.EVENT_BUS.addListener(InventoryBridge::onClick);
		}
	}

	private static boolean betterInventory(Screen screen)
	{
		return screen != null && SCREEN.equals(screen.getClass().getName()) && ForeverFixesConfig.enabled(ForeverFixesConfig.BI_MOD_PANELS);
	}

	private static void onBackground(ContainerScreenEvent.Render.Background event)
	{
		final AbstractContainerScreen<?> screen = event.getContainerScreen();
		if (chassis && !chassisFailed && betterInventory(screen))
		{
			try
			{
				if (ChassisTabsBridge.active())
				{
					ChassisTabsBridge.renderBackground(event.getGuiGraphics(), screen.getGuiLeft(), screen.getGuiTop());
				}
			}
			catch (LinkageError | RuntimeException e)
			{
				chassisFailed = true;
				ForeverFixes.LOGGER.warn("NTGL chassis tabs on Better Inventory disabled: {}", e.toString());
			}
		}
	}

	private static void onRender(ScreenEvent.Render.Post event)
	{
		if (!betterInventory(event.getScreen()))
		{
			return;
		}
		final AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) event.getScreen();
		if (chassis && !chassisFailed)
		{
			try
			{
				if (ChassisTabsBridge.active())
				{
					ChassisTabsBridge.renderIcons(event.getGuiGraphics(), screen.getGuiLeft(), screen.getGuiTop());
				}
			}
			catch (LinkageError | RuntimeException e)
			{
				chassisFailed = true;
				ForeverFixes.LOGGER.warn("NTGL chassis tabs on Better Inventory disabled: {}", e.toString());
			}
		}
		if (spellAtlas && !spellAtlasFailed)
		{
			try
			{
				SpellAtlasBridge.renderPanel(standIn(screen), event.getGuiGraphics(), event.getMouseX(), event.getMouseY());
			}
			catch (ReflectiveOperationException | LinkageError | RuntimeException e)
			{
				spellAtlasFailed = true;
				ForeverFixes.LOGGER.warn("Spell Atlas panel on Better Inventory disabled: {}", e.toString());
			}
		}
	}

	private static void onClick(ScreenEvent.MouseButtonPressed.Pre event)
	{
		if (event.getButton() != 0 || !betterInventory(event.getScreen()))
		{
			return;
		}
		final AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) event.getScreen();
		try
		{
			if (chassis && !chassisFailed && ChassisTabsBridge.active()
					&& ChassisTabsBridge.click(screen.getGuiLeft(), screen.getGuiTop(), event.getMouseX(), event.getMouseY()))
			{
				event.setCanceled(true);
				return;
			}
			if (spellAtlas && !spellAtlasFailed && SpellAtlasBridge.clickPanel(standIn(screen), event.getMouseX(), event.getMouseY()))
			{
				event.setCanceled(true);
			}
		}
		catch (LinkageError | RuntimeException e)
		{
			ForeverFixes.LOGGER.warn("Better Inventory panel click failed: {}", e.toString());
		}
	}

	/** A vanilla inventory screen object with the given screen's size and panel geometry, for code that only reads those. */
	private static InventoryScreen standIn(AbstractContainerScreen<?> screen)
	{
		final Minecraft minecraft = Minecraft.getInstance();
		if (standIn == null || standIn.getMenu() != minecraft.player.inventoryMenu)
		{
			standIn = new InventoryScreen(minecraft.player);
		}
		standIn.width = screen.width;
		standIn.height = screen.height;
		final AccessorAbstractContainerScreen geometry = (AccessorAbstractContainerScreen) standIn;
		geometry.foreverfixes$setLeftPos(screen.getGuiLeft());
		geometry.foreverfixes$setTopPos(screen.getGuiTop());
		geometry.foreverfixes$setImageWidth(screen.getXSize());
		geometry.foreverfixes$setImageHeight(screen.getYSize());
		return standIn;
	}
}
