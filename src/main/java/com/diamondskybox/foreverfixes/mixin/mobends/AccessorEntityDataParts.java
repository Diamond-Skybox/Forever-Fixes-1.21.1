package com.diamondskybox.foreverfixes.mixin.mobends;

import goblinbob.mobends.core.data.EntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashMap;

/** Mo' Bends' name -> animated part map of an entity's animation data (read by the BendsRecorder command). */
@Mixin(value = EntityData.class, remap = false)
public interface AccessorEntityDataParts
{
	@Accessor(value = "nameToPartMap", remap = false)
	HashMap<String, Object> foreverfixes$parts();
}
