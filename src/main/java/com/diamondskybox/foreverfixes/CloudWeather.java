package com.diamondskybox.foreverfixes;

import dev.nonamecrackers2.simpleclouds.api.common.cloud.weather.WeatherType;
import dev.nonamecrackers2.simpleclouds.common.cloud.CloudType;
import dev.nonamecrackers2.simpleclouds.common.world.CloudManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Petrichor's rain and Hoarfrost's snow from the Simple Clouds cloud over each player. Both mods only ever read the vanilla
 * weather flags and rain level, which Simple Clouds keeps off while it runs its own localized weather; their clients already take
 * the rain level Simple Clouds writes from the cloud at the camera, but their type (drizzle to thunderstorm, light snow to
 * blizzard) and Petrichor's wetness come from a level-wide server sync that says "no weather". This replaces that sync with a
 * per-player one driven by the cloud type and edge fade over the player (see {@link ForeverFixesCommonConfig#CLOUD_WEATHER_TYPES}),
 * keeps Petrichor's wetness per player, grows Hoarfrost's drifts for players under snow, and turns a share of Simple Clouds'
 * lightning into real Petrichor strikes near players in a thunder cloud's core. Server side; the two mods' own level ticks are
 * cancelled while this is active (see the scpetrichor and schoarfrost mixins).
 */
public final class CloudWeather
{
	private static final boolean PETRICHOR = ModList.get().isLoaded("petrichor");
	private static final boolean HOARFROST = ModList.get().isLoaded("hoarfrost");
	private static final int SYNC_INTERVAL = 40;
	/** a cloud's rain level (0..1) below which it is not raining on the player */
	private static final float RAINING = 0.001F;
	/** the rain level at which the mapped type is reached in full; below it the type is one step lighter */
	private static final float FULL_INTENSITY = 0.6F;
	/** the rain level from which a player counts as standing in a thunder cloud's core (Petrichor strikes) */
	private static final float STORM_CORE = 0.5F;

	private static final Map<UUID, State> STATES = new HashMap<>();
	private static List<? extends String> parsedFrom;
	private static final Map<ResourceLocation, int[]> MAPPING = new HashMap<>();

	/** what the last sync told a player, and that player's wetness */
	private static final class State
	{
		int sentRain = Integer.MIN_VALUE;
		int sentSnow = Integer.MIN_VALUE;
		float wetness;
	}

	private CloudWeather()
	{
	}

	/** Simple Clouds runs its own weather in this level and the bridge is on. */
	public static boolean isActive(Level level)
	{
		return ForeverFixesCommonConfig.CLOUD_WEATHER.get() && !CloudManager.get(level).shouldUseVanillaWeather();
	}

	public static void onLevelTick(LevelTickEvent.Post event)
	{
		if (!(event.getLevel() instanceof ServerLevel level) || !isActive(level) || (!PETRICHOR && !HOARFROST))
		{
			return;
		}
		final CloudManager<?> manager = CloudManager.get(level);
		final long time = level.getGameTime();
		final boolean drifts = HOARFROST && ForeverFixesCommonConfig.CLOUD_DRIFTS.get();
		for (ServerPlayer player : level.players())
		{
			final State state = STATES.computeIfAbsent(player.getUUID(), k -> new State());
			final Pair<CloudType, Float> info = manager.getCloudTypeAtWorldPos((float) player.getX(), (float) player.getZ());
			final CloudType type = info.getLeft();
			final float rain = type.weatherType().includesRain()
					? Mth.clamp(manager.getRainLevel((float) player.getX(), (float) player.getY(), (float) player.getZ()), 0F, 1F) : 0F;
			final boolean raining = rain > RAINING;
			final int[] mapped = mapping(type);
			final boolean sync = time % SYNC_INTERVAL == 0;
			if (PETRICHOR)
			{
				// a downpour cloud starts as rain at its edge and becomes a downpour in its core
				final float target = raining && mapped[0] >= 0 ? Math.max(0F, mapped[0] - (1F - Mth.clamp(rain / FULL_INTENSITY, 0F, 1F))) : 0F;
				state.wetness = PetrichorBridge.stepWetness(state.wetness, rain, raining ? target : -1F, level.isDay());
				final int ordinal = raining && mapped[0] >= 0 ? PetrichorBridge.typeAt(target) : -1;
				if (ordinal != state.sentRain || sync)
				{
					state.sentRain = ordinal;
					PetrichorBridge.send(player, ordinal, target, state.wetness);
				}
			}
			if (HOARFROST)
			{
				final int kind = raining ? mapped[1] : -1;
				if (kind != state.sentSnow || sync)
				{
					state.sentSnow = kind;
					HoarfrostBridge.send(player, kind);
				}
				if (kind >= 0 && drifts)
				{
					HoarfrostBridge.drift(level, player, kind, rain, type.weatherType().includesThunder() ? rain : 0F);
				}
			}
		}
	}

	public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event)
	{
		STATES.remove(event.getEntity().getUUID());
	}

	/**
	 * Simple Clouds decided on a lightning bolt in this level: with the configured chance, and a player standing in a thunder
	 * cloud's core, a real Petrichor strike lands near that player instead. True when that happened (the caller drops its own bolt).
	 */
	public static boolean petrichorStrike(ServerLevel level)
	{
		final double share = ForeverFixesCommonConfig.CLOUD_STRIKE_SHARE.get();
		if (!PETRICHOR || share <= 0D || !isActive(level) || level.random.nextDouble() >= share)
		{
			return false;
		}
		final CloudManager<?> manager = CloudManager.get(level);
		ServerPlayer chosen = null;
		int count = 0;
		for (ServerPlayer player : level.players())
		{
			if (player.isSpectator())
			{
				continue;
			}
			final CloudType type = manager.getCloudTypeAtWorldPos((float) player.getX(), (float) player.getZ()).getLeft();
			if (type.weatherType().includesThunder()
					&& manager.getRainLevel((float) player.getX(), (float) player.getY(), (float) player.getZ()) >= STORM_CORE
					&& level.random.nextInt(++count) == 0)
			{
				chosen = player;
			}
		}
		return chosen != null && PetrichorBridge.strikeNear(level, chosen);
	}

	/** {petrichor rain type ordinal or -1, hoarfrost snow kind ordinal or -1} for a cloud type */
	private static int[] mapping(CloudType type)
	{
		final List<? extends String> list = ForeverFixesCommonConfig.CLOUD_WEATHER_TYPES.get();
		if (list != parsedFrom)
		{
			MAPPING.clear();
			for (String entry : list)
			{
				final int eq = entry.indexOf('=');
				final ResourceLocation id = eq > 0 ? ResourceLocation.tryParse(entry.substring(0, eq).trim()) : null;
				if (id == null)
				{
					ForeverFixes.LOGGER.warn("simpleclouds.cloudWeatherTypes: cannot read '{}'", entry);
					continue;
				}
				final String[] kinds = entry.substring(eq + 1).split(",");
				MAPPING.put(id, new int[] { PETRICHOR ? PetrichorBridge.typeOrdinal(kinds[0].trim()) : -1,
						HOARFROST && kinds.length > 1 ? HoarfrostBridge.kindOrdinal(kinds[1].trim()) : -1 });
			}
			parsedFrom = list;
		}
		final int[] mapped = MAPPING.get(type.id());
		if (mapped != null)
		{
			return mapped;
		}
		final WeatherType weather = type.weatherType();
		if (!weather.includesRain())
		{
			return new int[] { -1, -1 };
		}
		return weather.includesThunder()
				? new int[] { PETRICHOR ? PetrichorBridge.typeOrdinal("thunderstorm") : -1, HOARFROST ? HoarfrostBridge.kindOrdinal("blizzard") : -1 }
				: new int[] { PETRICHOR ? PetrichorBridge.typeOrdinal("rain") : -1, HOARFROST ? HoarfrostBridge.kindOrdinal("snow") : -1 };
	}
}
