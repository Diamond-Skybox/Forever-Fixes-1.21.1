package com.diamondskybox.foreverfixes.mixin.sablebscrain;

import com.diamondskybox.foreverfixes.ShipRainCover;
import com.diamondskybox.foreverfixes.ShipWetGround;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Better Simple Clouds' wet ground: the ground under a Sable ship (and its lower decks) is not open sky (see {@link ShipRainCover}). */
@Mixin(targets = "dev.bettersimpleclouds.rain.RainHaze", remap = false)
public abstract class MixinBscRainHazeShips
{
	@WrapOperation(method = "updateHeightmap", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/client/multiplayer/ClientLevel;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I"))
	private static int foreverfixes$wetGroundUnderShips(ClientLevel level, Heightmap.Types type, int x, int z, Operation<Integer> original)
	{
		final int height = original.call(level, type, x, z);
		// with the ships handed to the shader, the shader covers the ground under them on the frame it is drawn
		return ShipWetGround.active() ? height : ShipRainCover.height(level, x, z, height);
	}
}
