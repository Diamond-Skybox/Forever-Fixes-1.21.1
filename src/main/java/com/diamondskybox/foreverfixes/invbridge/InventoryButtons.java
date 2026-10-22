package com.diamondskybox.foreverfixes.invbridge;

import com.diamondskybox.foreverfixes.ForeverFixes;
import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Other mods' inventory buttons on Better Inventory's screen. Those mods add their button to the vanilla inventory screen only,
 * which Better Inventory replaces. Better Inventory 2 has no cells for other mods' buttons any more, so they are stacked down the
 * empty gutter between the character and the item grid (x 82..106 of its panel, below its top tab row), in the order of
 * {@code betterinventory.modButtonOrder}, for each listed mod that is installed and has its own inventory button switched on.
 */
final class InventoryButtons
{
	private static final int GUTTER_X = 85;
	private static final int FIRST_Y = 22;
	private static final int PITCH = 20;
	private static final int MAX_BUTTONS = 9;

	private InventoryButtons()
	{
	}

	static void addTo(ScreenEvent.Init.Post event, AbstractContainerScreen<?> screen)
	{
		if (!ForeverFixesConfig.enabled(ForeverFixesConfig.BI_MOD_BUTTONS))
		{
			return;
		}
		int index = 0;
		for (String mod : ForeverFixesConfig.BI_MOD_BUTTON_ORDER.get())
		{
			if (index >= MAX_BUTTONS || !ModList.get().isLoaded(mod))
			{
				continue;
			}
			final int x = screen.getGuiLeft() + GUTTER_X;
			final int y = screen.getGuiTop() + FIRST_Y + index * PITCH;
			final GutterButton button = switch (mod)
			{
				case "spell_atlas" -> make(mod, SpellAtlasBridge::buttonEnabled, () -> new GutterButton(x, y, "foreverfixes.betterinventory.spell_atlas",
						GutterButton.ownSprite(id("spell_atlas", "atlas_button"), id("spell_atlas", "atlas_button_highlighted")), SpellAtlasBridge::open));
				case "fieldguide" -> make(mod, FieldGuideBridge::buttonEnabled, () -> new GutterButton(x, y, "foreverfixes.betterinventory.fieldguide",
						GutterButton.ownSprite(id("fieldguide", "widget/fieldguide_inventory_button"), id("fieldguide", "widget/fieldguide_inventory_button_highlighted")),
						FieldGuideBridge::open));
				case "solonion" -> make(mod, FoodBookBridge::buttonEnabled, () -> new GutterButton(x, y, "foreverfixes.betterinventory.solonion",
						GutterButton.icon(id("solonion", "textures/item/food_book.png"), 0, 0, 16, 16, 16, 16), FoodBookBridge::open));
				case "doggytalents" -> make(mod, DogInventoryBridge::buttonEnabled, () -> new GutterButton(x, y, "foreverfixes.betterinventory.doggytalents",
						GutterButton.icon(id("doggytalents", "textures/gui/small_widgets.png"), 0, 46, 13, 10, 256, 256), DogInventoryBridge::open));
				default -> null;
			};
			if (button != null)
			{
				event.addListener(button);
				index++;
			}
		}
	}

	private static GutterButton make(String mod, BooleanSupplier enabled, Supplier<GutterButton> factory)
	{
		try
		{
			return enabled.getAsBoolean() ? factory.get() : null;
		}
		catch (LinkageError | RuntimeException e)
		{
			ForeverFixes.LOGGER.warn("Better Inventory button for {} skipped: {}", mod, e.toString());
			return null;
		}
	}

	private static ResourceLocation id(String namespace, String path)
	{
		return ResourceLocation.fromNamespaceAndPath(namespace, path);
	}
}
