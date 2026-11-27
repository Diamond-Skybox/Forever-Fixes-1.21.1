package com.diamondskybox.foreverfixes.mixin.galosphere;

import com.diamondskybox.foreverfixes.ForeverFixesCommonConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Galosphere: a sparkle's block sensor (once a second) lists every pollinated cluster within 8 blocks and computes a full path to
 * each of them, nearest first, but only keeps the first reachable one, and only while it remembers none: every path after that, or
 * while one is remembered, is thrown away. In a crystal cave that was dozens of pathfinds per sparkle per second, about a third of
 * the server thread. Those paths are no longer computed; the remembered cluster is the same.
 */
@Mixin(targets = "net.orcinus.galosphere.entities.ai.sensors.NearestBlockSensor", remap = false)
public abstract class MixinNearestBlockSensorPaths
{
	@Shadow(remap = false)
	@Final
	private MemoryModuleType<BlockPos> memory;

	@WrapOperation(method = "doTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Mob;)V", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;createPath(Lnet/minecraft/core/BlockPos;I)Lnet/minecraft/world/level/pathfinder/Path;"))
	private Path foreverfixes$noPathOnceRemembered(PathNavigation navigation, BlockPos pos, int accuracy, Operation<Path> original,
			@Local(argsOnly = true) Mob entity)
	{
		if (ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.GALOSPHERE_SENSOR_PATHS) && entity.getBrain().hasMemoryValue(this.memory))
		{
			return null;
		}
		return original.call(navigation, pos, accuracy);
	}
}
