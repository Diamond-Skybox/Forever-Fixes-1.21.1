package com.diamondskybox.foreverfixes.mixin.nostructureoverlap;

import com.diamondskybox.foreverfixes.StructureReservations;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * No Structure Overlap 3.0.1 remembers every structure start it accepts and blocks later ones that touch it. Trial layouts of
 * StructureReservations are never placed, so they must not be remembered; priority structures are accepted without being remembered,
 * because StructureReservations already keeps everything out of their way and orders them among themselves.
 */
@Mixin(targets = "com.ankin.nostructureoverlap.StructureBoundingBoxTracker", remap = false)
public abstract class MixinStructureBoundingBoxTracker
{
	@Inject(method = "tryAccept", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
	private static void foreverfixes$leavePriorityStructures(ResourceKey<Level> dimension, ResourceLocation structureId, StructureStart start,
			CallbackInfoReturnable<Boolean> cir)
	{
		if (StructureReservations.inTrial() || StructureReservations.isPriority(structureId))
		{
			cir.setReturnValue(true);
		}
	}
}
