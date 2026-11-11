package com.diamondskybox.foreverfixes;

import com.diamondskybox.foreverfixes.mixin.simpleclouds.AccessorCloudRegionLife;
import dev.nonamecrackers2.simpleclouds.api.SimpleCloudsAPI;
import dev.nonamecrackers2.simpleclouds.common.cloud.SimpleCloudsConstants;
import dev.nonamecrackers2.simpleclouds.common.cloud.region.CloudRegion;
import dev.nonamecrackers2.simpleclouds.common.cloud.spawning.CloudGenerator;
import dev.nonamecrackers2.simpleclouds.common.world.CloudManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Stationary Simple Clouds formations for Hoarfrost's fair-weather snow spells. During a spell, a player in snowy land with no
 * cloud core overhead gets a fair-weather cloud (see {@link ForeverFixesCommonConfig#FAIR_SNOW_CLOUD_TYPE}) centred on them: speed
 * and acceleration 0 so it ignores the wind, at the bottom of Simple Clouds' region list so any drifting cloud that reaches the
 * player takes over the sky. It shrinks away (Simple Clouds' own end-of-life shrink, over a minute) when the spell ends, the
 * player leaves its core or another cloud covers the player. Stationary clouds found after a restart that no player is under
 * are shrunk away too. Server side; with the flurry mapping in {@link CloudWeather}, the cloud is what makes it snow.
 */
final class FairSnowClouds
{
	/** ticks a placed cloud takes to grow in and to shrink away */
	private static final int GROW_TICKS = 1200;
	private static final int FADE_TICKS = 1200;
	/** a placed cloud's nominal life: it is shrunk away long before */
	private static final int LIFE_TICKS = Integer.MAX_VALUE / 2;
	/** Simple Clouds' edge fade at which rain (and the flurry) starts: that far inside the edge is the "core" (cloud units) */
	private static final float CORE_MARGIN = (1F - SimpleCloudsConstants.RAIN_THRESHOLD) / SimpleCloudsConstants.REGION_EDGE_FADE_FACTOR;

	/** each player's placed cloud */
	private static final Map<UUID, CloudRegion> PLACED = new HashMap<>();

	private FairSnowClouds()
	{
	}

	static void tick(ServerLevel level, CloudManager<?> manager)
	{
		if (!ForeverFixesCommonConfig.FAIR_SNOW_CLOUDS.get() || !manager.isCloudGeneratorActive())
		{
			return;
		}
		final CloudGenerator generator = manager.getCloudGenerator();
		final List<CloudRegion> clouds = generator.getClouds();
		final boolean spell = HoarfrostBridge.fairSpell(level);
		PLACED.values().removeIf(region -> !clouds.contains(region));
		for (ServerPlayer player : level.players())
		{
			if (player.isSpectator())
			{
				continue;
			}
			final float x = (float) player.getX() / SimpleCloudsConstants.CLOUD_SCALE;
			final float z = (float) player.getZ() / SimpleCloudsConstants.CLOUD_SCALE;
			final Pair<CloudRegion, Float> at = CloudRegion.calculateAt(clouds, x, z);
			final CloudRegion over = at.getLeft();
			final boolean inCore = over != null && at.getRight() >= 1F - SimpleCloudsConstants.RAIN_THRESHOLD;
			final CloudRegion placed = PLACED.get(player.getUUID());
			if (placed != null)
			{
				final float dx = placed.getPosX() - x;
				final float dz = placed.getPosZ() - z;
				final boolean inOwnCore = dx * dx + dz * dz <= square(placed.getInitialRadius() - CORE_MARGIN);
				if (!spell || !inOwnCore || (inCore && over != placed))
				{
					shrinkAway(placed);
					PLACED.remove(player.getUUID());
				}
				continue;
			}
			if (spell && !inCore && HoarfrostBridge.snowsAt(level, player.blockPosition()) && noneBeingPlacedOver(x, z))
			{
				place(level, generator, player, x, z);
			}
		}
		// stationary clouds nobody owns (left from before a restart, or whose player left) shrink away
		for (CloudRegion region : clouds)
		{
			if (region.getMaxSpeed() == 0F && region.getAccelerationFactor() == 0F && !PLACED.containsValue(region)
					&& ((AccessorCloudRegionLife) region).foreverfixes$existsForTicks() == LIFE_TICKS)
			{
				shrinkAway(region);
			}
		}
	}

	private static void place(ServerLevel level, CloudGenerator generator, ServerPlayer player, float x, float z)
	{
		final ResourceLocation type = ResourceLocation.tryParse(ForeverFixesCommonConfig.FAIR_SNOW_CLOUD_TYPE.get());
		if (type == null || CloudManager.get(level).getCloudTypeForId(type) == null)
		{
			return;
		}
		final float radius = (float) ForeverFixesCommonConfig.FAIR_SNOW_CLOUD_RADIUS.get() / SimpleCloudsConstants.CLOUD_SCALE;
		final CloudRegion region = (CloudRegion) SimpleCloudsAPI.getApi()
				.createCloudRegion(type, new Vec2(1F, 0F), 0F, 0F, x, z, radius, 0F, 1F, LIFE_TICKS, GROW_TICKS, 0);
		if (generator.addCloud(region, CloudGenerator.Order.BOTTOM))
		{
			PLACED.put(player.getUUID(), region);
		}
	}

	/** no cloud placed for another player is still growing over this spot (its core would cover it once grown) */
	private static boolean noneBeingPlacedOver(float x, float z)
	{
		for (CloudRegion region : PLACED.values())
		{
			final float dx = region.getPosX() - x;
			final float dz = region.getPosZ() - z;
			if (dx * dx + dz * dz <= square(region.getInitialRadius() - CORE_MARGIN))
			{
				return false;
			}
		}
		return true;
	}

	/** the region's remaining life becomes a shrink from its current size over FADE_TICKS (Simple Clouds scales the radius down) */
	private static void shrinkAway(CloudRegion region)
	{
		final AccessorCloudRegionLife life = (AccessorCloudRegionLife) region;
		final int tick = life.foreverfixes$tickCount();
		if (life.foreverfixes$existsForTicks() - tick <= FADE_TICKS)
		{
			return;
		}
		// a cloud still growing keeps its current radius as the size it shrinks from
		final float scale = region.getInitialRadius() > 0F ? region.getRadius() / region.getInitialRadius() : 1F;
		life.foreverfixes$setGrowTicks(tick);
		life.foreverfixes$setExistsForTicks(tick + Math.max(1, Math.round(FADE_TICKS * Math.max(0.05F, scale))));
	}

	private static float square(float v)
	{
		return v * v;
	}
}
