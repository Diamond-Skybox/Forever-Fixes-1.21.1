package com.diamondskybox.foreverfixes;

import com.pockyl.hoarfrost.client.ClientWeather;
import com.pockyl.hoarfrost.weather.SnowConditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Petrichor's haze for snow as well as rain. Petrichor draws its haze (Atmosphere) from its own rain level, which only counts land
 * where rain falls and, with a hysteresis, drops to nothing unless it rains right where you stand and most of the land around is
 * rainy; in snowy land it never drew, and walking from snow into rain the view cleared before the rain haze faded back in. Here the
 * haze is driven by the real weather's rain level (Simple Clouds' cloud over the camera, or vanilla) times the share of the land
 * around you where any precipitation falls, surveyed like Petrichor does (25 columns 12 blocks apart) but without the hysteresis
 * and eased 0.02 a tick. Its visibility and heaviness blend Petrichor's rain values with Hoarfrost's snow ones by the snowy share.
 * Client only; read from the mixins on Petrichor's Atmosphere.
 */
public final class SnowHaze
{
	private static final float EASE = 0.02F;
	private static float presence;
	private static float presenceTarget;
	private static int ticks;

	private SnowHaze()
	{
	}

	public static boolean enabled()
	{
		return ForeverFixesConfig.enabled(ForeverFixesConfig.SNOW_HAZE);
	}

	public static void onClientTick(ClientTickEvent.Post event)
	{
		final Minecraft mc = Minecraft.getInstance();
		final ClientLevel level = mc.level;
		if (level == null || mc.isPaused())
		{
			return;
		}
		if (ticks++ % 10 == 0)
		{
			final Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
			final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
			int wet = 0;
			for (int i = -2; i <= 2; i++)
			{
				for (int j = -2; j <= 2; j++)
				{
					final int x = Mth.floor(cam.x) + i * 12;
					final int z = Mth.floor(cam.z) + j * 12;
					pos.set(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z), z);
					if (level.getBiome(pos).value().getPrecipitationAt(pos) != Biome.Precipitation.NONE)
					{
						wet++;
					}
				}
			}
			presenceTarget = wet / 25F;
		}
		presence += Mth.clamp(presenceTarget - presence, -EASE, EASE);
	}

	/** the precipitation level the haze is drawn with: Petrichor's own, or rain-or-snow from the real weather, whichever is more */
	public static float precipitation(float petrichorRain)
	{
		if (!enabled())
		{
			return petrichorRain;
		}
		final ClientLevel level = Minecraft.getInstance().level;
		final float weather = level == null ? 0F : Mth.clamp(level.getRainLevel(1.0F), 0F, 1F);
		return Math.max(petrichorRain, weather * presence);
	}

	/** the share of the haze that is snow: Hoarfrost's snowy share of the land within the land where anything falls */
	public static float snowShare()
	{
		if (!enabled() || presence <= 1.0E-3F)
		{
			return 0F;
		}
		return Mth.clamp(ClientWeather.presence() / presence, 0F, 1F);
	}

	/** Petrichor's rain visibility blended with Hoarfrost's snow visibility (the haze density goes as 1 / visibility) */
	public static float visibility(float rainVisibility)
	{
		final float share = snowShare();
		if (share <= 0F)
		{
			return rainVisibility;
		}
		final SnowConditions snow = ClientWeather.conditions();
		final float snowVisibility = Math.max(1F, snow.visibility);
		final float rain = Math.max(1F, rainVisibility);
		return 1F / (share / snowVisibility + (1F - share) / rain);
	}

	/** Petrichor's rain heaviness (overcast, gloom, rain shafts) blended with Hoarfrost's snow cloudiness */
	public static float heaviness(float rainHeaviness)
	{
		final float share = snowShare();
		return share <= 0F ? rainHeaviness : Mth.lerp(share, rainHeaviness, ClientWeather.conditions().clouds);
	}
}
