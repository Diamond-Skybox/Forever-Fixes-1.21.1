package com.diamondskybox.foreverfixes.mixin.worldgen;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ChunkGenerator.applyBiomeDecoration looks every placed feature of the chunk's biomes up in the feature index built once per
 * generator (FeatureSorter, one list per decoration step). A feature missing from that index maps to -1 and the chunk fails with
 * "Biome decoration: Index -1 out of bounds", over and over, leaving a hole in the world. In this pack the vanilla sorter finds
 * feature-order cycles (Blooming Nature, Vinery, Wisp Willow, Hexalia, Dynamic Trees adding features in conflicting orders) and
 * Biolith's resilient indexer takes over; depending on the run its result can leave a feature out (Blooming Nature's larch forest
 * in a single-player world, 2026-10-19). Missing features are now skipped for that chunk instead, with a warning: the chunk
 * generates, minus that one decoration.
 */
@Mixin(value = ChunkGenerator.class, remap = false)
public abstract class MixinChunkGeneratorDecoration
{
	@Unique
	private static final Logger foreverfixes$LOGGER = LoggerFactory.getLogger("ForeverFixes");
	@Unique
	private static final AtomicInteger foreverfixes$SKIPPED = new AtomicInteger();

	@WrapOperation(method = "applyBiomeDecoration", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lit/unimi/dsi/fastutil/ints/IntSet;toIntArray()[I"))
	private int[] foreverfixes$dropUnindexedFeatures(IntSet set, Operation<int[]> original, @Local(argsOnly = true) ChunkAccess chunk)
	{
		final int[] indices = original.call(set);
		if (!set.contains(-1))
		{
			return indices;
		}
		final int count = foreverfixes$SKIPPED.incrementAndGet();
		if (count == 1 || count % 500 == 0)
		{
			foreverfixes$LOGGER.warn("Chunk {}: a biome has a placed feature missing from the generator's feature index (feature-order "
					+ "cycle fallback); skipping it instead of failing the chunk ({} occurrence(s) so far)", chunk.getPos(), count);
		}
		return Arrays.stream(indices).filter(index -> index >= 0).toArray();
	}
}
