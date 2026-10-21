package com.diamondskybox.foreverfixes;

import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Follow-up to Create: Cosmonautics putting a seated player back on their seat after a space transition. Cosmonautics moves the
 * player (which takes them off the seat), moves the ship, and re-seats them on the same tick; when that does not stick the ship
 * flies on without them (Cosmonautics issue #170). For two seconds afterwards the player is re-seated if they are not on that
 * seat (the seat may only become findable a tick later), and the seat's passenger list is re-sent to their client in case the
 * client missed it. Each step is logged, so a failed re-seat shows in the log.
 */
public final class ShipReseat
{
	private static final int FOLLOW_UP_TICKS = 40;
	private static final List<Pending> PENDING = new ArrayList<>();

	private ShipReseat()
	{
	}

	private static final class Pending
	{
		final UUID player;
		final UUID vehicle;
		int ticks;

		Pending(UUID player, UUID vehicle)
		{
			this.player = player;
			this.vehicle = vehicle;
		}
	}

	/** Called right after Cosmonautics' own re-seat attempt (server thread). */
	public static void afterCosmonauticsReseat(ServerLevel level, ServerPlayer player, UUID vehicleId)
	{
		final Entity vehicle = level.getEntity(vehicleId);
		ForeverFixes.LOGGER.info("Space transition re-seat: {} -> {} in {}: seat {}, riding it {}", player.getGameProfile().getName(),
				vehicleId, level.dimension().location(), vehicle == null ? "not found" : vehicle.getType().toShortString(),
				vehicle != null && player.getVehicle() == vehicle);
		if (vehicle == null)
		{
			for (ServerLevel other : level.getServer().getAllLevels())
			{
				final Entity found = other.getEntity(vehicleId);
				if (found != null)
				{
					ForeverFixes.LOGGER.info("Space transition re-seat: the seat is in {} at {} {} {} (removed: {})", other.dimension().location(),
							(int) found.getX(), (int) found.getY(), (int) found.getZ(), found.isRemoved());
				}
			}
		}
		PENDING.add(new Pending(player.getUUID(), vehicleId));
	}

	public static void onServerTick(ServerTickEvent.Post event)
	{
		if (PENDING.isEmpty())
		{
			return;
		}
		for (Iterator<Pending> it = PENDING.iterator(); it.hasNext(); )
		{
			final Pending pending = it.next();
			pending.ticks++;
			final ServerPlayer player = event.getServer().getPlayerList().getPlayer(pending.player);
			if (player == null)
			{
				it.remove();
				continue;
			}
			final Entity vehicle = player.serverLevel().getEntity(pending.vehicle);
			if (vehicle != null && vehicle.isAlive() && player.getVehicle() != vehicle)
			{
				final boolean seated = player.startRiding(vehicle, true);
				ForeverFixes.LOGGER.info("Space transition re-seat: {} put back on {} after {} tick(s): {}", player.getGameProfile().getName(),
						vehicle.getType().toShortString(), pending.ticks, seated);
			}
			if (vehicle != null && (pending.ticks == 5 || pending.ticks == 20))
			{
				player.connection.send(new ClientboundSetPassengersPacket(vehicle));
			}
			if (pending.ticks >= FOLLOW_UP_TICKS)
			{
				ForeverFixes.LOGGER.info("Space transition re-seat: {} after {} ticks: seat {}, riding it {}", player.getGameProfile().getName(),
						pending.ticks, vehicle == null ? "not found" : "present", vehicle != null && player.getVehicle() == vehicle);
				it.remove();
			}
		}
	}
}
