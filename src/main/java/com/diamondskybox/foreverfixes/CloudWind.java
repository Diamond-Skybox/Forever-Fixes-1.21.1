package com.diamondskybox.foreverfixes;

import com.macatmovie.livingwind.api.WindApi;
import com.macatmovie.livingwind.server.WindSavedData;
import com.macatmovie.livingwind.wind.WindSnapshot;
import dev.nonamecrackers2.simpleclouds.api.common.event.CloudRegionNaturallySpawnEvent;
import dev.nonamecrackers2.simpleclouds.common.cloud.region.CloudRegion;
import dev.nonamecrackers2.simpleclouds.common.world.CloudManager;
import dev.nonamecrackers2.simpleclouds.common.world.ServerCloudManager;
import dev.nonamecrackers2.simpleclouds.common.world.SyncType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Simple Clouds' clouds move with Living Wind, decided by the server only. Every simpleclouds.windUpdateSeconds the server reads
 * Living Wind's prevailing (base) direction and strength, and sets them as the cloud wind: each formation's movement direction
 * (its length carries the speed factor) and Simple Clouds' scroll angle, which is the direction the drifting cloud shapes scroll
 * (see {@code MixinCloudManagerWindScroll}) and what its rain slant reads. Simple Clouds saves both and syncs both (formations every
 * 12 s and on change, the scroll angle every 10 s and on change), so the client moves the clouds exactly as the server does between
 * readings and never reads the live wind; the client and server copies used to drift apart and snap back at each resync. Server
 * side; new formations take the current direction as they spawn.
 */
public final class CloudWind
{
	/** Living Wind's usual clear-weather strength (Petrichor and Hoarfrost use the same figure), the speed factor's 1x point */
	private static final float USUAL_STRENGTH = 0.34F;
	private static final float MIN_SPEED_FACTOR = 0.5F;
	private static final float MAX_SPEED_FACTOR = 2.0F;

	/** per level: the cloud wind {x, z, speed factor} of the last reading and when the next one is due */
	private static final Map<ServerLevel, State> STATES = new WeakHashMap<>();

	private static final class State
	{
		float[] wind;
		long nextReading = Long.MIN_VALUE;
	}

	private CloudWind()
	{
	}

	public static void onLevelTick(LevelTickEvent.Post event)
	{
		if (!(event.getLevel() instanceof ServerLevel level) || !ForeverFixesCommonConfig.CLOUD_WIND.get()
				|| !(CloudManager.get(level) instanceof ServerCloudManager manager))
		{
			return;
		}
		final State state = STATES.computeIfAbsent(level, k -> new State());
		final long now = level.getGameTime();
		if (now < state.nextReading)
		{
			return;
		}
		state.nextReading = now + ForeverFixesCommonConfig.CLOUD_WIND_UPDATE_SECONDS.get() * 20L;
		final float[] wind = read(level);
		if (wind == null)
		{
			return;
		}
		state.wind = wind;
		manager.setScrollAngle((float) Mth.atan2(wind[1], wind[0]));
		manager.queueSync(SyncType.MOVEMENT);
		for (CloudRegion region : manager.getClouds())
		{
			steer(region, wind);
		}
		manager.queueSync(SyncType.CLOUD_FORMATIONS);
	}

	/** A formation spawned: it moves with the current cloud wind from the start. */
	public static void onRegionSpawn(CloudRegionNaturallySpawnEvent event)
	{
		if (event.getLevel() instanceof ServerLevel level && ForeverFixesCommonConfig.CLOUD_WIND.get()
				&& event.getCloudRegion() instanceof CloudRegion region)
		{
			final State state = STATES.get(level);
			if (state != null && state.wind != null)
			{
				steer(region, state.wind);
			}
		}
	}

	/** stationary formations (FairSnowClouds) keep still */
	private static void steer(CloudRegion region, float[] wind)
	{
		if (region.getMaxSpeed() > 0F)
		{
			region.setMovementDirection(new Vec2(wind[0] * wind[2], wind[1] * wind[2]));
		}
	}

	/** {x, z, speed factor} of Living Wind's prevailing wind in this level, or null where it has none */
	private static float[] read(ServerLevel level)
	{
		final WindSnapshot snapshot = WindApi.get(level);
		final float degrees = WindSavedData.get(level).commandBaseDirection();
		final float strength = snapshot.strength();
		if (!Float.isFinite(degrees) || !Float.isFinite(strength))
		{
			return null;
		}
		final float radians = degrees * Mth.DEG_TO_RAD;
		final float factor = ForeverFixesCommonConfig.CLOUD_WIND_SPEED.get()
				? Mth.clamp(strength / USUAL_STRENGTH, MIN_SPEED_FACTOR, MAX_SPEED_FACTOR) : 1F;
		return new float[] { -Mth.sin(radians), Mth.cos(radians), factor };
	}
}
