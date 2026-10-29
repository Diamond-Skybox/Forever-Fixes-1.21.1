package com.diamondskybox.foreverfixes.mixin.hexerei;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.Map;

/**
 * Hexerei's book paint tick walks every page of every book 20 times a second and parses each page's id from its location string
 * every time (spark: ~0.55% of the render thread in ResourceLocation.parse alone). Ids are immutable, so each string is parsed once.
 */
@Mixin(targets = "net.joefoxe.hexerei.data.books.PageDrawingEvents", remap = false)
public abstract class MixinPageDrawingEvents
{
	@Unique
	private static final Map<String, ResourceLocation> foreverfixes$pageIds = new HashMap<>();

	@WrapOperation(method = "clientTickEvent", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/resources/ResourceLocation;parse(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
	private static ResourceLocation foreverfixes$cachedParse(String location, Operation<ResourceLocation> original)
	{
		if (!ForeverFixesConfig.enabled(ForeverFixesConfig.HEXEREI_CACHE_PAGE_IDS))
		{
			return original.call(location);
		}
		ResourceLocation id = foreverfixes$pageIds.get(location);
		if (id == null)
		{
			id = original.call(location);
			foreverfixes$pageIds.put(location, id);
		}
		return id;
	}
}
