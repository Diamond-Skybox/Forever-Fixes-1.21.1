package com.diamondskybox.foreverfixes;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/** Corail Tombstone's Blessing draws from holy.blessingEffects instead of every beneficial effect in the game. */
public final class BlessingPool
{
	private BlessingPool()
	{
	}

	/** null = no list configured, use Tombstone's own pick. */
	@SuppressWarnings("unchecked")
	public static <T> Optional<Holder<T>> pick(Registry<T> registry)
	{
		final List<? extends String> ids = ForeverFixesCommonConfig.BLESSING_EFFECTS.get();
		if (ids.isEmpty())
		{
			return null;
		}
		final List<Holder<T>> holders = new ArrayList<>();
		for (String id : ids)
		{
			final ResourceLocation rl = ResourceLocation.tryParse(id);
			if (rl != null)
			{
				registry.getHolder(rl).ifPresent(h -> holders.add((Holder<T>) h));
			}
		}
		return holders.isEmpty() ? Optional.empty() : Optional.of(holders.get(ThreadLocalRandom.current().nextInt(holders.size())));
	}
}
