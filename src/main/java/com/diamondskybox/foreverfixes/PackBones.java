package com.diamondskybox.foreverfixes;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Which bones a Bends pack posed on an entity this frame. Mo' Bends runs the pack right before it copies Player Animation Lib
 * poses in, so the set is recorded by the pack layer and consumed once by the Player Animation Lib step (render thread only).
 */
public final class PackBones
{
	private static final Map<Object, Set<String>> APPLIED = new WeakHashMap<>();

	private PackBones()
	{
	}

	public static void record(Object entityData, Iterable<String> bones)
	{
		final Set<String> set = APPLIED.computeIfAbsent(entityData, k -> new HashSet<>());
		for (String bone : bones)
		{
			set.add(bone);
		}
	}

	/** The bones posed since the last take, then forgets them. */
	public static Set<String> take(Object entityData)
	{
		final Set<String> set = APPLIED.remove(entityData);
		return set == null ? Set.of() : set;
	}
}
