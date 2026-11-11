package com.diamondskybox.foreverfixes.mixin.schoarfrost;

import com.diamondskybox.foreverfixes.CloudWeather;
import com.pockyl.hoarfrost.client.ClientWeather;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Hoarfrost: its client-side fair-weather snow level (the "fair" local in its tick, from a time schedule or 0 when that is off)
 * is at least the flurry of the Simple Clouds fair-weather cloud over the camera (see {@link CloudWeather#fairSnowAt}).
 */
@Mixin(value = ClientWeather.class, remap = false)
public abstract class MixinHoarfrostClientWeatherFair
{
	@ModifyVariable(method = "tick", remap = false, require = 1, index = 4, at = @At("STORE"))
	private static float foreverfixes$flurryUnderFairClouds(float fair, ClientLevel level, Vec3 camera)
	{
		return Math.max(fair, CloudWeather.fairSnowAt(level, camera.x, camera.y, camera.z));
	}
}
