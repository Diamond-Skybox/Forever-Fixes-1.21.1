package com.diamondskybox.foreverfixes;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

/** A block change Sable saw during its physics step, applied right after it (see mixin.sable.MixinSubLevelPhysicsSystemDeferBlockChanges). */
public record SableDeferredBlockChange(SectionPos section, LevelChunkSection chunkSection, int x, int y, int z, BlockState oldState,
		BlockState newState)
{
}
