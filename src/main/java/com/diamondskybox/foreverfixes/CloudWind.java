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

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Simple Clouds' clouds move with Living Wind. Formations get Living Wind's direction (and, optionally, a speed scaled by its
 * strength) through Simple Clouds' per-tick region event, on both sides; the drifting cloud shapes follow it through
 * {@code MixinCloudManagerWindScroll}. Living Wind's heading is a yaw: 0 blows toward +z, 90 toward -x.
 * <p>
 * The heading is smoothed per level: Living Wind's effective direction wanders by tens of degrees over tens of seconds, and
 * Simple Clouds turns everything that reads its wind direction with it, including its 2D atmospheric cloud layer, which rotates
 * about the camera to face the wind. Clouds here turn toward the wind at about Simple Clouds' own natural rate (its scroll
 * angle advances by the cloud speed times 1e-4 radians a tick), so the sky never visibly swings.
 */
public final class CloudWind
{
	/** Living Wind's usual clear-weather strength (Petrichor and Hoarfrost use the same figure), the speed factor's 1x point */
	private static final float USUAL_STRENGTH = 0.34F;
	private static final float MIN_SPEED_FACTOR = 0.5F;
	private static final float MAX_SPEED_FACTOR = 2.0F;
	/** radians a tick the cloud heading may turn toward the wind: a quarter turn in about two minutes */
	private static final float TURN_RATE = 2.0E-4F;
	/** how fast the speed factor follows the wind strength */
	private static final float STRENGTH_EASE = 0.01F;

	/** per level: the smoothed heading (radians, atan2(z, x) of the wind vector) and speed factor, advanced once a tick */
	private static final Map<Level, Smoothed> SMOOTHED = new WeakHashMap<>();

	private static final class Smoothed
	{
		float heading = Float.NaN;
		float factor = 1F;
		long tick = Long.MIN_VALUE;
	}

	private CloudWind()
	{
	}

	/** {x, z, speed factor} of the (smoothed) wind in this level, or null when Living Wind has no wind here (or the bridge is off) */
	public static float[] vector(Level level)
	{
		if (!ForeverFixesCommonConfig.CLOUD_WIND.get())
		{
			return null;
		}
		final float[] target;
		if (level instanceof ServerLevel serverLevel)
		{
			final WindSnapshot snapshot = WindApi.get(serverLevel);
			target = fromHeading(snapshot.directionDegrees(), snapshot.strength());
		}
		else if (level.isClientSide())
		{
			target = CloudWindClient.vector();
		}
		else
		{
			target = null;
		}
		if (target == null)
		{
			SMOOTHED.remove(level);
			return null;
		}
		final Smoothed smoothed = SMOOTHED.computeIfAbsent(level, k -> new Smoothed());
		final float wanted = (float) Mth.atan2(target[1], target[0]);
		if (Float.isNaN(smoothed.heading))
		{
			smoothed.heading = wanted;
			smoothed.factor = target[2];
		}
		else if (smoothed.tick != level.getGameTime())
		{
			final float delta = Mth.wrapDegrees((wanted - smoothed.heading) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
			smoothed.heading += Mth.clamp(delta, -TURN_RATE, TURN_RATE);
			smoothed.factor += (target[2] - smoothed.factor) * STRENGTH_EASE;
		}
		smoothed.tick = level.getGameTime();
		return new float[] { Mth.cos(smoothed.heading), Mth.sin(smoothed.heading), smoothed.factor };
	}

	/** {x, z, speed factor} straight from a Living Wind heading and strength */
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
		if (region.getMaxSpeed() <= 0F)
		{
			// a stationary cloud (FairSnowClouds) ignores the wind; the event refuses a speed of 0 anyway
			return;
		}
		event.setModifiedMovementDirection(new Vec2(wind[0], wind[1]));
		final float speed = region.getMaxSpeed() * CloudManager.get(event.getLevel()).getCloudSpeed() * wind[2];
		if (wind[2] != 1F && speed > 0F)
		{
			event.setModifiedMaxSpeed(speed);
		}
	}
}
