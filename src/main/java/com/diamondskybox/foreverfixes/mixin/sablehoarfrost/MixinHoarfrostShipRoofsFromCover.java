package com.diamondskybox.foreverfixes.mixin.sablehoarfrost;

import com.diamondskybox.foreverfixes.ShipRainCover;
import com.pockyl.hoarfrost.compat.SubLevelRoofs;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hoarfrost: its shelter under Sable ships comes from Forever Fixes' ship cover (see {@link ShipRainCover}) instead of its own survey.
 * Hoarfrost traced every ship around the camera again every tick the ship moved (and once a second while it sat still): down each
 * world column under the ship, block by block through the ship's height. A ship nudged by the wind, even one floating on water,
 * never sits still, so every ship in view was traced every tick. The cover reads a ship's column tops once, again only when a block
 * on it changes, and lays them over the world through the ship's pose when it moves; here those columns, with the block on top and
 * its exact height, fill Hoarfrost's roof map. Its air flow asks whether a point is inside a ship block; that
 * is answered from the ships the cover keeps, without the traces.
 */
@Mixin(targets = "com.pockyl.hoarfrost.compat.SableRoofs", remap = false)
public abstract class MixinHoarfrostShipRoofsFromCover
{
	@Unique
	private static final Long2ObjectOpenHashMap<SubLevelRoofs.Roof> foreverfixes$roofs = new Long2ObjectOpenHashMap<>();
	@Unique
	private static int foreverfixes$roofsVersion = Integer.MIN_VALUE;

	@Inject(method = "survey", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private void foreverfixes$surveyFromCover(Level level, int fromX, int fromZ, int toX, int toZ, Long2ObjectMap<SubLevelRoofs.Roof> out,
			CallbackInfo ci)
	{
		if (!ShipRainCover.serves(level))
		{
			return;
		}
		ShipRainCover.demand();
		if (foreverfixes$roofsVersion != ShipRainCover.coverVersion())
		{
			foreverfixes$roofsVersion = ShipRainCover.coverVersion();
			foreverfixes$roofs.clear();
			ShipRainCover.forEachRoof((key, top, state) -> foreverfixes$roofs.put(key, new SubLevelRoofs.Roof(top, state)));
		}
		for (Long2ObjectMap.Entry<SubLevelRoofs.Roof> entry : foreverfixes$roofs.long2ObjectEntrySet())
		{
			final long key = entry.getLongKey();
			final int x = ShipRainCover.keyX(key);
			final int z = ShipRainCover.keyZ(key);
			if (x >= fromX && x <= toX && z >= fromZ && z <= toZ)
			{
				out.put(key, entry.getValue());
			}
		}
		ci.cancel();
	}

	@Inject(method = "solid", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private void foreverfixes$solidFromCover(Level level, double x, double y, double z, CallbackInfoReturnable<Boolean> cir)
	{
		if (ShipRainCover.serves(level))
		{
			cir.setReturnValue(ShipRainCover.solid(level, x, y, z));
		}
	}
}
