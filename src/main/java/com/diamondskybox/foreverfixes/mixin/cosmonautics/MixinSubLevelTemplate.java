package com.diamondskybox.foreverfixes.mixin.cosmonautics;

import com.diamondskybox.foreverfixes.ShipTransferOrigin;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.egg.DimensionalSable;
import dev.egg.registries.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Dimensional Sable (bundled in Create: Cosmonautics) moves a ship between dimensions by saving its plot chunks with
 * {@code SubLevelTemplate.save} and rebuilding them with {@code SubLevelTemplate.load}. Block entities and passengers are
 * moved relative to an origin of "plot centre at mid build height, plus the dimension's minimum Y", while the chunk sections are
 * keyed by array index and written back at the same index, i.e. relative to the dimension's bottom. Neither matches where a
 * Sable ship actually is (the height it was assembled at), so between dimensions of different bottom or height the blocks,
 * their block entities and the passengers end up in three different places:
 * <ul>
 * <li>Twilight Forest (-32..256) to deep space (-64..320): blocks 32 down, block entities 16; the ship arrives as a broken shell.</li>
 * <li>A 2096-block-tall overworld (JJThunder) to deep space: block entities and passengers about 850 blocks down, below the
 * world, the player in the void; a ship assembled above Y 320 there crashed the server (Cosmonautics issue #116).</li>
 * </ul>
 * Heights are now kept absolute and one vertical shift (see {@link ShipTransferOrigin}, chosen in {@link MixinSubLevelWarper})
 * applies to sections, block entities and passengers alike. Keys stay Dimensional Sable's; templates without the note load as
 * before.
 */
@Mixin(targets = "dev.egg.SubLevelTemplate", remap = false)
public abstract class MixinSubLevelTemplate
{
	@Unique
	private static final Logger foreverfixes$LOGGER = LoggerFactory.getLogger("ForeverFixes");

	@WrapOperation(method = "save", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/core/BlockPos;offset(III)Lnet/minecraft/core/BlockPos;"))
	private static BlockPos foreverfixes$saveOrigin(BlockPos center, int x, int y, int z, Operation<BlockPos> original)
	{
		return original.call(center, x, y, z).atY(0);
	}

	@Inject(method = "save", remap = false, require = 1, at = @At("RETURN"))
	private static void foreverfixes$recordSave(CallbackInfoReturnable<CompoundTag> cir, @Local ServerLevel level)
	{
		final CompoundTag tag = cir.getReturnValue();
		if (tag != null)
		{
			ShipTransferOrigin.recordSave(tag, level.getMinSection());
		}
	}

	@WrapOperation(method = "load", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/core/BlockPos;offset(III)Lnet/minecraft/core/BlockPos;"))
	private static BlockPos foreverfixes$loadOrigin(BlockPos center, int x, int y, int z, Operation<BlockPos> original,
			@Local(argsOnly = true) CompoundTag tag, @Local(argsOnly = true) BlockEntityRegistry.MoveInfo moveInfo)
	{
		final BlockPos origin = original.call(center, x, y, z);
		return tag.contains(ShipTransferOrigin.MIN_SECTION, Tag.TAG_INT) ? origin.atY(foreverfixes$shift(tag, moveInfo)) : origin;
	}

	@WrapOperation(method = "load", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Ljava/lang/Integer;parseInt(Ljava/lang/String;)I"))
	private static int foreverfixes$loadSectionIndex(String key, Operation<Integer> original, @Local(argsOnly = true) CompoundTag tag,
			@Local(argsOnly = true) BlockEntityRegistry.MoveInfo moveInfo, @Local LevelChunk chunk)
	{
		final int index = original.call(key);
		if (!tag.contains(ShipTransferOrigin.MIN_SECTION, Tag.TAG_INT))
		{
			return index;
		}
		final int sectionY = index + tag.getInt(ShipTransferOrigin.MIN_SECTION) + (foreverfixes$shift(tag, moveInfo) >> 4);
		final int shifted = sectionY - chunk.getMinSection();
		final int last = chunk.getSectionsCount() - 1;
		if (shifted < 0 || shifted > last)
		{
			foreverfixes$LOGGER.warn("Ship section at section Y {} does not fit into the destination dimension (sections {}..{}); clamped",
					sectionY, chunk.getMinSection(), chunk.getMaxSection() - 1);
			return Mth.clamp(shifted, 0, last);
		}
		return shifted;
	}

	/** The vertical shift chosen for this sub-level, as recorded in its move offset (0 when unknown). */
	@Unique
	private static int foreverfixes$shift(CompoundTag tag, BlockEntityRegistry.MoveInfo moveInfo)
	{
		if (moveInfo == null || !tag.hasUUID("SubLevelID"))
		{
			return 0;
		}
		final DimensionalSable.Pair<java.util.UUID, Vec3i> move = moveInfo.oldToNewSubLevelMap().get(tag.getUUID("SubLevelID"));
		return move == null ? 0 : move.second().getY();
	}
}
