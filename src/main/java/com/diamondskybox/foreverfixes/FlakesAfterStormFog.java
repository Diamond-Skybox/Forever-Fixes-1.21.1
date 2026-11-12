package com.diamondskybox.foreverfixes;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.pockyl.hoarfrost.client.SnowClient;
import dev.nonamecrackers2.simpleclouds.client.renderer.SimpleCloudsRenderer;
import dev.nonamecrackers2.simpleclouds.common.config.SimpleCloudsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;

/**
 * Hoarfrost's snowflakes drawn after Simple Clouds' storm fog. Hoarfrost draws them in vanilla's weather pass; Simple Clouds
 * composites its storm fog at the end of the frame over every pixel whose depth reads as sky, and the flakes write no depth, so
 * under a storm cloud they vanished wherever sky was behind them. While Simple Clouds renders here with storm fog on, the weather
 * pass draw is held (its arguments and the fog state of that moment kept) and done right after Simple Clouds' after-level pass,
 * with that fog state put back for the draw. Client only; needs Hoarfrost and Simple Clouds.
 */
public final class FlakesAfterStormFog
{
	private static boolean drawing;
	private static boolean held;
	private static LightTexture lightTexture;
	private static float partialTick;
	private static double camX;
	private static double camY;
	private static double camZ;
	private static float fogStart;
	private static float fogEnd;
	private static FogShape fogShape;
	private static float[] fogColor;

	private FlakesAfterStormFog()
	{
	}

	/** Hoarfrost is about to draw its flakes in the weather pass: true when they are held for after the storm fog instead. */
	public static boolean hold(LightTexture light, float partial, double x, double y, double z)
	{
		if (drawing || !ForeverFixesConfig.enabled(ForeverFixesConfig.HOARFROST_FLAKES_AFTER_STORM_FOG))
		{
			return false;
		}
		final Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || !SimpleCloudsRenderer.canRenderInDimension(mc.level) || SimpleCloudsRenderer.getOptionalInstance().isEmpty()
				|| !SimpleCloudsConfig.CLIENT.renderStormFog.get())
		{
			return false;
		}
		held = true;
		lightTexture = light;
		partialTick = partial;
		camX = x;
		camY = y;
		camZ = z;
		fogStart = RenderSystem.getShaderFogStart();
		fogEnd = RenderSystem.getShaderFogEnd();
		fogShape = RenderSystem.getShaderFogShape();
		fogColor = RenderSystem.getShaderFogColor().clone();
		return true;
	}

	/** Simple Clouds' after-level pass (storm fog included) is done: the held flakes are drawn now. */
	public static void drawHeld()
	{
		if (!held)
		{
			return;
		}
		held = false;
		final float start = RenderSystem.getShaderFogStart();
		final float end = RenderSystem.getShaderFogEnd();
		final FogShape shape = RenderSystem.getShaderFogShape();
		final float[] color = RenderSystem.getShaderFogColor().clone();
		RenderSystem.setShaderFogStart(fogStart);
		RenderSystem.setShaderFogEnd(fogEnd);
		RenderSystem.setShaderFogShape(fogShape);
		RenderSystem.setShaderFogColor(fogColor[0], fogColor[1], fogColor[2], fogColor[3]);
		drawing = true;
		try
		{
			SnowClient.renderSnowfall(lightTexture, partialTick, camX, camY, camZ);
		}
		finally
		{
			drawing = false;
			lightTexture = null;
			RenderSystem.setShaderFogStart(start);
			RenderSystem.setShaderFogEnd(end);
			RenderSystem.setShaderFogShape(shape);
			RenderSystem.setShaderFogColor(color[0], color[1], color[2], color[3]);
		}
	}
}
