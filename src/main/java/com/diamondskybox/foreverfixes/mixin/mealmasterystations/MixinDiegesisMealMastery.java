package com.diamondskybox.foreverfixes.mixin.mealmasterystations;

import com.diamondskybox.foreverfixes.MealMasteryStations;
import com.pockyl.diegesis.impl.network.Packets;
import com.pockyl.diegesis.api.station.StationContext;
import com.pockyl.diegesis.impl.server.StationManager;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Meal Mastery x Diegesis (Hands On Delight): station opens/actions count as the workstation click Meal Mastery waits for; see MealMasteryStations. */
@Mixin(value = StationManager.class, remap = false)
public abstract class MixinDiegesisMealMastery
{
	@Shadow @Final private MinecraftServer server;
	@Shadow @Final private Map<UUID, StationContext> byPlayer; // really Map<UUID, ActiveStation> (package-private; same erasure)

	/** HEAD: before the station hands anything over, so the dish lands after Meal Mastery's inventory baseline */
	@Inject(method = "handle(Lnet/minecraft/server/level/ServerPlayer;Lcom/pockyl/diegesis/impl/network/Packets$ClientPacket;)V", remap = false,
			require = 1, at = @At("HEAD"))
	private void foreverfixes$mealMasteryClick(ServerPlayer player, Packets.ClientPacket packet, CallbackInfo ci)
	{
		if (packet instanceof Packets.Open open)
		{
			MealMasteryStations.used(player, open.pos());
		}
		else if (packet instanceof Packets.Action action)
		{
			MealMasteryStations.used(player, action.pos());
		}
	}

	/** while a station stays open, keep Meal Mastery's window going (credit and the cooking-speed bonus, like an open menu) */
	@Inject(method = "tick()V", remap = false, require = 1, at = @At("RETURN"))
	private void foreverfixes$mealMasteryOpenStations(CallbackInfo ci)
	{
		for (Map.Entry<UUID, StationContext> e : List.copyOf(this.byPlayer.entrySet()))
		{
			final ServerPlayer player = this.server.getPlayerList().getPlayer(e.getKey());
			if (player != null)
			{
				MealMasteryStations.used(player, e.getValue().pos());
			}
		}
	}

	@Inject(method = "onLogout(Lnet/minecraft/server/level/ServerPlayer;)V", remap = false, require = 1, at = @At("HEAD"))
	private void foreverfixes$mealMasteryLogout(ServerPlayer player, CallbackInfo ci)
	{
		MealMasteryStations.forget(player.getUUID());
	}
}
