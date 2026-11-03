package com.diamondskybox.foreverfixes;

import dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Sable ships keep rain off what is under them. Rain, its splashes, Better Simple Clouds' downpour and its wet ground all find
 * where rain stops from the world's heightmap, which only knows the world's own blocks: Sable ships live in their own plots and
 * are drawn moved and turned into place, so rain fell through airships and the ground under them counted as open sky.
 * <p>
 * Each nearby ship keeps the tops of its block columns in its own (plot) coordinates, worked out again only when a block on the
 * ship changes (and every ten seconds, in case a change was missed). While rain or snow falls on the ship, those tops are laid
 * over the world's columns through the ship's pose, again only when the ship has moved. A parked ship costs nothing, and a ship
 * out of the weather blocks nothing. The height lookups take the top of the ship where it is above the terrain. Client only.
 */
public final class ShipRainCover
{
	private static final int UPDATE_TICKS = 2;
	private static final double RANGE = 128.0;
	/** updates (of UPDATE_TICKS) between re-reads of a ship's blocks, in case a block change was missed */
	private static final int REREAD_UPDATES = 100;
	/** updates between checks whether rain or snow falls on a ship */
	private static final int WEATHER_CHECK_UPDATES = 10;
	/** updates a ship keeps covering after the last rain or snow on it: wet ground takes a while to dry */
	private static final int LINGER_UPDATES = 1200;
	private static final int MAX_TOPS = 65536;
	private static final double POSITION_EPSILON = 1.0E-3;
	private static final double ORIENTATION_EPSILON = 1.0E-5;

	private static final Map<SubLevel, Ship> ships = new IdentityHashMap<>();
	/** column (x, z) -> first free y above the ships there, like a MOTION_BLOCKING heightmap value */
	private static final Long2IntOpenHashMap cover = new Long2IntOpenHashMap();
	private static ClientLevel coverLevel;
	private static int ticks;

	private ShipRainCover()
	{
	}

	/** what one ship covers */
	private static final class Ship
	{
		/** the tops of the ship's block columns in plot coordinates: x, first free y, z */
		final IntArrayList tops = new IntArrayList();
		boolean blocksChanged = true;
		int updatesSinceRead;
		int weatherCheckIn;
		int weatherLeft;
		/** the pose the columns below were laid with, or placed = false */
		boolean placed;
		double px, py, pz, qx, qy, qz, qw;
		final Long2IntOpenHashMap columns = new Long2IntOpenHashMap();
	}

	/** a block in a ship's plot changed (both sides call this; only client ships are tracked) */
	public static void onPlotBlockChange(LevelPlot plot)
	{
		final SubLevel subLevel = plot.getSubLevel();
		if (subLevel == null || subLevel.getLevel() == null || !subLevel.getLevel().isClientSide())
		{
			return;
		}
		final Ship ship = ships.get(subLevel);
		if (ship != null)
		{
			ship.blocksChanged = true;
		}
	}

	public static void onClientTick(ClientTickEvent.Post event)
	{
		if (++ticks % UPDATE_TICKS != 0)
		{
			return;
		}
		final Minecraft mc = Minecraft.getInstance();
		final ClientLevel level = mc.level;
		if (level != coverLevel || level == null || mc.gameRenderer == null || !ForeverFixesConfig.enabled(ForeverFixesConfig.SABLE_SHIP_RAIN))
		{
			ships.clear();
			cover.clear();
			coverLevel = level;
			if (level == null || mc.gameRenderer == null || !ForeverFixesConfig.enabled(ForeverFixesConfig.SABLE_SHIP_RAIN))
			{
				return;
			}
		}
		final ClientSubLevelContainer container = SubLevelContainer.getContainer(level);
		if (container == null)
		{
			if (!ships.isEmpty())
			{
				ships.clear();
				cover.clear();
			}
			return;
		}
		final Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		boolean changed = false;
		final Map<SubLevel, Boolean> seen = new IdentityHashMap<>();
		for (ClientSubLevel subLevel : container.getAllSubLevels())
		{
			final BoundingBox3dc box = subLevel.boundingBox();
			if (box == null || box.maxX() < camera.x - RANGE || box.minX() > camera.x + RANGE || box.maxZ() < camera.z - RANGE
					|| box.minZ() > camera.z + RANGE)
			{
				continue;
			}
			seen.put(subLevel, Boolean.TRUE);
			final Ship ship = ships.computeIfAbsent(subLevel, s -> new Ship());
			if (--ship.weatherCheckIn <= 0)
			{
				ship.weatherCheckIn = WEATHER_CHECK_UPDATES;
				if (precipitationOn(level, box))
				{
					ship.weatherLeft = LINGER_UPDATES;
				}
			}
			if (ship.weatherLeft <= 0)
			{
				if (!ship.columns.isEmpty())
				{
					ship.columns.clear();
					changed = true;
				}
				ship.placed = false;
				continue;
			}
			ship.weatherLeft--;
			if (ship.blocksChanged || ++ship.updatesSinceRead >= REREAD_UPDATES)
			{
				readTops(subLevel, ship);
				ship.blocksChanged = false;
				ship.updatesSinceRead = 0;
				ship.placed = false;
			}
			final Pose3dc pose = subLevel.renderPose();
			if (pose != null && (!ship.placed || moved(ship, pose)))
			{
				place(ship, pose);
				changed = true;
			}
		}
		for (Iterator<SubLevel> it = ships.keySet().iterator(); it.hasNext(); )
		{
			if (!seen.containsKey(it.next()))
			{
				it.remove();
				changed = true;
			}
		}
		if (changed)
		{
			cover.clear();
			for (Ship ship : ships.values())
			{
				for (Long2IntMap.Entry column : ship.columns.long2IntEntrySet())
				{
					final long key = column.getLongKey();
					cover.put(key, Math.max(column.getIntValue(), cover.getOrDefault(key, Integer.MIN_VALUE)));
				}
			}
		}
	}

