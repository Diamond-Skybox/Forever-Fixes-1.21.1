package com.diamondskybox.foreverfixes;

import net.blay09.mods.waystones.api.requirement.WarpRequirement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/** The requirement given to waystone warps into another dimension: never affordable, so nothing is charged and the screen says why. */
public final class WaystoneDimensionDeny implements WarpRequirement
{
	public static final WaystoneDimensionDeny INSTANCE = new WaystoneDimensionDeny();

	private WaystoneDimensionDeny()
	{
	}

	public static boolean applies(net.blay09.mods.waystones.api.WaystoneTeleportContext context)
	{
		return context.isDimensionalTeleport() && ForeverFixesCommonConfig.SPEC.isLoaded() && ForeverFixesCommonConfig.BLOCK_CROSS_DIMENSION_TELEPORTS.get()
				&& !(ForeverFixesCommonConfig.CROSS_DIMENSION_OPS_EXEMPT.get() && context.getEntity() instanceof Player p && p.hasPermissions(2));
	}

	@Override
	public boolean canAfford(Player player)
	{
		return false;
	}

	@Override
	public void consume(Player player)
	{
	}

	@Override
	public void rollback(Player player)
	{
	}

	@Override
	public void appendHoverText(Player player, List<Component> tooltip)
	{
		tooltip.add(Component.translatable("foreverfixes.teleport.waystone_cross_dimension").withStyle(ChatFormatting.RED));
	}
}
