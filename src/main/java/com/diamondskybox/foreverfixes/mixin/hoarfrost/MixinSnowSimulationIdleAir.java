package com.diamondskybox.foreverfixes.mixin.hoarfrost;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.pockyl.hoarfrost.client.ClientWeather;
import com.pockyl.hoarfrost.sim.AirFlow;
import com.pockyl.hoarfrost.weather.SnowConditions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.BiPredicate;

/**
 * Hoarfrost: no air flow work while there is no snow anywhere near. Its snow simulation updates its air flow every client tick in
 * any dimension with weather: a terrain grid around the camera, rebuilt whenever the camera has moved or the wind has turned (with a
 * trace of the Sable ships overhead for roofs), then eddies and a blur. Only the flakes use it, yet it ran with no snow for thousands
 * of blocks, about 1 ms a tick. While Hoarfrost sees no snowy land in its survey and no snow falling, the update is skipped and the
 * gusts and entity wakes queued for it are dropped (the update would have used and cleared them). Snowy land coming into the survey
 * starts it again before any flake falls; the camera has moved by then, so the grid is rebuilt fresh.
 */
@Mixin(targets = "com.pockyl.hoarfrost.sim.SnowSimulation", remap = false)
public abstract class MixinSnowSimulationIdleAir
{
	@WrapOperation(method = "tick", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lcom/pockyl/hoarfrost/sim/AirFlow;update(Lnet/minecraft/world/level/Level;Lcom/pockyl/hoarfrost/weather/SnowConditions;DDDLjava/util/function/BiPredicate;)V"))
	private void foreverfixes$idleWithoutSnow(AirFlow air, Level level, SnowConditions weather, double x, double y, double z,
			BiPredicate<Level, BlockPos> cold, Operation<Void> original)
	{
		if (ClientWeather.presence() <= 0F && ClientWeather.snow() <= 0F
				&& ForeverFixesConfig.enabled(ForeverFixesConfig.WEATHER_IDLE_WHEN_CLEAR))
		{
			final AccessorAirFlowQueues queues = (AccessorAirFlowQueues) (Object) air;
			queues.foreverfixes$blasts().clear();
			queues.foreverfixes$wakes().clear();
			return;
		}
		original.call(air, level, weather, x, y, z, cold);
	}
}
