package com.diamondskybox.foreverfixes.mixin.hoarfrost;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/** Hoarfrost's air flow: the gusts and entity wakes queued for its next update (see {@link MixinSnowSimulationIdleAir}). */
@Mixin(targets = "com.pockyl.hoarfrost.sim.AirFlow", remap = false)
public interface AccessorAirFlowQueues
{
	@Accessor(value = "blasts", remap = false)
	List<?> foreverfixes$blasts();

	@Accessor(value = "wakes", remap = false)
	List<?> foreverfixes$wakes();
}
