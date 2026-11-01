package com.diamondskybox.foreverfixes;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Priority structures (worldgen.priorityStructures.ids) keep their place. Where structures start follows from the seed, so before a
 * structure is accepted, the priority structures due within worldgen.priorityStructures.radius chunks are laid out the way
 * ChunkGenerator.createStructures lays them out (once per position, cached), and a structure whose pieces would overlap one that ranks
 * higher is not placed. Rank is the position in the list (earlier wins, equal rank: the lower chunk position wins); all other
 * structures rank below every listed one and are left to each other. The outcome does not depend on the order chunks generate in.
 * <p>
 * A reservation follows the real selection one step further: a structure that would itself be left out for a higher-ranked one
 * reserves nothing, and its structure set moves on to its next pick, as ChunkGenerator.createStructures does (a fortress blocked by a
 * better fortress makes room for the bastion of the same set). The higher-ranked layouts used for that step are taken as they come,
 * which keeps the look-ahead bounded. Alternatives of the structure set being generated, in the same chunk, never block each other.
 * <p>
 * The layouts are computed through Structure.generate, so other mods' changes to it (Forever Fixes' floating clearance) apply to them
 * too. No Structure Overlap must not count those trial layouts, and is told to accept priority structures without tracking them
 * (mixin.nostructureoverlap), since this class decides their overlaps.
 */
public final class StructureReservations
{
	/** generator state of the chunk whose structure starts are being created on this thread (set around ChunkGenerator.createStructures) */
	private static final ThreadLocal<ChunkGeneratorStructureState> STATE = new ThreadLocal<>();
	/** above 0 while a trial layout runs on this thread */
	private static final ThreadLocal<int[]> TRIAL = ThreadLocal.withInitial(() -> new int[1]);
	private static final Map<ChunkGeneratorStructureState, Cache> CACHES = Collections.synchronizedMap(new WeakHashMap<>());
	private static final int MAX_CACHED = 200_000;
	private static final int UNRANKED = Integer.MAX_VALUE;

	private StructureReservations()
	{
	}

	public static void enter(ChunkGeneratorStructureState state)
	{
		STATE.set(state);
	}

	public static void exit()
	{
		STATE.remove();
	}

	/** a trial layout is running on this thread: whatever it produces is never placed */
	public static boolean inTrial()
	{
		return TRIAL.get()[0] > 0;
	}

	public static boolean isPriority(ResourceLocation id)
	{
		return enabled() && rank(id) != UNRANKED;
	}

	private static boolean enabled()
	{
		return ForeverFixesCommonConfig.SPEC.isLoaded() && !ForeverFixesCommonConfig.PRIORITY_STRUCTURES.get().isEmpty();
	}

	/** position of the first worldgen.priorityStructures.ids entry naming the structure ("namespace:*" names all of a mod's) */
	private static int rank(ResourceLocation id)
	{
		if (id == null)
		{
			return UNRANKED;
		}
		final String full = id.toString();
		final String anyOfMod = id.getNamespace() + ":*";
		final List<? extends String> ids = ForeverFixesCommonConfig.PRIORITY_STRUCTURES.get();
		for (int i = 0; i < ids.size(); i++)
		{
			if (ids.get(i).equals(full) || ids.get(i).equals(anyOfMod))
			{
				return i;
			}
		}
		return UNRANKED;
	}

	/** true when the laid-out structure would overlap a higher-ranked structure reserved nearby, so it must not be placed */
	public static boolean blocked(Structure structure, ResourceLocation id, PiecesContainer pieces, RegistryAccess registryAccess,
			ChunkGenerator generator, BiomeSource biomeSource, RandomState randomState, StructureTemplateManager templateManager, long seed,
			ChunkPos chunkPos, LevelHeightAccessor level)
	{
		final ChunkGeneratorStructureState state = STATE.get();
		if (state == null || inTrial() || pieces.isEmpty() || !enabled())
		{
			return false;
		}
		final Cache cache = cache(state);
		if (cache.prioritySets.isEmpty())
		{
			return false;
		}
		final List<BoundingBox> boxes = new ArrayList<>();
		for (StructurePiece piece : pieces.pieces())
		{
			boxes.add(piece.getBoundingBox());
		}
		final Trial trial = new Trial(state, registryAccess, generator, biomeSource, randomState, templateManager, seed, level);
		// A priority structure is checked against the same unrefined layouts its own reservation was decided with, so a structure that
		// reserved nothing is never placed; other structures see the refined reservations.
		final int rank = rank(id);
		final Reservation reserved = cache.conflict(structure, id, rank, chunkPos, boxes, trial, rank == UNRANKED);
		if (reserved != null && ForeverFixesCommonConfig.PRIORITY_STRUCTURES_LOG.get())
		{
			ForeverFixes.LOGGER.info("Priority structures: {} at chunk {} not placed, it would overlap {} reserved at chunk {}",
					id, chunkPos, reserved.id, new ChunkPos(reserved.chunk));
		}
		return reserved != null;
	}

