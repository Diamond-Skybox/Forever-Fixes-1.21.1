package com.diamondskybox.foreverfixes.invbridge;

import com.diamondskybox.foreverfixes.ForeverFixes;
import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Entries for Better Inventory's compat cells, in the order of {@code betterinventory.modButtons}, for each listed mod that is
 * installed and has its own inventory button switched on. Better Inventory keeps the first four entries, its own first. The
 * entries use Better Inventory's own (private) entry and icon types, built by reflection; icons are the mods' button art.
 */
public final class InventoryButtons
{
	private static final String COMPAT = "dev.fallingcloud.betterinventory.client.CompatButtons";

	private InventoryButtons()
	{
	}

	public static void addTo(List<Object> list)
	{
		if (!ForeverFixesConfig.enabled(ForeverFixesConfig.BI_MOD_BUTTONS))
		{
			return;
		}
		try
		{
			final Class<?> compat = Class.forName(COMPAT);
			final Class<?> icon = Class.forName(COMPAT + "$Icon");
			final Constructor<?> entry = Class.forName(COMPAT + "$Entry").getDeclaredConstructor(String.class, String.class, icon, Runnable.class);
			entry.setAccessible(true);
			final Method sprite = compat.getDeclaredMethod("sprite", ResourceLocation.class, ResourceLocation.class, int.class, int.class);
			final Method texture = compat.getDeclaredMethod("texture", ResourceLocation.class, ResourceLocation.class, int.class, int.class);
			final Method region = compat.getDeclaredMethod("region", ResourceLocation.class, int.class, int.class, int.class, int.class, int.class, int.class);
			sprite.setAccessible(true);
			texture.setAccessible(true);
			region.setAccessible(true);
			for (String mod : ForeverFixesConfig.BI_MOD_BUTTON_ORDER.get())
			{
				if (!ModList.get().isLoaded(mod))
				{
					continue;
				}
				final Object made = switch (mod)
				{
					case "spell_atlas" -> make(entry, mod, "foreverfixes.betterinventory.spell_atlas", SpellAtlasBridge::buttonEnabled,
							sprite.invoke(null, id("spell_atlas", "atlas_button"), id("spell_atlas", "atlas_button_highlighted"), 15, 14), SpellAtlasBridge::open);
					case "fieldguide" -> make(entry, mod, "foreverfixes.betterinventory.fieldguide", FieldGuideBridge::buttonEnabled,
							sprite.invoke(null, id("fieldguide", "widget/fieldguide_inventory_button"), id("fieldguide", "widget/fieldguide_inventory_button_highlighted"), 15, 14),
							FieldGuideBridge::open);
					case "solonion" -> make(entry, mod, "foreverfixes.betterinventory.solonion", FoodBookBridge::buttonEnabled,
							texture.invoke(null, id("solonion", "textures/item/food_book.png"), id("solonion", "textures/item/food_book.png"), 16, 16), FoodBookBridge::open);
					case "doggytalents" -> make(entry, mod, "foreverfixes.betterinventory.doggytalents", DogInventoryBridge::buttonEnabled,
							region.invoke(null, id("doggytalents", "textures/gui/small_widgets.png"), 0, 46, 13, 10, 256, 256), DogInventoryBridge::open);
					default -> null;
				};
				if (made != null)
				{
					list.add(made);
				}
			}
		}
		catch (ReflectiveOperationException | LinkageError | RuntimeException e)
		{
			ForeverFixes.LOGGER.warn("Better Inventory mod buttons are not available ({})", e.toString());
		}
	}

	private static Object make(Constructor<?> entry, String mod, String label, BooleanSupplier enabled, Object icon, Runnable action)
			throws ReflectiveOperationException
	{
		try
		{
			if (!enabled.getAsBoolean())
			{
				return null;
			}
		}
		catch (LinkageError | RuntimeException e)
		{
			ForeverFixes.LOGGER.warn("Better Inventory button for {} skipped: {}", mod, e.toString());
			return null;
		}
		return entry.newInstance(mod, label, icon, action);
	}

	private static ResourceLocation id(String namespace, String path)
	{
		return ResourceLocation.fromNamespaceAndPath(namespace, path);
	}
}
