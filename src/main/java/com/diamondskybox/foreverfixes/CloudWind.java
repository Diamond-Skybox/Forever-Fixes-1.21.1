package com.diamondskybox.foreverfixes;

import com.macatmovie.livingwind.api.WindApi;
import com.macatmovie.livingwind.wind.WindSnapshot;
import dev.nonamecrackers2.simpleclouds.api.common.cloud.region.ScAPICloudRegion;
import dev.nonamecrackers2.simpleclouds.api.common.event.CloudRegionTickEvent;
import dev.nonamecrackers2.simpleclouds.common.world.CloudManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;

/**
 * Simple Clouds' clouds move with Living Wind. Formations get Living Wind's direction (and, optionally, a speed scaled by its
 * strength) through Simple Clouds' per-tick region event, on both sides; the drifting cloud shapes follow it through
 * {@code MixinCloudManagerWindScroll}. Living Wind's heading is a yaw: 0 blows toward +z, 90 toward -x.
 */
public final class CloudWind
{
	/** Living Wind's usual clear-weather strength (Petrichor and Hoarfrost use the same figure), the speed factor's 1x point */
	private static final float USUAL_STRENGTH = 0.34F;
	private static final float MIN_SPEED_FACTOR = 0.5F;
	private static final float MAX_SPEED_FACTOR = 2.0F;

	private CloudWind()
	{
	}

	/** {x, z, speed factor} of the wind in this level, or null when Living Wind has no wind here (or the bridge is off) */
	public static float[] vector(Level level)
	{
		if (!ForeverFixesCommonConfig.CLOUD_WIND.get())
		{
			return null;
		}
		final float[] wind;
		if (level instanceof ServerLevel serverLevel)
		{
			final WindSnapshot snapshot = WindApi.get(serverLevel);
			wind = fromHeading(snapshot.directionDegrees(), snapshot.strength());
		}
		else if (level.isClientSide())
		{
			wind = CloudWindClient.vector();
		}
		else
		{
			wind = null;
		}
		return wind;
	}

	static float[] fromHeading(float degrees, float strength)
	{
		if (!Float.isFinite(degrees) || !Float.isFinite(strength))
		{
			return null;
		}
		final float radians = degrees * Mth.DEG_TO_RAD;
		final float factor = ForeverFixesCommonConfig.CLOUD_WIND_SPEED.get()
				? Mth.clamp(strength / USUAL_STRENGTH, MIN_SPEED_FACTOR, MAX_SPEED_FACTOR) : 1F;
		return new float[] { -Mth.sin(radians), Mth.cos(radians), factor };
	}

	public static void onRegionTick(CloudRegionTickEvent event)
	{
		final float[] wind = vector(event.getLevel());
		if (wind == null)
		{
			return;
		}
		final ScAPICloudRegion region = event.getCloudRegion();
		event.setModifiedMovementDirection(new Vec2(wind[0], wind[1]));
		if (wind[2] != 1F)
		{
			event.setModifiedMaxSpeed(region.getMaxSpeed() * CloudManager.get(event.getLevel()).getCloudSpeed() * wind[2]);
		}
	}
}
