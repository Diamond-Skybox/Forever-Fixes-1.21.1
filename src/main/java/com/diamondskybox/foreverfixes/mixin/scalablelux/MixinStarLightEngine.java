package com.diamondskybox.foreverfixes.mixin.scalablelux;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import ca.spottedleaf.starlight.common.light.StarLightEngine;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * ScalableLux (Starlight) handles a block change by centring its 5x5 chunk caches on the changed chunk, fetching that chunk from
 * the cache and indexing its emptiness map by the chunk's own position. While a world is being left, the client chunk cache
 * hands out Minecraft's empty placeholder chunk (always at 0, 0) for chunks already unloaded; for a Sable sub-level chunk about
 * 20 million blocks out that index was millions off and the game crashed ("Index -7680372 out of bounds for length 25"). A chunk
 * whose position is not the one asked for is now treated as missing, which ScalableLux already handles by skipping the update.
 */
@Mixin(targets = "ca.spottedleaf.starlight.common.light.StarLightEngine", remap = false)
public abstract class MixinStarLightEngine
{
	@WrapOperation(method = { "blocksChangedInChunk", "handleEmptySectionChanges" }, remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lca/spottedleaf/starlight/common/light/StarLightEngine;getChunkInCache(II)Lnet/minecraft/world/level/chunk/ChunkAccess;"))
	private ChunkAccess foreverfixes$onlyTheChunkAskedFor(StarLightEngine engine, int chunkX, int chunkZ,
			Operation<ChunkAccess> original)
	{
		final ChunkAccess chunk = original.call(engine, chunkX, chunkZ);
		if (chunk == null)
		{
			return null;
		}
		final ChunkPos pos = chunk.getPos();
		return pos.x == chunkX && pos.z == chunkZ ? chunk : null;
	}
}
