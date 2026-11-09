package com.diamondskybox.foreverfixes.mixin.chromewaredoppel;

import com.chromeware.client.sandevistan.AfterimageTimeline;
import com.diamondskybox.foreverfixes.DoppelAfterimages;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Set;
import java.util.UUID;

/** Chromeware opens afterimage sessions only for players (and their rigs); the Dark Doppelganger's active Sandevistan gets one too. */
@Mixin(targets = "com.chromeware.client.sandevistan.SandevistanHistory", remap = false)
public abstract class MixinSandevistanHistoryDoppel
{
	@Shadow(remap = false)
	@Final
	static AfterimageTimeline TIMELINE;

	@ModifyArg(method = "begin", remap = false, require = 1, index = 1,
			at = @At(value = "INVOKE", target = "Lcom/chromeware/client/sandevistan/AfterimageTimeline;update(DLjava/util/Set;)V", remap = false))
	private static Set<UUID> foreverfixes$doppelOwners(Set<UUID> owners)
	{
		DoppelAfterimages.addOwners(owners, TIMELINE);
		return owners;
	}
}
