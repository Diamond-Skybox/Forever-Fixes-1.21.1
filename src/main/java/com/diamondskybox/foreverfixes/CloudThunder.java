package com.diamondskybox.foreverfixes;

import dev.nonamecrackers2.simpleclouds.client.renderer.WorldEffects;
import dev.nonamecrackers2.simpleclouds.common.cloud.CloudType;
import dev.nonamecrackers2.simpleclouds.common.world.CloudManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

/**
 * The client level's thunder level from the Simple Clouds cloud at the camera, set right after Simple Clouds sets the rain level
 * from it. Simple Clouds zeroes the thunder level while it runs its own weather; Petrichor's storm sounds and flashes and
 * Hoarfrost's thunder blizzards read it. Client only.
 */
public final class CloudThunder
{
	private CloudThunder()
	{
	}

	public static void afterRenderPost(WorldEffects effects, double camX, double camY, double camZ)
	{
		final ClientLevel level = Minecraft.getInstance().level;
		if (level == null || !ForeverFixesCommonConfig.CLOUD_WEATHER.get())
		{
			return;
		}
		final CloudManager<ClientLevel> manager = CloudManager.get(level);
		if (manager.shouldUseVanillaWeather())
		{
			return;
		}
		final CloudType type = effects.getCloudTypeAtCamera();
		final float thunder = type != null && type.weatherType().includesThunder()
				? Mth.clamp(manager.getRainLevel((float) camX, (float) camY, (float) camZ), 0F, 1F) : 0F;
		level.setThunderLevel(thunder);
	}
}
