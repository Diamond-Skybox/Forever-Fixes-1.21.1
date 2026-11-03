package com.diamondskybox.foreverfixes.mixin.sablerain;

import com.diamondskybox.foreverfixes.ShipRainCover;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Vanilla rain and its splashes stop on Sable ships (see {@link ShipRainCover}). */
@Mixin(value = LevelRenderer.class, remap = false)
public abstract class MixinLevelRendererShipRain
{
	@WrapOperation(method = "renderSnowAndRain", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/world/level/Level;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I"))
	private int foreverfixes$rainStopsOnShips(Level level, Heightmap.Types type, int x, int z, Operation<Integer> original)
	{
		return ShipRainCover.height(level, x, z, original.call(level, type, x, z));
	}

	@WrapOperation(method = "tickRain", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/world/level/LevelReader;getHeightmapPos(Lnet/minecraft/world/level/levelgen/Heightmap$Types;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"))
	private BlockPos foreverfixes$splashesOnShips(LevelReader reader, Heightmap.Types type, BlockPos pos, Operation<BlockPos> original)
	{
		final BlockPos ground = original.call(reader, type, pos);
		if (!(reader instanceof Level level))
		{
			return ground;
		}
		final int y = ShipRainCover.height(level, ground.getX(), ground.getZ(), ground.getY());
		return y == ground.getY() ? ground : new BlockPos(ground.getX(), y, ground.getZ());
	}
}
