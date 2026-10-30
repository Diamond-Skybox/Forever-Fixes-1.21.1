package com.diamondskybox.foreverfixes.mixin.sableflywheel;

import dev.ryanhcode.sable.ActiveSableCompanion;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Sable 2.0.6's Flywheel light storage runs updateLightSections every frame that light changed, on a Flywheel worker the render
 * thread then waits for. For each changed section it looked at all 27 neighbours and, for every neighbour, built a Stream over
 * all lighting scenes to ask whether one tracks it; neighbouring changed sections share most of those neighbours, so the same
 * sections were checked many times (spark: ~8 s of worker time in 25 s, the render thread waiting ~29% of every frame). Same
 * result here: each neighbour section is checked once, with a plain loop over the scenes.
 */
@Mixin(targets = "dev.ryanhcode.sable.neoforge.compatibility.flywheel.SableFlywheelLightStorage", remap = false)
public abstract class MixinSableFlywheelLightStorage
{
	@Shadow(remap = false)
	@Final
	private Int2ObjectMap<Long2IntMap> scene2SectionArenaIndexMap;
	@Shadow(remap = false)
	@Final
	private LongSet updatedSections;
	@Shadow(remap = false)
	private LongSet requestedSections;

	@Shadow(remap = false)
	private void removeUnusedSections()
	{
	}

	@Shadow(remap = false)
	public abstract void collectSection(int scene, long section);

	/**
	 * @author ForeverFixes
	 * @reason same sections collected as Sable 2.0.6, without a Stream per neighbour section and with each neighbour checked once
	 */
	@Overwrite(remap = false)
	private void updateLightSections()
	{
		removeUnusedSections();
		final ActiveSableCompanion helper = Sable.HELPER;
		final ClientLevel level = Minecraft.getInstance().level;
		final ClientSubLevelContainer container = SubLevelContainer.getContainer(level);
		final Int2ObjectMap<LongSet> sectionsToCollect = new Int2ObjectOpenHashMap<>();
		if (requestedSections != null)
		{
			for (LongIterator it = requestedSections.iterator(); it.hasNext(); )
			{
				final long section = it.nextLong();
				int scene = 0;
				if (helper.getContaining(level, SectionPos.of(section)) instanceof ClientSubLevel clientSubLevel)
				{
					final int id = container.getLightingSceneId(clientSubLevel);
					if (id != -1)
					{
						scene = id;
					}
				}
				sectionsToCollect.computeIfAbsent(scene, x -> new LongOpenHashSet()).add(section);
			}
		}
		for (Int2ObjectMap.Entry<Long2IntMap> entry : scene2SectionArenaIndexMap.int2ObjectEntrySet())
		{
			final LongSet requested = sectionsToCollect.get(entry.getIntKey());
			if (requested != null)
			{
				requested.removeAll(entry.getValue().keySet());
			}
		}
		final LongSet neighbours = new LongOpenHashSet(updatedSections.size() * 8);
		for (LongIterator it = updatedSections.iterator(); it.hasNext(); )
		{
			final long updated = it.nextLong();
			for (int x = -1; x <= 1; x++)
			{
				for (int y = -1; y <= 1; y++)
				{
					for (int z = -1; z <= 1; z++)
					{
						neighbours.add(SectionPos.offset(updated, x, y, z));
					}
				}
			}
		}
		for (LongIterator it = neighbours.iterator(); it.hasNext(); )
		{
			final long section = it.nextLong();
			final SubLevel subLevel = helper.getContaining(level, SectionPos.of(section));
			if (subLevel instanceof ClientSubLevel clientSubLevel)
			{
				final int id = container.getLightingSceneId(clientSubLevel);
				if (id != -1)
				{
					final Long2IntMap map = scene2SectionArenaIndexMap.get(id);
					if (map != null && map.containsKey(section))
					{
						sectionsToCollect.computeIfAbsent(id, x -> new LongOpenHashSet()).add(section);
					}
					continue;
				}
			}
			for (Long2IntMap map : scene2SectionArenaIndexMap.values())
			{
				if (map.containsKey(section))
				{
					sectionsToCollect.computeIfAbsent(0, x -> new LongOpenHashSet()).add(section);
					break;
				}
			}
		}
		for (Int2ObjectMap.Entry<LongSet> entry : sectionsToCollect.int2ObjectEntrySet())
		{
			final int scene = entry.getIntKey();
			for (LongIterator it = entry.getValue().iterator(); it.hasNext(); )
			{
				collectSection(scene, it.nextLong());
			}
		}
		updatedSections.clear();
		requestedSections = null;
	}
}
