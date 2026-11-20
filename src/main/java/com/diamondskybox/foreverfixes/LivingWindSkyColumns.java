package com.diamondskybox.foreverfixes;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.lang.ref.WeakReference;

/**
 * Living Wind's "can this spot see the sky, ignoring leaves" check, answered per column (see {@code MixinWindShelterSkyColumns}).
 * Where vanilla says no sky, Living Wind climbs from the spot up to the column's motion-blocking height, block by block, and stops
 * at the first block that is not leaves and has collision or occludes. That runs for every probe of its shelter survey and for
 * every particle it pushes, so under a floating island 1000 blocks up each probe read the whole gap up to the island.
 * <p>
 * A spot is blocked exactly when the highest such block in its column (below the motion-blocking height) is at or above it. That
 * block is found scanning down from the top, which under an island stops on the island's top layer; the scan goes no lower than
 * the lowest spot asked about so far, so open water is read once per column, not once per probe. Columns are kept for a second
 * (Living Wind keeps its own shelter results for three to four and a half), then read again.
 * <p>
 * Render thread only: the map is not shared.
 */
public final class LivingWindSkyColumns
{
	private static final int LIFE_TICKS = 20;
	private static final int NONE = Integer.MIN_VALUE;
	private static final int TOP = 0, CLEAR_FROM = 1, BLOCKER = 2;

	/** column -> { motion-blocking height, lowest y scanned clear (everything from here up to the top is open), highest blocker or NONE } */
	private static final Long2ObjectOpenHashMap<int[]> COLUMNS = new Long2ObjectOpenHashMap<>();
	private static WeakReference<ClientLevel> cachedLevel = new WeakReference<>(null);
	private static long cachedEpoch = Long.MIN_VALUE;

	private LivingWindSkyColumns()
	{
	}

	/** Same answer as WindShelter.canSeeSkyIgnoringLeaves. */
	public static boolean canSeeSkyIgnoringLeaves(ClientLevel level, BlockPos at)
	{
		if (level.canSeeSky(at))
		{
			return true;
		}
		if (level.isOutsideBuildHeight(at) || !level.hasChunkAt(at))
		{
			return false;
		}
		final long epoch = level.getGameTime() / LIFE_TICKS;
		if (cachedLevel.get() != level || epoch != cachedEpoch)
		{
			COLUMNS.clear();
			cachedLevel = new WeakReference<>(level);
			cachedEpoch = epoch;
		}
		final int x = at.getX();
		final int y = at.getY();
		final int z = at.getZ();
		final long key = ChunkPos.asLong(x, z);
		int[] column = COLUMNS.get(key);
		if (column == null)
		{
			final int top = Math.min(level.getMaxBuildHeight(), level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z));
			column = new int[] { top, top, NONE };
			COLUMNS.put(key, column);
		}
		if (column[BLOCKER] != NONE)
		{
			return y > column[BLOCKER];
		}
		if (y >= column[CLEAR_FROM])
		{
			return true;
		}
		final BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
		for (int sy = column[CLEAR_FROM] - 1; sy >= y; sy--)
		{
			probe.set(x, sy, z);
			final BlockState block = level.getBlockState(probe);
			if (!block.is(BlockTags.LEAVES) && (!block.getCollisionShape(level, probe).isEmpty() || block.canOcclude()))
			{
				column[BLOCKER] = sy;
				return false;
			}
		}
		column[CLEAR_FROM] = y;
		return true;
	}
}
