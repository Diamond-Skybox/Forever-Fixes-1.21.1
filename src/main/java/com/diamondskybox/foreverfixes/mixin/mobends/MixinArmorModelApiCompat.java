package com.diamondskybox.foreverfixes.mixin.mobends;

import com.diamondskybox.foreverfixes.ForeverFixes;
import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

/**
 * Mo' Bends 5.6.2 looks up Armor Model API's {@code TrimLayer.resolveSprite(TextureAtlas, ArmorTrim)} inside the one try/catch that
 * decides whether its whole Armor Model API compat is available. Armor Model API 1.1.0 declares
 * {@code resolveSprite(TextureAtlas, ResourceLocation, ResourceLocation)} instead (1.0.0 has no such method), so the lookup throws,
 * the compat stays off, and every API-rendered armor falls back to vanilla {@code _layer_N} textures that these mods do not ship:
 * purple armor. After Mo' Bends' {@code init()} this redoes the lookups with the trim part optional, and answers
 * {@code getTrimSprite} the way the API's own {@code TrimLayer.render} does (Mo' Bends, MIT; Armor Model API, MIT; no code copied).
 * <p>
 * Mo' Bends 6.0 looks the trim part up separately and no longer has {@code resolveSpriteMethod}; this mixin is only applied to
 * older versions (ForeverFixesMixinPlugin.FIXED_IN).
 */
@Mixin(targets = "goblinbob.mobends.compat.ArmorModelApiCompat", remap = false)
public abstract class MixinArmorModelApiCompat
{
	@Shadow private static boolean available;
	@Shadow private static Method getRendererMethod;
	@Shadow private static Method modelMethod;
	@Shadow private static Method configMethod;
	@Shadow private static Method textureMethod;
	@Shadow private static Method applySlotVisibilityMethod;
	@Shadow private static Method layersMethod;
	@Shadow private static Class<?> trimLayerClass;
	@Shadow private static Method resolveSpriteMethod;

	@Shadow
	private static Object getRenderer(ItemStack itemStack)
	{
		throw new AssertionError();
	}

	@Inject(method = "init", at = @At("TAIL"), remap = false, require = 1)
	private static void foreverfixes$repair(CallbackInfo ci)
	{
		if (available || !ForeverFixesConfig.enabled(ForeverFixesConfig.MOBENDS_ARMOR_MODEL_API)
				|| !ModList.get().isLoaded("armor_model_api"))
		{
			return;
		}
		try
		{
			final Class<?> renderers = Class.forName("net.rpg_foundation.armor_api.client.ArmorRenderers");
			final Class<?> renderer = Class.forName("net.rpg_foundation.armor_api.client.GeoArmorRenderer");
			final Class<?> config = Class.forName("net.rpg_foundation.armor_api.client.GeoArmorRenderer$Config");
			final Class<?> model = Class.forName("net.rpg_foundation.armor_api.client.model.GeoArmorModel");
			getRendererMethod = renderers.getMethod("get", Item.class);
			modelMethod = renderer.getMethod("model");
			configMethod = renderer.getMethod("config");
			textureMethod = config.getMethod("texture");
			applySlotVisibilityMethod = model.getMethod("applySlotVisibility", EquipmentSlot.class);
			layersMethod = config.getMethod("layers");
		}
		catch (Throwable t)
		{
			ForeverFixes.LOGGER.warn("Mo' Bends Armor Model API compat: the API's classes look different from 1.1.0, not repairing", t);
			return;
		}
		// Trims are optional: a missing or renamed method must not disable the compat.
		trimLayerClass = null;
		resolveSpriteMethod = null;
		try
		{
			final Class<?> trimLayer = Class.forName("net.rpg_foundation.armor_api.client.layer.TrimLayer");
			for (Method m : trimLayer.getDeclaredMethods())
			{
				if (m.getName().equals("resolveSprite") && m.getParameterCount() >= 2 && m.getParameterTypes()[0] == TextureAtlas.class)
				{
					m.setAccessible(true);
					trimLayerClass = trimLayer;
					resolveSpriteMethod = m;
					break;
				}
			}
		}
		catch (Throwable ignored)
		{
		}
		available = true;
		ForeverFixes.LOGGER.info("Mo' Bends Armor Model API compat repaired (armor trims: {})",
				resolveSpriteMethod == null ? "off" : resolveSpriteMethod.getParameterCount() + "-argument resolveSprite");
	}

	/**
	 * Armor Model API 1.1.0: {@code resolveSprite(atlas, texturePermutations.apply(trim), fallbackTexture)}, as its TrimLayer does.
	 * Mo' Bends' own code expects the old two-argument instance call and is left alone when that is what the API has.
	 */
	@Inject(method = "getTrimSprite", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
	private static void foreverfixes$trimSprite(ItemStack itemStack, ArmorTrim trim, TextureAtlas atlas,
			CallbackInfoReturnable<TextureAtlasSprite> cir)
	{
		if (!available || trimLayerClass == null || resolveSpriteMethod == null || resolveSpriteMethod.getParameterCount() != 3)
		{
			return;
		}
		final Object renderer = getRenderer(itemStack);
		if (renderer == null)
		{
			cir.setReturnValue(null);
			return;
		}
		try
		{
			for (Object layer : (List<?>) layersMethod.invoke(configMethod.invoke(renderer)))
			{
				if (!trimLayerClass.isInstance(layer))
				{
					continue;
				}
				final Field permutations = trimLayerClass.getDeclaredField("texturePermutations");
				final Field fallback = trimLayerClass.getDeclaredField("fallbackTexture");
				permutations.setAccessible(true);
				fallback.setAccessible(true);
				@SuppressWarnings("unchecked")
				final Function<ArmorTrim, ResourceLocation> permutation = (Function<ArmorTrim, ResourceLocation>) permutations.get(layer);
				if (permutation == null)
				{
					cir.setReturnValue(null);
					return;
				}
				cir.setReturnValue((TextureAtlasSprite) resolveSpriteMethod.invoke(null, atlas, permutation.apply(trim), fallback.get(layer)));
				return;
			}
		}
		catch (Throwable t)
		{
			ForeverFixes.LOGGER.debug("Mo' Bends Armor Model API compat: no trim sprite", t);
		}
		cir.setReturnValue(null);
	}
}
