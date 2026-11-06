package com.diamondskybox.foreverfixes.mixin.aerowarptics;

import com.diamondskybox.foreverfixes.AeroWarpticsBridge;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.co.iceconchy.aerowarptics.airship.Airship;
import uk.co.iceconchy.aerowarptics.anchor.WarpAnchor;
import uk.co.iceconchy.aerowarptics.anchor.WarpAnchorRegistry;
import uk.co.iceconchy.aerowarptics.drive.RiftDriveBlockEntity;
import uk.co.iceconchy.aerowarptics.network.ClientboundCorridorPacket;
import uk.co.iceconchy.aerowarptics.network.ClientboundWarpEffectPacket;
import uk.co.iceconchy.aerowarptics.warp.WarpFlight;

import java.util.List;
import java.util.UUID;

/**
 * When a Rift Drive bound for an anchor in another dimension would teleport its ship out of the corridor (inside the ship's own
 * dimension), finish the warp here and hand the ship to {@link AeroWarpticsBridge} instead.
 */
@Mixin(value = RiftDriveBlockEntity.class, remap = false)
public abstract class MixinRiftDriveCrossDimension
{
	@Shadow(remap = false)
	private WarpFlight flight;
	@Shadow(remap = false)
	private UUID destinationAnchor;
	@Shadow(remap = false)
	private float charge;
	@Shadow(remap = false)
	private float committedCost;

	@Shadow(remap = false)
	public abstract Airship airship();

	@Shadow(remap = false)
	private void completeWarp()
	{
		throw new AssertionError();
	}

	@Shadow(remap = false)
	private void broadcastRift(ClientboundWarpEffectPacket.Stage stage, WarpFlight.Rift rift, int duration)
	{
		throw new AssertionError();
	}

	@Shadow(remap = false)
	private static void send(List<ServerPlayer> players, CustomPacketPayload payload)
	{
		throw new AssertionError();
	}

	@Inject(method = "tickFlight", remap = false, cancellable = true, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Luk/co/iceconchy/aerowarptics/airship/Airship;relocate(Lorg/joml/Vector3dc;Lorg/joml/Quaterniondc;D)Z"))
	private void foreverfixes$crossDimension(CallbackInfo ci)
	{
		if (!(((BlockEntity) (Object) this).getLevel() instanceof ServerLevel source))
		{
			return;
		}
		final WarpFlight current = this.flight;
		final UUID anchorId = this.destinationAnchor;
		final Airship ship = this.airship();
		if (current == null || anchorId == null || ship == null)
		{
			return;
		}
		final WarpAnchor anchor = WarpAnchorRegistry.get(source).byId(anchorId);
		if (anchor == null || anchor.isInSameDimension(source))
		{
			return;
		}
		final ServerLevel destination = source.getServer().getLevel(anchor.dimension());
		if (destination == null)
		{
			return;
		}
		ci.cancel();
		final Vector3d arrival = new Vector3d(current.arrivalOrigin());
		final Quaterniond orientation = new Quaterniond(current.arrivalOrientation());
		final List<ServerPlayer> crew = ship.crew();
		this.broadcastRift(ClientboundWarpEffectPacket.Stage.RIFT_CLOSE, current.entryRift(), 0);
		send(crew, ClientboundCorridorPacket.leave());
		// what onLeftCorridor + completeWarp would do: pay the charge, settle the crew, release the ship, start the cooldown;
		// the drive is saved with the ship in that state, so it wakes up in the new dimension in cooldown, not "interrupted"
		this.charge = Math.max(0.0F, this.charge - this.committedCost);
		this.completeWarp();
		AeroWarpticsBridge.cross(ship, destination, arrival, orientation, anchor.pos());
	}
}