	private static boolean outranks(Reservation reserved, int rank, ResourceLocation id, ChunkPos chunkPos)
	{
		if (reserved.rank != rank)
		{
			return reserved.rank < rank;
		}
		final int byPosition = Long.compare(reserved.chunk, chunkPos.toLong());
		return byPosition != 0 ? byPosition < 0 : reserved.id.toString().compareTo(String.valueOf(id)) < 0;
	}

	private static Cache cache(ChunkGeneratorStructureState state)
	{
		synchronized (CACHES)
		{
			return CACHES.computeIfAbsent(state, Cache::new);
		}
	}

	/** arguments Structure.generate needs, taken from the structure being checked (same level, same generator) */
	private record Trial(ChunkGeneratorStructureState state, RegistryAccess registryAccess, ChunkGenerator generator, BiomeSource biomeSource, RandomState randomState,
			StructureTemplateManager templateManager, long seed, LevelHeightAccessor level)
	{
	}

	private record Reservation(Structure structure, ResourceLocation id, int rank, long chunk, List<BoundingBox> boxes)
	{
		boolean overlaps(List<BoundingBox> others, int margin)
		{
			for (BoundingBox box : others)
			{
				for (BoundingBox reserved : boxes)
				{
					if (reserved.inflatedBy(margin).intersects(box))
					{
						return true;
					}
				}
			}
			return false;
		}
	}

	private record Key(Holder<StructureSet> set, long chunk, boolean refined)
	{
	}

	/** per level: the structure sets holding priority structures, and the trial layouts done so far */
	private static final class Cache
	{
		final List<Holder<StructureSet>> prioritySets;
		/** best (lowest) rank among each priority set's structures; a set can only block structures ranked the same or below */
		final Map<Holder<StructureSet>, Integer> bestRank = new ConcurrentHashMap<>();
		final Map<Key, Optional<Reservation>> layouts = new ConcurrentHashMap<>();

		/** the priority list is read once per level; changing it needs a restart */
		Cache(ChunkGeneratorStructureState state)
		{
			final List<Holder<StructureSet>> sets = new ArrayList<>();
			for (Holder<StructureSet> set : state.possibleStructureSets())
			{
				int best = UNRANKED;
				for (StructureSet.StructureSelectionEntry entry : set.value().structures())
				{
					best = Math.min(best, rank(entry.structure().unwrapKey().map(ResourceKey::location).orElse(null)));
				}
				if (best != UNRANKED)
				{
					sets.add(set);
					bestRank.put(set, best);
				}
			}
			prioritySets = List.copyOf(sets);
		}

