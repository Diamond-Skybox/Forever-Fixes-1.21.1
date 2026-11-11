package com.diamondskybox.foreverfixes;

import com.pockyl.petrichor.Config;
import com.pockyl.petrichor.event.WeatherEvents;
import com.pockyl.petrichor.network.WeatherSyncPayload;
import com.pockyl.petrichor.weather.RainType;
import com.pockyl.petrichor.weather.Wetness;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/** Petrichor's classes, kept out of {@link CloudWeather} so it loads without Petrichor. */
final class PetrichorBridge
{
	private PetrichorBridge()
	{
	}

	/** a rain type's ordinal by its id (drizzle, rain, downpour, thunderstorm), or -1 */
	static int typeOrdinal(String id)
	{
		final RainType type = RainType.byId(id);
		if (type == null)
		{
			ForeverFixes.LOGGER.warn("simpleclouds.cloudWeatherTypes: unknown Petrichor rain type '{}'", id);
			return -1;
		}
		return type.ordinal();
	}

	/** the rain type ordinal at a Petrichor rain level (0 = drizzle .. 3 = thunderstorm) */
	static int typeAt(float level)
	{
		return RainType.at(level).ordinal();
	}

	/** Petrichor's wetness step with the cloud's rain level in place of the vanilla one; level -1 = not raining */
	static float stepWetness(float wetness, float rain, float level, boolean day)
	{
		return Wetness.step(wetness, rain, level, day, Config.FILL_SPEED.get(), Config.DRYING_SPEED.get());
	}

	static void send(ServerPlayer player, int typeOrdinal, float level, float wetness)
	{
		PacketDistributor.sendToPlayer(player, new WeatherSyncPayload(typeOrdinal, level, false, wetness));
	}

	/** a real strike within Petrichor's strike radius of the player, where it rains; false if no spot qualified */
	static boolean strikeNear(ServerLevel level, ServerPlayer player)
	{
		return WeatherEvents.strikeNear(level, player.blockPosition()).isPresent();
	}
}
