package com.diamondskybox.foreverfixes.mixin.bscglass;

import net.minecraft.client.renderer.EffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.function.IntSupplier;

/** The textures a post shader was given: the storm fog's depth (see {@link com.diamondskybox.foreverfixes.StormFogFixes}). */
@Mixin(value = EffectInstance.class, remap = false)
public interface AccessorEffectInstance
{
	@Accessor(value = "samplerMap", remap = false)
	Map<String, IntSupplier> foreverfixes$samplers();
}