		/**
		 * The reserved structure that the given boxes would overlap and that outranks the structure, or null. Refined: reservations of
		 * structures that would themselves be left out do not count (one step of look-ahead); otherwise layouts are taken as they come.
		 */
		Reservation conflict(Structure structure, ResourceLocation id, int rank, ChunkPos chunkPos, List<BoundingBox> boxes, Trial trial,
				boolean refined)
		{
			if (rank == 0 || boxes.isEmpty())
			{
				return null;
			}
			final BoundingBox box = BoundingBox.encapsulatingBoxes(boxes).orElseThrow();
			final int radius = ForeverFixesCommonConfig.PRIORITY_STRUCTURES_RADIUS.get();
			final int margin = ForeverFixesCommonConfig.PRIORITY_STRUCTURES_MARGIN.get();
			final int minX = SectionPos.blockToSectionCoord(box.minX()) - radius;
			final int maxX = SectionPos.blockToSectionCoord(box.maxX()) + radius;
			final int minZ = SectionPos.blockToSectionCoord(box.minZ()) - radius;
			final int maxZ = SectionPos.blockToSectionCoord(box.maxZ()) + radius;
			for (Holder<StructureSet> set : prioritySets)
			{
				if (bestRank.get(set) > rank)
				{
					continue;
				}
				final boolean ownSet = contains(set, structure);
				final StructurePlacement placement = set.value().placement();
				for (int x = minX; x <= maxX; x++)
				{
					for (int z = minZ; z <= maxZ; z++)
					{
						if ((ownSet && x == chunkPos.x && z == chunkPos.z) || !placement.isStructureChunk(trial.state(), x, z))
						{
							continue;
						}
						final Reservation reserved = layOut(set, x, z, trial, refined);
						if (reserved != null && reserved.rank != UNRANKED && outranks(reserved, rank, id, chunkPos)
								&& reserved.overlaps(boxes, margin))
						{
							return reserved;
						}
					}
				}
			}
			return null;
		}

		private static boolean contains(Holder<StructureSet> set, Structure structure)
		{
			for (StructureSet.StructureSelectionEntry entry : set.value().structures())
			{
				if (entry.structure().value() == structure)
				{
					return true;
				}
			}
			return false;
		}

		/** what ChunkGenerator.createStructures would start for this set in this chunk, or null for nothing */
		Reservation layOut(Holder<StructureSet> set, int x, int z, Trial trial, boolean refined)
		{
			final Key key = new Key(set, ChunkPos.asLong(x, z), refined);
			final Optional<Reservation> done = layouts.get(key);
			if (done != null)
			{
				return done.orElse(null);
			}
			final Reservation result;
			TRIAL.get()[0]++;
			try
			{
				result = select(set.value().structures(), new ChunkPos(x, z), trial, refined);
			}
			finally
			{
				TRIAL.get()[0]--;
			}
			if (layouts.size() > MAX_CACHED)
			{
				layouts.clear();
			}
			layouts.put(key, Optional.ofNullable(result));
			return result;
		}

		/** the selection of ChunkGenerator.createStructures: a single structure, or weighted picks until one generates */
		private Reservation select(List<StructureSet.StructureSelectionEntry> entries, ChunkPos pos, Trial trial, boolean refined)
		{
			if (entries.size() == 1)
			{
				return generate(entries.get(0), pos, trial, refined);
			}
			final List<StructureSet.StructureSelectionEntry> left = new ArrayList<>(entries);
			final WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
			random.setLargeFeatureSeed(trial.seed(), pos.x, pos.z);
			int total = 0;
			for (StructureSet.StructureSelectionEntry entry : left)
			{
				total += entry.weight();
			}
			while (!left.isEmpty())
			{
				int roll = random.nextInt(total);
				int index = 0;
				for (StructureSet.StructureSelectionEntry entry : left)
				{
					roll -= entry.weight();
					if (roll < 0)
					{
						break;
					}
					index++;
				}
				final StructureSet.StructureSelectionEntry picked = left.get(index);
				final Reservation result = generate(picked, pos, trial, refined);
				if (result != null)
				{
					return result;
				}
				left.remove(index);
				total -= picked.weight();
			}
			return null;
		}

		/** the structure this pick would start, or null when it does not generate (or, refined, would be left out for a higher rank) */
		private Reservation generate(StructureSet.StructureSelectionEntry entry, ChunkPos pos, Trial trial, boolean refined)
		{
			final Structure structure = entry.structure().value();
			final StructureStart start = structure.generate(trial.registryAccess(), trial.generator(), trial.biomeSource(), trial.randomState(),
					trial.templateManager(), trial.seed(), pos, 0, trial.level(), structure.biomes()::contains);
			if (!start.isValid())
			{
				return null;
			}
			final ResourceLocation id = trial.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(structure);
			final List<BoundingBox> boxes = new ArrayList<>();
			for (StructurePiece piece : start.getPieces())
			{
				boxes.add(piece.getBoundingBox());
			}
			final Reservation reservation = new Reservation(structure, id, rank(id), pos.toLong(), List.copyOf(boxes));
			if (refined && conflict(structure, id, reservation.rank, pos, reservation.boxes, trial, false) != null)
			{
				return null;
			}
			return reservation;
		}
	}
}
