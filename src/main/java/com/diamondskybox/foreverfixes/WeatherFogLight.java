package com.diamondskybox.foreverfixes;

import com.diamondskybox.foreverfixes.mixin.vanilla.AccessorLightTexturePixels;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * Rain and snow fog as bright as the ground it hangs over. The fog colour comes from the sky colour, and Hoarfrost lightens it for its
 * snowy look; the terrain's brightness comes from the lightmap, which night and Simple Clouds' storm darkening (it lowers the sky
 * light under a storm) push down along the game's lighting curve. So a storm's haze, and the far chunks and the LODs fully inside it,
 * glowed against black terrain at night and turned terrain dark a few blocks off by day. Here the fog colour keeps its hue and takes
 * the brightness of the lightmap at full sky light and no block light, times {@link ForeverFixesConfig#WEATHER_FOG_BRIGHTNESS},
 * blended in by the level's rain level (so a fair-weather flurry, which sets none, leaves the fog alone). Runs after every other fog
 * colour listener; the colour also goes to Hoarfrost's Distant Horizons fog. Client only.
 */
public final class WeatherFogLight
{
	private static final boolean HOARFROST = ModList.get().isLoaded("hoarfrost");

	private WeatherFogLight()
	{
	}

	public static void onFogColor(ViewportEvent.ComputeFogColor event)
	{
		final double brightness = ForeverFixesConfig.WEATHER_FOG_BRIGHTNESS.get();
		final Minecraft mc = Minecraft.getInstance();
		final ClientLevel level = mc.level;
		if (brightness <= 0D || level == null || event.getCamera().getFluidInCamera() != FogType.NONE)
		{
			return;
		}
		final float rain = Mth.clamp(level.getRainLevel((float) event.getPartialTick()), 0F, 1F);
		if (rain <= 0F)
		{
			return;
		}
		final NativeImage pixels = ((AccessorLightTexturePixels) mc.gameRenderer.lightTexture()).foreverfixes$lightPixels();
		final int abgr = pixels.getPixelRGBA(0, 15);
		final float light = luma((abgr & 0xFF) / 255F, (abgr >> 8 & 0xFF) / 255F, (abgr >> 16 & 0xFF) / 255F);
		final float r = event.getRed();
		final float g = event.getGreen();
		final float b = event.getBlue();
		final float current = luma(r, g, b);
		if (current <= 1.0E-4F)
		{
			return;
		}
		final float target = (float) Math.min(1D, light * brightness);
		final float scale = Mth.lerp(rain, 1F, target / current);
		final float nr = Math.min(1F, r * scale);
		final float ng = Math.min(1F, g * scale);
		final float nb = Math.min(1F, b * scale);
		event.setRed(nr);
		event.setGreen(ng);
		event.setBlue(nb);
		if (HOARFROST)
		{
			HoarfrostFogColor.set(nr, ng, nb);
		}
	}

	private static float luma(float r, float g, float b)
	{
		return r * 0.3F + g * 0.59F + b * 0.11F;
	}
}
