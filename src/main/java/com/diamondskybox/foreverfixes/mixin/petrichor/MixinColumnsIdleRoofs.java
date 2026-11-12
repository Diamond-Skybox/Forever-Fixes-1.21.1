package com.diamondskybox.foreverfixes.mixin.petrichor;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.pockyl.petrichor.client.ClientWeather;
import com.pockyl.petrichor.compat.SubLevelRoofs;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Petrichor: no survey of the Sable ships overhead while it is not raining. Its rain columns re-trace every ship above the camera each
 * client tick, so rain stops under a ship's deck; with no rain that is all its column work did, rain or no rain within thousands of
 * blocks. While Petrichor's weather level and rain are both nil the survey is skipped; it runs again on the first tick of rain.
 */
@Mixin(targets = "com.pockyl.petrichor.client.Columns", remap = false)
public abstract class MixinColumnsIdleRoofs
{
	@WrapOperation(method = "begin", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lcom/pockyl/petrichor/compat/SubLevelRoofs;update(Lnet/minecraft/world/level/Level;DDII)V"))
	private void foreverfixes$idleWithoutRain(SubLevelRoofs roofs, Level level, double x, double z, int radius, int tick,
			Operation<Void> original)
	{
		if (ClientWeather.level() <= 0F && ClientWeather.rain() <= 0F
				&& ForeverFixesConfig.enabled(ForeverFixesConfig.WEATHER_IDLE_WHEN_CLEAR))
		{
			return;
		}
		original.call(roofs, level, x, z, radius, tick);
	}
}
