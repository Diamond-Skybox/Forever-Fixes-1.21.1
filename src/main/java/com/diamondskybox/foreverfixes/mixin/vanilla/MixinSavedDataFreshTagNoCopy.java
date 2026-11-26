package com.diamondskybox.foreverfixes.mixin.vanilla;

import com.diamondskybox.foreverfixes.ForeverFixesCommonConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * NeoForge writes saved data on an IO thread and so deep-copies each file's whole tag on the server thread first, in case the
 * SavedData hands out tags it keeps and changes later. Some build every tag new on each save and keep none: MCA's family tree
 * (one entry for every villager there ever was and its generated parents, 218k entries / 57 MB of NBT on the server) took about
 * half a second each autosave, most of it that copy. For the classes in saving.savedDataWithoutCopy the copy is left out.
 */
@Mixin(value = SavedData.class, remap = false)
public abstract class MixinSavedDataFreshTagNoCopy
{
	@WrapOperation(method = "save(Ljava/io/File;Lnet/minecraft/core/HolderLookup$Provider;)V", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/nbt/CompoundTag;copy()Lnet/minecraft/nbt/CompoundTag;"))
	private CompoundTag foreverfixes$noCopyForFreshTags(CompoundTag tag, Operation<CompoundTag> original)
	{
		return ForeverFixesCommonConfig.savedDataWithoutCopy(this.getClass()) ? tag : original.call(tag);
	}
}
