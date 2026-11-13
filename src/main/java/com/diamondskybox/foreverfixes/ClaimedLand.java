package com.diamondskybox.foreverfixes;

import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import io.github.flemmli97.flan.claim.ClaimStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import org.joml.Vector3d;

/**
 * Claimed land is weapon-safe: Create Big Cannons rounds (and the CBC: Advanced Technologies rockets) and Sable: Destructive crashes don't
 * break, blast, transform or set fire to blocks inside a Flan claim or an FTB Chunks claimed chunk, whoever fired or flew. That keeps
 * crash accidents off people's bases and lets anyone build a weapons range on their own land. A block on a Sable ship counts as standing
 * wherever the ship is right now, so ships over claimed land are covered too. Wilderness is unchanged.
 */
public final class ClaimedLand
{
	private static final boolean FLAN = ModList.get().isLoaded("flan");
	private static final boolean FTB_CHUNKS = ModList.get().isLoaded("ftbchunks");
	private static final boolean SABLE = ModList.get().isLoaded("sable");

	private ClaimedLand()
	{
	}

	public static boolean enabled()
	{
		return ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.CLAIMS_NO_WEAPON_DAMAGE);
	}

	/** true when pos (a world position, or a block in a Sable ship's plot) is in claimed land, where weapons and crashes leave blocks alone */
	public static boolean protects(Level level, BlockPos pos)
	{
		if (!enabled() || !(level instanceof ServerLevel serverLevel) || pos == null)
		{
			return false;
		}
		try
		{
			final BlockPos world = SABLE ? Ships.toWorld(serverLevel, pos) : pos;
			return FLAN && Flan.claimed(serverLevel, world) || FTB_CHUNKS && FtbChunks.claimed(serverLevel, world);
		}
		catch (RuntimeException | LinkageError e)
		{
			return false;
		}
	}

	private static final class Ships
	{
		static BlockPos toWorld(ServerLevel level, BlockPos pos)
		{
			final SubLevelAccess ship = Sable.HELPER.getContaining(level, pos);
			if (ship == null)
			{
				return pos;
			}
			final Vector3d at = ship.logicalPose().transformPosition(new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
			return BlockPos.containing(at.x, at.y, at.z);
		}
	}

	private static final class Flan
	{
		static boolean claimed(ServerLevel level, BlockPos pos)
		{
			return ClaimStorage.get(level).getClaimAt(pos) != null;
		}
	}

	private static final class FtbChunks
	{
		static boolean claimed(ServerLevel level, BlockPos pos)
		{
			return FTBChunksAPI.api().isManagerLoaded() && FTBChunksAPI.api().getManager().getChunk(new ChunkDimPos(level, pos)) != null;
		}
	}
}
