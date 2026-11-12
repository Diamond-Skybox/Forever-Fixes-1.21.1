package com.diamondskybox.foreverfixes.mixin.hoarfrost;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/** Hoarfrost's air flow: the gusts and entity wakes queued for its next update, and the weather it samples (see {@link MixinSnowSimulationIdleAir}). */
@Mixin(targets = "com.pockyl.hoarfrost.sim.AirFlow", remap = false)
public interface AccessorAirFlowQueues
{
	@Accessor(value = "blasts", remap = false)
	List<?> foreverfixes$blasts();

	@Accessor(value = "wakes", remap = false)
	List<?> foreverfixes$wakes();

	@Accessor(value = "weather", remap = false)
	com.pockyl.hoarfrost.weather.SnowConditions foreverfixes$weather();

	@Accessor(value = "weather", remap = false)
	void foreverfixes$setWeather(com.pockyl.hoarfrost.weather.SnowConditions weather);
}
