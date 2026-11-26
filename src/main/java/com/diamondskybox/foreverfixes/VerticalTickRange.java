package com.diamondskybox.foreverfixes;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.Tags;

/**
 * Mobs far above or below the players near them stop ticking (see MixinServerLevelVerticalTickRange). Chunks tick as whole
 * columns, so in a 2096-block-tall world the villagers on a floating island 1500 blocks overhead ran their AI for anyone passing
 * underneath. A mob is held when at least one player is within simulation distance horizontally and every such player is more than
 * entities.verticalTickRange blocks away vertically. With no player near horizontally (a chunk loader keeps the chunk ticking) it
 * ticks as usual. Never held: bosses, anything a player rides, mobs leashed to a player. The despawn check runs before the tick,
 * so mobs that despawn still go.
 */
public final class VerticalTickRange
{
	private VerticalTickRange()
	{
	}

	public static boolean held(ServerLevel level, Entity entity)
	{
		if (!(entity instanceof Mob mob) || !ForeverFixesCommonConfig.SPEC.isLoaded())
		{
			return false;
		}
		final int range = ForeverFixesCommonConfig.VERTICAL_TICK_RANGE.get();
		if (range <= 0 || entity.getType().is(Tags.EntityTypes.BOSSES) || mob.getLeashHolder() instanceof Player)
		{
			return false;
		}
		for (Entity passenger : entity.getIndirectPassengers())
		{
			if (passenger instanceof Player)
			{
				return false;
			}
		}
		final int chunks = level.getServer().getPlayerList().getSimulationDistance() + 1;
		final int cx = entity.chunkPosition().x;
		final int cz = entity.chunkPosition().z;
		final double y = entity.getY();
		boolean near = false;
		for (ServerPlayer player : level.players())
		{
			if (player.isSpectator() || Math.abs(player.chunkPosition().x - cx) > chunks || Math.abs(player.chunkPosition().z - cz) > chunks)
			{
				continue;
			}
			if (Math.abs(player.getY() - y) <= range)
			{
				return false;
			}
			near = true;
		}
		return near;
	}
}
