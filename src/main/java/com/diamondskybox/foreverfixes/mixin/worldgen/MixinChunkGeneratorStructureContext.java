package com.diamondskybox.foreverfixes.mixin.worldgen;

import com.diamondskybox.foreverfixes.StructureReservations;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hands the level's structure placement state to StructureReservations while a chunk's structure starts are created. */
@Mixin(value = ChunkGenerator.class, remap = false)
public abstract class MixinChunkGeneratorStructureContext
{
	@Inject(method = "createStructures", at = @At("HEAD"), remap = false, require = 1)
	private void foreverfixes$enter(RegistryAccess registryAccess, ChunkGeneratorStructureState state, StructureManager structureManager,
			ChunkAccess chunk, StructureTemplateManager templateManager, CallbackInfo ci)
	{
		StructureReservations.enter(state);
	}

	@Inject(method = "createStructures", at = @At("RETURN"), remap = false, require = 1)
	private void foreverfixes$exit(RegistryAccess registryAccess, ChunkGeneratorStructureState state, StructureManager structureManager,
			ChunkAccess chunk, StructureTemplateManager templateManager, CallbackInfo ci)
	{
		StructureReservations.exit();
	}
}