	/** rain or snow falls on the ship: checked above its middle and its corners */
	private static boolean precipitationOn(ClientLevel level, BoundingBox3dc box)
	{
		final int y = Mth.ceil(box.maxY()) + 1;
		final int minX = Mth.floor(box.minX());
		final int maxX = Mth.floor(box.maxX());
		final int minZ = Mth.floor(box.minZ());
		final int maxZ = Mth.floor(box.maxZ());
		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		final int[][] points = {{(minX + maxX) >> 1, (minZ + maxZ) >> 1}, {minX, minZ}, {minX, maxZ}, {maxX, minZ}, {maxX, maxZ}};
		for (int[] point : points)
		{
			pos.set(point[0], y, point[1]);
			// Simple Clouds answers isRainingAt from its own clouds; snow is checked the vanilla way
			if (level.isRainingAt(pos) || level.isRaining() && level.getBiome(pos).value().getPrecipitationAt(pos) == Biome.Precipitation.SNOW)
			{
				return true;
			}
		}
		return false;
	}

	/** the first free y above each block column of the ship, in plot coordinates */
	private static void readTops(ClientSubLevel subLevel, Ship ship)
	{
		ship.tops.clear();
		final LevelPlot plot = subLevel.getPlot();
		final BoundingBox3ic bounds = plot == null ? null : plot.getBoundingBox();
		if (bounds == null)
		{
			return;
		}
		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int x = bounds.minX(); x <= bounds.maxX(); x++)
		{
			for (int z = bounds.minZ(); z <= bounds.maxZ(); z++)
			{
				final LevelChunk chunk = plot.getChunk(new ChunkPos(x >> 4, z >> 4));
				if (chunk == null)
				{
					continue;
				}
				for (int y = bounds.maxY(); y >= bounds.minY(); y--)
				{
					final BlockState state = chunk.getBlockState(pos.set(x, y, z));
					if (state.blocksMotion() || !state.getFluidState().isEmpty())
					{
						if (ship.tops.size() >= MAX_TOPS * 3)
						{
							return;
						}
						ship.tops.add(x);
						ship.tops.add(y + 1);
						ship.tops.add(z);
						break;
					}
				}
			}
		}
	}

	private static boolean moved(Ship ship, Pose3dc pose)
	{
		final Vector3dc p = pose.position();
		final Quaterniondc q = pose.orientation();
		return Math.abs(p.x() - ship.px) > POSITION_EPSILON || Math.abs(p.y() - ship.py) > POSITION_EPSILON
				|| Math.abs(p.z() - ship.pz) > POSITION_EPSILON || Math.abs(q.x() - ship.qx) > ORIENTATION_EPSILON
				|| Math.abs(q.y() - ship.qy) > ORIENTATION_EPSILON || Math.abs(q.z() - ship.qz) > ORIENTATION_EPSILON
				|| Math.abs(q.w() - ship.qw) > ORIENTATION_EPSILON;
	}

	/** lays the ship's column tops over the world's columns: four points on each top face, so a turned ship leaves no gaps */
	private static void place(Ship ship, Pose3dc pose)
	{
		final Vector3dc p = pose.position();
		final Quaterniondc q = pose.orientation();
		ship.px = p.x();
		ship.py = p.y();
		ship.pz = p.z();
		ship.qx = q.x();
		ship.qy = q.y();
		ship.qz = q.z();
		ship.qw = q.w();
		ship.placed = true;
		ship.columns.clear();
		final Vector3d point = new Vector3d();
		final int[] tops = ship.tops.elements();
		for (int i = 0, n = ship.tops.size(); i < n; i += 3)
		{
			for (int corner = 0; corner < 4; corner++)
			{
				point.set(tops[i] + ((corner & 1) == 0 ? 0.25 : 0.75), tops[i + 1], tops[i + 2] + ((corner & 2) == 0 ? 0.25 : 0.75));
				pose.transformPosition(point, point);
				final long key = key(Mth.floor(point.x), Mth.floor(point.z));
				final int y = Mth.ceil(point.y - 1.0E-4);
				ship.columns.put(key, Math.max(y, ship.columns.getOrDefault(key, Integer.MIN_VALUE)));
			}
		}
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
