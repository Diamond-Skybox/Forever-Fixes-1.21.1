package com.diamondskybox.foreverfixes.mixin.cosmonautics;

import com.diamondskybox.foreverfixes.ShipTransferOrigin;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Set;

/**
 * {@code SubLevelWarper.WarpSubLevels} computes the offset between a ship's old and new plot origin, which moves passengers and
 * position-holding block entities. The old origin is put at Y 0 and the new one at the ship's vertical shift (see
 * {@link ShipTransferOrigin}), so passengers move exactly as far as the blocks they stand on.
 */
@Mixin(targets = "dev.egg.SubLevelWarper", remap = false)
public abstract class MixinSubLevelWarper
{
	@Unique
	private static final Logger foreverfixes$LOGGER = LoggerFactory.getLogger("ForeverFixes");

	@WrapOperation(method = "WarpSubLevels", remap = false, require = 1,
			at = @At(value = "INVOKE", ordinal = 0, remap = false, target = "Lnet/minecraft/core/BlockPos;offset(III)Lnet/minecraft/core/BlockPos;"))
	private static BlockPos foreverfixes$oldOrigin(BlockPos center, int x, int y, int z, Operation<BlockPos> original)
	{
		return original.call(center, x, y, z).atY(0);
	}

	@WrapOperation(method = "WarpSubLevels", remap = false, require = 1,
			at = @At(value = "INVOKE", ordinal = 1, remap = false, target = "Lnet/minecraft/core/BlockPos;offset(III)Lnet/minecraft/core/BlockPos;"))
	private static BlockPos foreverfixes$newOrigin(BlockPos center, int x, int y, int z, Operation<BlockPos> original,
			@Local(argsOnly = true, ordinal = 1) ServerSubLevelContainer destination)
	{
		final int shift = ShipTransferOrigin.takeShift(destination.getLevel());
		foreverfixes$LOGGER.info("Ship transfer: vertical shift {} into {}", shift, destination.getLevel().dimension().location());
		return original.call(center, x, y, z).atY(shift);
	}

	/**
	 * Entities to carry along are gathered from a box of exactly the ship's size around its position; the intended 1-block margin
	 * is computed and discarded ({@code aabb.inflate(1)} without using the result). Sable finds the ship's own entities (seats,
	 * passengers' mounts) by intersecting that box with the ship's stored bounds, which trail a fast ship by up to a tick of travel:
	 * with thrusters on at the transfer height the box misses, the seat entity is left behind and the player is not re-seated. The
	 * box now gets a real margin.
	 */
	@WrapOperation(method = "WarpSubLevels", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/server/level/ServerLevel;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
	private static List<Entity> foreverfixes$gatherWithMargin(ServerLevel level, Entity except, AABB box, Operation<List<Entity>> original)
	{
		return original.call(level, except, box.inflate(ShipTransferOrigin.GATHER_MARGIN));
	}

	/** Diagnostic (1.14.1): every entity Dimensional Sable carries along, where to, and whether the teleport worked. */
	@WrapOperation(method = "TeleportEntity", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/world/entity/Entity;teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z"))
	private static boolean foreverfixes$logTeleport(Entity entity, ServerLevel level, double x, double y, double z, Set<?> relative,
			float yaw, float pitch, Operation<Boolean> original)
	{
		final String from = String.format("%.1f %.1f %.1f in %s", entity.getX(), entity.getY(), entity.getZ(), entity.level().dimension().location());
		final boolean result = original.call(entity, level, x, y, z, relative, yaw, pitch);
		foreverfixes$LOGGER.info("Ship transfer: entity {} {} from {} to {} {} {} in {}: {}", entity.getType().toShortString(), entity.getUUID(),
				from, String.format("%.1f", x), String.format("%.1f", y), String.format("%.1f", z), level.dimension().location(), result);
		return result;
	}
}
