package com.diamondskybox.foreverfixes.mixin.scpetrichor;

import com.diamondskybox.foreverfixes.CloudWeather;
import com.pockyl.petrichor.event.WeatherEvents;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Petrichor: its level-wide weather sync (and vanilla-thunder extra strikes) stand down while Simple Clouds runs the weather and
 * {@link CloudWeather} syncs each player from the cloud overhead.
 */
@Mixin(value = WeatherEvents.class, remap = false)
public abstract class MixinPetrichorWeatherEvents
{
	@Inject(method = "onLevelTick", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private static void foreverfixes$cloudsRunTheWeather(LevelTickEvent.Post event, CallbackInfo ci)
	{
		if (event.getLevel() instanceof ServerLevel level && CloudWeather.isActive(level))
		{
			ci.cancel();
		}
	}
}
