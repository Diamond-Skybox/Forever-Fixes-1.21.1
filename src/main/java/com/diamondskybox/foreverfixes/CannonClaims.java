package com.diamondskybox.foreverfixes;

import io.github.flemmli97.flan.api.ClaimHandler;
import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Create Big Cannons in Flan claims: a cannon round changes a block in a claim only if whoever fired it may break blocks there.
 * CBC removes the blocks a shot penetrates, and the blocks its shell explosions transform, straight through Level.setBlock, which
 * no claim mod sees (only its explosions' blast lists went through the explosion event Flan filters, and only by the claim's own
 * explosion setting). Each change now asks Flan as the round's owner; a round with no player behind it (a cannon fired by a
 * contraption or a machine) asks with no player, which claims refuse unless they allow it to everyone. Wilderness is unchanged.
 */
public final class CannonClaims
{
	private CannonClaims()
	{
	}

	public static boolean enabled()
	{
		return ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.CBC_CLAIM_PROTECTION);
	}

	/** Flan refuses this change at pos for the actor (a player, or anything else, which counts as nobody) */
	public static boolean refused(Level level, BlockPos pos, @Nullable Entity actor, ResourceLocation permission)
	{
		if (!enabled() || !(level instanceof ServerLevel serverLevel))
		{
			return false;
		}
		try
		{
			final IPermissionContainer claim = ClaimHandler.getPermissionStorage(serverLevel).getForPermissionCheck(pos);
			return claim != null && !claim.canInteract(actor instanceof ServerPlayer player ? player : null, permission, pos);
		}
		catch (RuntimeException e)
		{
			return false;
		}
	}

	public static boolean breakRefused(Level level, BlockPos pos, @Nullable Entity actor)
	{
		return refused(level, pos, actor, BuiltinPermission.BREAK);
	}

	public static boolean placeRefused(Level level, BlockPos pos, @Nullable Entity actor)
	{
		return refused(level, pos, actor, BuiltinPermission.PLACE);
	}
}
