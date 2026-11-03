package com.diamondskybox.foreverfixes.mixin.sablebscrain;

import com.diamondskybox.foreverfixes.ShipRainCover;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Better Simple Clouds' downpour, its splashes and spray stop on Sable ships (see {@link ShipRainCover}). */
@Mixin(targets = "dev.bettersimpleclouds.rain.DownpourRenderer", remap = false)
public abstract class MixinBscShipRain
{
	@WrapOperation(method = { "tick", "splash", "spawn", "spawnSpray" }, remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/client/multiplayer/ClientLevel;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I"))
	private int foreverfixes$downpourStopsOnShips(ClientLevel level, Heightmap.Types type, int x, int z, Operation<Integer> original)
	{
		return ShipRainCover.height(level, x, z, original.call(level, type, x, z));
	}
}
