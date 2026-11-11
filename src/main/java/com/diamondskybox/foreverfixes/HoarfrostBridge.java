package com.diamondskybox.foreverfixes;

import com.pockyl.hoarfrost.Config;
import com.pockyl.hoarfrost.compat.LivingWind;
import com.pockyl.hoarfrost.network.SnowSyncPayload;
import com.pockyl.hoarfrost.weather.SnowConditions;
import com.pockyl.hoarfrost.weather.SnowKind;
import com.pockyl.hoarfrost.world.DriftBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.WeakHashMap;

/** Hoarfrost's classes, kept out of {@link CloudWeather} so it loads without Hoarfrost. */
final class HoarfrostBridge
{
	/** Hoarfrost's drift constants (SnowEvents): wind below which no drift grows, the range over which it reaches full strength */
	private static final float DRIFT_WIND = 0.15F;
	private static final float DRIFT_WIND_RANGE = 0.25F;
	private static final int DRIFT_RANGE = 40;
	private static final float DRIFT_ATTEMPTS = 8.0F;

	/** per level: the snow wind Hoarfrost would have advanced itself, advanced here once per tick */
	private static final Map<ServerLevel, Wind> WIND = new WeakHashMap<>();
	private static final float[] OUTER_WIND = new float[3];

	private static final class Wind
	{
		final SnowConditions conditions = new SnowConditions();
		long tick = Long.MIN_VALUE;
	}

	private HoarfrostBridge()
	{
	}

	/** a snow kind's ordinal by its id (light_snow .. heavy_blizzard), or -1 */
	static int kindOrdinal(String id)
	{
		final SnowKind kind = SnowKind.byId(id);
		if (kind == null)
		{
			ForeverFixes.LOGGER.warn("simpleclouds.cloudWeatherTypes: unknown Hoarfrost snow kind '{}'", id);
			return -1;
		}
		return kind.ordinal();
	}

	static void send(ServerPlayer player, int kindOrdinal)
	{
		PacketDistributor.sendToPlayer(player, new SnowSyncPayload(kindOrdinal));
	}

	/**
	 * Hoarfrost's drift growth (SnowEvents.buildDrifts) for one player: its snow wind blended toward the kind over this player and
	 * advanced with the cloud's rain level, then a few drift attempts within 40 blocks. Drifts only grow where the climate snows.
	 */
	static void drift(ServerLevel level, ServerPlayer player, int kindOrdinal, float rain, float thunder)
	{
		final SnowKind kind = SnowKind.byOrdinal(kindOrdinal);
		if (kind == null || !Config.<Boolean>value(Config.DRIFTS) || player.isSpectator())
		{
			return;
		}
		final Wind wind = WIND.computeIfAbsent(level, k -> new Wind());
		final SnowConditions conditions = wind.conditions;
		if (wind.tick != level.getGameTime())
		{
			wind.tick = level.getGameTime();
			conditions.blendTo(kind, 0.006F);
			final boolean outer = LivingWind.readServer(level, rain, thunder, OUTER_WIND);
			conditions.advanceWind(level.getGameTime(), rain, outer ? OUTER_WIND : null);
		}
		final float strength = Mth.clamp((conditions.windSpeed - DRIFT_WIND) / DRIFT_WIND_RANGE, 0F, 1F)
				* Config.<Double>value(Config.DRIFT_SPEED).floatValue();
		if (strength <= 0F)
		{
			return;
		}
		final int maxLayers = Config.<Integer>value(Config.DRIFT_MAX_LAYERS);
		final float attempts = DRIFT_ATTEMPTS * strength;
		final int n = (int) attempts + (level.random.nextFloat() < attempts - (int) attempts ? 1 : 0);
		for (int k = 0; k < n; k++)
		{
			final int x = player.getBlockX() + level.random.nextInt(DRIFT_RANGE * 2 + 1) - DRIFT_RANGE;
			final int z = player.getBlockZ() + level.random.nextInt(DRIFT_RANGE * 2 + 1) - DRIFT_RANGE;
			DriftBuilder.grow(level, x, z, conditions.windDirX, conditions.windDirZ, maxLayers, false);
		}
	}
}
