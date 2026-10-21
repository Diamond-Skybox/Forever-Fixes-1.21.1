package com.diamondskybox.foreverfixes.mixin.sable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
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
 * Sable's {@code ServerLevelPlot.save} keys a ship's chunk sections by their index in the section array, and {@code load}
 * writes each section back at the same index of the destination chunk, while block entities keep their real Y. Loading a
 * plot in a dimension whose bottom differs from the one it was saved in (Create: Cosmonautics' ship library saving in the
 * overworld, min Y -64, and loading in the Twilight Forest, min Y -32) moves every block by the difference but leaves the
 * block-entity data behind, so typewriter bindings, Redstone Link frequencies, inventories and so on come back empty.
 * <p>
 * The keys stay exactly as Sable writes them, so saved data remains readable without this mod; save additionally records the
 * source dimension's lowest section, and load shifts each section by the difference to the destination. Same dimension means
 * no shift (Sable's own world storage is unaffected); data without the note loads exactly as before.
 */
@Mixin(targets = "dev.ryanhcode.sable.sublevel.plot.ServerLevelPlot", remap = false)
public abstract class MixinServerLevelPlot
{
	@Unique
	private static final String foreverfixes$MIN_SECTION = "foreverfixes_min_section";
	@Unique
	private static final Logger foreverfixes$LOGGER = LoggerFactory.getLogger("ForeverFixes");

	@Inject(method = "save", remap = false, require = 1, at = @At("RETURN"))
	private void foreverfixes$recordMinSection(CallbackInfoReturnable<CompoundTag> cir, @Local ServerLevel level)
	{
		final CompoundTag tag = cir.getReturnValue();
		if (tag != null)
		{
			tag.putInt(foreverfixes$MIN_SECTION, level.getMinSection());
		}
	}

	@WrapOperation(method = "load", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Ljava/lang/Integer;parseInt(Ljava/lang/String;)I"))
	private int foreverfixes$shiftSectionIndex(String key, Operation<Integer> original,
			@Local(argsOnly = true) CompoundTag tag, @Local LevelChunk chunk)
	{
		final int index = original.call(key);
		if (!tag.contains(foreverfixes$MIN_SECTION, Tag.TAG_INT))
		{
			return index;
		}
		final int shifted = index + tag.getInt(foreverfixes$MIN_SECTION) - chunk.getMinSection();
		final int last = chunk.getSectionsCount() - 1;
		if (shifted < 0 || shifted > last)
		{
			foreverfixes$LOGGER.warn("Plot section {} (saved with min section {}) does not fit into this dimension (sections {}..{}); clamped",
					index, tag.getInt(foreverfixes$MIN_SECTION), chunk.getMinSection(), chunk.getMaxSection() - 1);
			return Mth.clamp(shifted, 0, last);
		}
		return shifted;
	}
}
