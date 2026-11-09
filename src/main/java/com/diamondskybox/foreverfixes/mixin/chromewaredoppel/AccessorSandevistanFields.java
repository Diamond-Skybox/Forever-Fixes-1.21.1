package com.diamondskybox.foreverfixes.mixin.chromewaredoppel;

import com.chromeware.sandevistan.Sandevistan;
import com.chromeware.sandevistan.TimeField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

/** Chromeware's active time fields by owner; the Dark Doppelganger's Sandevistan adds its own field here (see DoppelChrome). */
@Mixin(value = Sandevistan.class, remap = false)
public interface AccessorSandevistanFields
{
	@Accessor(value = "FIELDS", remap = false)
	static Map<UUID, TimeField> foreverfixes$fields()
	{
		throw new AssertionError();
	}
}
