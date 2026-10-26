package com.diamondskybox.foreverfixes;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;

/**
 * Refuses moving a player into another dimension when the move comes from a general teleport (waystones, Tombstone tablets, FTB
 * Essentials commands, recall spells...) instead of Tempad, a stargate, a rocket or the destination's own way in. The cause is read from the call
 * stack: the first frame whose class matches teleports.crossDimensionDenyCallers. Called by MixinServerPlayerTeleport.
 */
public final class CrossDimensionGuard
{
	private static final StackWalker WALKER = StackWalker.getInstance();

	private CrossDimensionGuard()
	{
	}

	public static boolean refuse(ServerPlayer player, ServerLevel target)
	{
		if (target == null || target == player.level() || !ForeverFixesCommonConfig.SPEC.isLoaded()
				|| !ForeverFixesCommonConfig.BLOCK_CROSS_DIMENSION_TELEPORTS.get())
		{
			return false;
		}
		if (ForeverFixesCommonConfig.CROSS_DIMENSION_OPS_EXEMPT.get() && player.hasPermissions(2))
		{
			return false;
		}
		final List<? extends String> deny = ForeverFixesCommonConfig.CROSS_DIMENSION_DENY_CALLERS.get();
		final Optional<String> cause = WALKER.walk(frames -> frames.map(StackWalker.StackFrame::getClassName)
				.filter(c -> deny.stream().anyMatch(c::startsWith)).findFirst());
		if (cause.isEmpty())
		{
			return false;
		}
		ForeverFixes.LOGGER.info("Refused moving {} from {} to {} (caused by {})", player.getGameProfile().getName(),
				player.level().dimension().location(), target.dimension().location(), cause.get());
		player.displayClientMessage(Component.translatable("foreverfixes.teleport.cross_dimension_refused"), true);
		return true;
	}
}
