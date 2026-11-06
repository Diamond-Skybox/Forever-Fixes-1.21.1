package com.diamondskybox.foreverfixes.mixin.chronomancy;

import com.chronomancy.network.ChronoNetwork;
import com.chronomancy.temporal.worldstop.GlobalTimeStopManager;
import com.diamondskybox.foreverfixes.ChromewareHook;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * A player whose Sandevistan keeps them outside stopped time joins the world stop's movers, the way Chronomancy's own rift-trial
 * participants do: checked at the start of every server tick of a stop, before the frozen players are pinned in place.
 */
@Mixin(value = GlobalTimeStopManager.class, remap = false)
public abstract class MixinGlobalTimeStopManager
{
	@Shadow(remap = false)
	private static boolean active;
	@Shadow(remap = false)
	private static MinecraftServer server;

	@Shadow(remap = false)
	static boolean enlist(ServerPlayer player)
	{
		throw new AssertionError();
	}

	@Shadow(remap = false)
	static List<Integer> moverIds()
	{
		throw new AssertionError();
	}

	@Inject(method = "onServerTick", remap = false, require = 1, at = @At("HEAD"))
	private static void foreverfixes$sandevistanMovers(ServerTickEvent.Post event, CallbackInfo ci)
	{
		if (!active || server == null)
		{
			return;
		}
		boolean joined = false;
		for (ServerPlayer player : server.getPlayerList().getPlayers())
		{
			if (!GlobalTimeStopManager.isMover(player) && player.isAlive() && !player.isSpectator()
					&& ChromewareHook.outsideStoppedTime(player) && enlist(player))
			{
				joined = true;
			}
		}
		if (joined)
		{
			ChronoNetwork.broadcastWorldStopMovers(moverIds());
		}
	}
}
