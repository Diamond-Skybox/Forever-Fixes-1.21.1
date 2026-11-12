package com.diamondskybox.foreverfixes;

import com.pockyl.hoarfrost.client.render.SnowFog;

/** Hoarfrost's Distant Horizons fog colour, kept out of {@link WeatherFogLight} so it loads without Hoarfrost. */
final class HoarfrostFogColor
{
	private HoarfrostFogColor()
	{
	}

	static void set(float r, float g, float b)
	{
		SnowFog.setColor(r, g, b);
	}
}
