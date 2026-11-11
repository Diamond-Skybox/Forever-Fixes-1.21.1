package com.diamondskybox.foreverfixes.mixin.simpleclouds;

import dev.nonamecrackers2.simpleclouds.common.cloud.region.CloudRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Simple Clouds: a cloud formation's age and life, so Forever Fixes can shrink one away early (see {@code FairSnowClouds}). */
@Mixin(value = CloudRegion.class, remap = false)
public interface AccessorCloudRegionLife
{
	@Accessor(value = "tickCount", remap = false)
	int foreverfixes$tickCount();

	@Accessor(value = "existsForTicks", remap = false)
	int foreverfixes$existsForTicks();

	@Accessor(value = "existsForTicks", remap = false)
	void foreverfixes$setExistsForTicks(int ticks);

	@Accessor(value = "growTicks", remap = false)
	void foreverfixes$setGrowTicks(int ticks);
}
