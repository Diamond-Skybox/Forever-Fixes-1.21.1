package com.diamondskybox.foreverfixes.mixin.worldgen;

import com.diamondskybox.foreverfixes.ForeverFixesCommonConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * Sky islands and airships start at a fixed or random height with no regard for the terrain (terrain_adaptation none), and in the
 * 2096-block JJThunder overworld mountains reach any height they can roll: they were placed half inside mountainsides. For the
 * structures in worldgen.floatingStructures.ids the terrain under the whole footprint is sampled when the start is built, and all
 * pieces are raised together until the lowest block clears the highest ground (or water surface) by the configured clearance. A
 * structure that would then reach past the build limit is not placed. Structures with a height band (worldgen.floatingStructures.
 * heightBands, for those that place themselves in code and ignore a datapack's start_height) are first moved into the band.
 */
@Mixin(value = Structure.class, remap = false)
public abstract class MixinStructureFloatingClearance
{
	@WrapOperation(method = "generate", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/world/level/levelgen/structure/pieces/StructurePiecesBuilder;build()Lnet/minecraft/world/level/levelgen/structure/pieces/PiecesContainer;"))
	private PiecesContainer foreverfixes$clearTerrain(StructurePiecesBuilder builder, Operation<PiecesContainer> original,
			RegistryAccess registryAccess, ChunkGenerator generator, BiomeSource biomeSource, RandomState randomState,
			StructureTemplateManager templateManager, long seed, ChunkPos chunkPos, int references, LevelHeightAccessor level)
	{
		final ResourceLocation id = registryAccess.registryOrThrow(Registries.STRUCTURE).getKey((Structure) (Object) this);
		if (!builder.isEmpty() && ForeverFixesCommonConfig.SPEC.isLoaded() && foreverfixes$isFloating(id))
		{
			final int[] band = foreverfixes$band(id);
			if (band != null)
			{
				final BoundingBox placed = builder.getBoundingBox();
				final WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
				random.setLargeFeatureSeed(seed, chunkPos.x, chunkPos.z);
				final int top = Math.max(band[0], band[1] - placed.getYSpan());
				builder.offsetPiecesVertically(band[0] + random.nextInt(top - band[0] + 1) - placed.minY());
			}
			final BoundingBox box = builder.getBoundingBox();
			final int ground = foreverfixes$highestGround(box, generator, level, randomState);
			final int lift = ground + ForeverFixesCommonConfig.FLOATING_STRUCTURES_CLEARANCE.get() - box.minY();
			if (lift > 0)
			{
				if (box.maxY() + lift >= level.getMaxBuildHeight())
				{
					return new PiecesContainer(List.of());
				}
				builder.offsetPiecesVertically(lift);
			}
		}
		return original.call(builder);
	}

	private static boolean foreverfixes$isFloating(ResourceLocation id)
	{
		if (id == null)
		{
			return false;
		}
		final String full = id.toString();
		final String anyOfMod = id.getNamespace() + ":*";
		for (String entry : ForeverFixesCommonConfig.FLOATING_STRUCTURES.get())
		{
			if (entry.equals(full) || entry.equals(anyOfMod))
			{
				return true;
			}
		}
		return false;
	}

	/** the structure's height band {min, max} from worldgen.floatingStructures.heightBands, or null */
	private static int[] foreverfixes$band(ResourceLocation id)
	{
		final String full = id.toString();
		final String anyOfMod = id.getNamespace() + ":*";
		for (String entry : ForeverFixesCommonConfig.FLOATING_STRUCTURE_BANDS.get())
		{
			final int eq = entry.indexOf('=');
			final String key = entry.substring(0, eq);
			if (key.equals(full) || key.equals(anyOfMod))
			{
				final String[] range = entry.substring(eq + 1).split("\\.\\.");
				final int a = Integer.parseInt(range[0]);
				final int b = Integer.parseInt(range[1]);
				return new int[] { Math.min(a, b), Math.max(a, b) };
			}
		}
		return null;
	}

	/** highest first free block (terrain or water surface) on a grid over the box's footprint, edges included */
	private static int foreverfixes$highestGround(BoundingBox box, ChunkGenerator generator, LevelHeightAccessor level, RandomState randomState)
	{
		final int stepX = Math.max(8, box.getXSpan() / 24);
		final int stepZ = Math.max(8, box.getZSpan() / 24);
		int highest = level.getMinBuildHeight();
		for (int x = box.minX(); ; x = Math.min(x + stepX, box.maxX()))
		{
			for (int z = box.minZ(); ; z = Math.min(z + stepZ, box.maxZ()))
			{
				highest = Math.max(highest, generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, randomState));
				if (z == box.maxZ())
				{
					break;
				}
			}
			if (x == box.maxX())
			{
				break;
			}
		}
		return highest;
	}
}
