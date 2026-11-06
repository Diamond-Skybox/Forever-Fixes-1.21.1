package com.diamondskybox.foreverfixes;

import dev.egg.SubLevelWarper;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iceconchy.aerowarptics.airship.Airship;
import uk.co.iceconchy.aerowarptics.warp.CrossDimensionWarp;
import uk.co.iceconchy.aerowarptics.warp.WarpFailure;

/**
 * Aero Warptics x Dimensional Sable: Rift Drive warps to anchors in other dimensions (planets).
 *
 * Aero Warptics asks its cross-dimension handler for permission when the warp begins, then flies the whole warp with the
 * ship still in its own dimension and teleports it within that dimension as it leaves the corridor. The handler here only
 * approves (refusing Cosmonautics' deep space, whose ships must live in orbit instances); the actual move happens in
 * {@code mixin.aerowarptics.MixinRiftDriveCrossDimension}: at the moment the drive would teleport the ship out of the corridor,
 * the drive finishes its warp bookkeeping (so it arrives settled, in cooldown), and the ship is moved by Dimensional Sable to its
 * planned resting place beside the anchor. The arrival run-out through the exit rift is skipped.
 *
 * Still needs {@code allowCrossDimensionWarp = true} in the world's aerowarptics-server.toml.
 */
public final class AeroWarpticsBridge
{
	private static final Logger LOGGER = LoggerFactory.getLogger("ForeverFixes");
	private static final ResourceLocation DEEP_SPACE = ResourceLocation.fromNamespaceAndPath("rocketnautics", "deep_space");

	private AeroWarpticsBridge()
	{
	}

	public static void init()
	{
		CrossDimensionWarp.setHandler((airship, destination, anchorPos) ->
				destination.dimension().location().equals(DEEP_SPACE) ? WarpFailure.DIMENSION_UNSUPPORTED : WarpFailure.NONE);
		LOGGER.info("Aero Warptics: cross-dimension Rift Drive warps go through Dimensional Sable");
	}

	/** The anchor's "airship arrived" hook, called by name: its class drags Create's block-entity internals into compilation. */
	private static void anchorArrived(BlockEntity anchor)
	{
		if (anchor == null || !anchor.getClass().getName().equals("uk.co.iceconchy.aerowarptics.anchor.WarpAnchorBlockEntity"))
		{
			return;
		}
		try
		{
			anchor.getClass().getMethod("onAirshipArrived").invoke(anchor);
		}
		catch (ReflectiveOperationException e)
		{
			LOGGER.debug("Aero Warptics: anchor arrival hook unavailable: {}", e.toString());
		}
	}

	/**
	 * Moves the ship into {@code destination} at the next tick boundary (not inside the drive's own block-entity tick, which the
	 * move removes), facing {@code orientation} with its pose at {@code position}.
	 */
	public static void cross(Airship airship, ServerLevel destination, Vector3dc position, Quaterniondc orientation, BlockPos anchorPos)
	{
		final ServerSubLevel ship = airship.subLevel();
		final Vector3d target = new Vector3d(position);
		final MinecraftServer server = destination.getServer();
		server.tell(new TickTask(server.getTickCount(), () ->
		{
			if (ship.isRemoved())
			{
				LOGGER.warn("Aero Warptics: ship {} vanished before its cross-dimension warp", ship.getUniqueId());
				return;
			}
			final Airship before = Airship.of(ship);
			before.relocate(before.position(), orientation, 0.0D); // Dimensional Sable keeps the orientation it is given
			final String from = ship.getLevel().dimension().location().toString();
			final int moved = ShipTransferOrigin.riftWarp(() -> SubLevelWarper.WarpSubLevel(ship, destination, target));
			LOGGER.info("Aero Warptics: warped {} sub-level(s) from {} to {} at {}", moved, from, destination.dimension().location(),
					BlockPos.containing(target.x, target.y, target.z).toShortString());
			if (destination.isLoaded(anchorPos))
			{
				anchorArrived(destination.getBlockEntity(anchorPos));
			}
		}));
	}
}
