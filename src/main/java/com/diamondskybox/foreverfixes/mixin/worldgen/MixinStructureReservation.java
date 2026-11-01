package com.diamondskybox.foreverfixes.mixin.worldgen;

import com.diamondskybox.foreverfixes.StructureReservations;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * A laid-out structure that would overlap a higher-ranked priority structure reserved nearby is not placed (StructureReservations).
 * Runs on the finished pieces, so after the floating clearance has moved them.
 */
@Mixin(value = Structure.class, remap = false)
public abstract class MixinStructureReservation
{
	@WrapOperation(method = "generate", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/world/level/levelgen/structure/pieces/StructurePiecesBuilder;build()Lnet/minecraft/world/level/levelgen/structure/pieces/PiecesContainer;"))
	private PiecesContainer foreverfixes$keepReservations(StructurePiecesBuilder builder, Operation<PiecesContainer> original,
			RegistryAccess registryAccess, ChunkGenerator generator, BiomeSource biomeSource, RandomState randomState,
			StructureTemplateManager templateManager, long seed, ChunkPos chunkPos, int references, LevelHeightAccessor level)
	{
		final PiecesContainer pieces = original.call(builder);
		if (pieces.isEmpty())
		{
			return pieces;
		}
		final Structure self = (Structure) (Object) this;
		if (StructureReservations.blocked(self, registryAccess.registryOrThrow(Registries.STRUCTURE).getKey(self), pieces, registryAccess,
				generator, biomeSource, randomState, templateManager, seed, chunkPos, level))
		{
			return new PiecesContainer(List.of());
		}
		return pieces;
	}
}
