package com.diamondskybox.foreverfixes;

import dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import dev.ryanhcode.sable.mixinterface.clip_overwrite.ClipContextExtension;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Sable ships keep rain off what is under them. Rain, its splashes, Better Simple Clouds' downpour and its wet ground all find
 * where rain stops from the world's heightmap, which only knows the world's own blocks: Sable ships live in their own plots and
 * are drawn moved and turned into place, so rain fell through airships and the ground under them counted as open sky. Every
 * couple of client ticks, each column under a nearby ship is raycast downward against ship blocks only (Sable's own clip, as it
 * does for mobs in sunlight), and the height lookups take the top of the ship where it is above the terrain. Client only.
 */
public final class ShipRainCover
{
	private static final int UPDATE_TICKS = 2;
	private static final double RANGE = 128.0;
	private static final int MAX_COLUMNS = 32768;

	/** column (x, z) -> first free y above the ship there, like a MOTION_BLOCKING heightmap value */
	private static final Long2IntOpenHashMap cover = new Long2IntOpenHashMap();
	private static ClientLevel coverLevel;
	private static int ticks;

	private ShipRainCover()
	{
	}

	public static void onClientTick(ClientTickEvent.Post event)
	{
		if (++ticks % UPDATE_TICKS != 0)
		{
			return;
		}
		final Minecraft mc = Minecraft.getInstance();
		final ClientLevel level = mc.level;
		cover.clear();
		coverLevel = level;
		if (level == null || mc.gameRenderer == null || !ForeverFixesConfig.enabled(ForeverFixesConfig.SABLE_SHIP_RAIN))
		{
			return;
		}
		final ClientSubLevelContainer container = SubLevelContainer.getContainer(level);
		if (container == null)
		{
			return;
		}
		final Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		int columns = 0;
		for (ClientSubLevel ship : container.getAllSubLevels())
		{
			final BoundingBox3dc box = ship.boundingBox();
			if (box == null || box.maxX() < camera.x - RANGE || box.minX() > camera.x + RANGE || box.maxZ() < camera.z - RANGE
					|| box.minZ() > camera.z + RANGE)
			{
				continue;
			}
			final double top = box.maxY() + 1.0;
			final double bottom = box.minY() - 1.0;
			for (int x = Mth.floor(box.minX()); x <= Mth.floor(box.maxX()); x++)
			{
				for (int z = Mth.floor(box.minZ()); z <= Mth.floor(box.maxZ()); z++)
				{
					if (++columns > MAX_COLUMNS)
					{
						return;
					}
					final int y = shipTop(level, x + 0.5, z + 0.5, top, bottom);
					if (y != Integer.MIN_VALUE)
					{
						final long key = key(x, z);
						cover.put(key, Math.max(y, cover.getOrDefault(key, Integer.MIN_VALUE)));
					}
				}
			}
		}
	}

	/** the first free y above the ship in this column, or Integer.MIN_VALUE when no ship block is in it */
	private static int shipTop(Level level, double x, double z, double top, double bottom)
	{
		final ClipContext context = new ClipContext(new Vec3(x, top, z), new Vec3(x, bottom, z), ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE, CollisionContext.empty());
		((ClipContextExtension) context).sable$setIgnoreMainLevel(true);
		final BlockHitResult hit = level.clip(context);
		return hit.getType() == HitResult.Type.BLOCK ? Mth.ceil(hit.getLocation().y - 1.0E-4) : Integer.MIN_VALUE;
	}

	/** a heightmap height with the ships over the column taken into account */
	public static int height(Level level, int x, int z, int original)
	{
		if (cover.isEmpty() || level != coverLevel)
		{
			return original;
		}
		return Math.max(original, cover.getOrDefault(key(x, z), Integer.MIN_VALUE));
	}

	private static long key(int x, int z)
	{
		return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
	}
}
