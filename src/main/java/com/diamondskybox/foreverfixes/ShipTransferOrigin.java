package com.diamondskybox.foreverfixes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

/**
 * Height bookkeeping for Dimensional Sable's ship transfer (see the cosmonautics mixins). Dimensional Sable moves block entities
 * and passengers relative to an origin of "plot centre at mid build height, plus the dimension's minimum Y", which assumes a ship
 * sits at the middle of its dimension's height; a Sable ship keeps the height it was assembled at instead. Forever Fixes puts
 * that origin at Y 0 on the source side, so heights stay absolute, and at a vertical shift on the destination side: 0 when the
 * ship fits the destination's build height, otherwise whole sections that centre it there. Blocks, block entities and passengers
 * all move by that one shift.
 */
public final class ShipTransferOrigin
{
	/** Lowest section of the source dimension, recorded in the transfer template; also marks templates using these rules. */
	public static final String MIN_SECTION = "foreverfixes_min_section";

	/** Margin (blocks) around the ship when gathering the entities that travel with it; covers about 900 m/s of trailing bounds. */
	public static final double GATHER_MARGIN = 48.0D;

	/** Block-Y extent of the ship saved last on this thread; WarpSubLevels saves a sub-level right before computing its offset. */
	private static final ThreadLocal<int[]> LAST_EXTENT = new ThreadLocal<>();

	private ShipTransferOrigin()
	{
	}

	/** Called with a freshly saved template: records the source's lowest section and the ship's height range. */
	public static void recordSave(CompoundTag template, int minSection)
	{
		int lowest = Integer.MAX_VALUE;
		int highest = Integer.MIN_VALUE;
		final CompoundTag chunks = template.getCompound("chunks");
		for (String chunkKey : chunks.getAllKeys())
		{
			for (String sectionKey : chunks.getCompound(chunkKey).getCompound("sections").getAllKeys())
			{
				try
				{
					final int index = Integer.parseInt(sectionKey);
					lowest = Math.min(lowest, index);
					highest = Math.max(highest, index);
				}
				catch (NumberFormatException ignored)
				{
				}
			}
		}
		template.putInt(MIN_SECTION, minSection);
		if (lowest <= highest)
		{
			LAST_EXTENT.set(new int[] { (lowest + minSection) << 4, ((highest + minSection) << 4) + 15 });
		}
		else
		{
			LAST_EXTENT.remove();
		}
	}

	/** Vertical shift (a multiple of 16) for the ship saved last on this thread, moving into the given level. */
	public static int takeShift(Level destination)
	{
		final int[] extent = LAST_EXTENT.get();
		LAST_EXTENT.remove();
		if (extent == null)
		{
			return 0;
		}
		final int bottom = destination.getMinBuildHeight();
		final int top = destination.getMaxBuildHeight();
		if (extent[0] >= bottom && extent[1] < top)
		{
			return 0;
		}
		return ((bottom + top) / 2 - (extent[0] + extent[1] + 1) / 2) & ~15;
	}
}
