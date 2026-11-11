package com.diamondskybox.foreverfixes.mixin.schoarfrost;

import com.diamondskybox.foreverfixes.CloudWeather;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.pockyl.hoarfrost.event.SnowEvents;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hoarfrost: its level-wide snow sync and drift growth stand down while Simple Clouds runs the weather and {@link CloudWeather}
 * does both per player from the cloud overhead; its cancelling of natural lightning near players standing in snow, which asks
 * the vanilla rain flag, applies whenever Simple Clouds runs the weather (its own per-player snow check still decides).
 */
@Mixin(value = SnowEvents.class, remap = false)
public abstract class MixinHoarfrostSnowEvents
{
	@Inject(method = "onLevelTick", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private static void foreverfixes$cloudsRunTheWeather(LevelTickEvent.Post event, CallbackInfo ci)
	{
		if (event.getLevel() instanceof ServerLevel level && CloudWeather.isActive(level))
		{
			ci.cancel();
		}
	}

	@WrapOperation(method = "onEntityJoin", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/server/level/ServerLevel;isRaining()Z"))
	private static boolean foreverfixes$rainingUnderClouds(ServerLevel level, Operation<Boolean> original)
	{
		return original.call(level) || CloudWeather.isActive(level);
	}
}
