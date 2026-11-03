package com.diamondskybox.foreverfixes.mixin.bscglass;

import com.diamondskybox.foreverfixes.WetGround;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Better Simple Clouds' wet ground stays dry where the biome has no rain, or is listed as dry (see {@link WetGround}). */
@Mixin(targets = "dev.bettersimpleclouds.rain.RainHaze", remap = false)
public abstract class MixinRainHazeDryBiomes
{
	@WrapOperation(method = "updateHeightmap", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/client/multiplayer/ClientLevel;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I"))
	private static int foreverfixes$dryWhereNoRain(ClientLevel level, Heightmap.Types type, int x, int z, Operation<Integer> original)
	{
		return WetGround.height(level, x, z, original.call(level, type, x, z));
	}
}
