package com.diamondskybox.foreverfixes;

/**
 * Set while Mo' Bends measures a vanilla model part for its adaptive geometry (it renders the part's cubes into a capture
 * buffer once per model and derives the elbow / knee pivots and the split meshes from the result). Chromeware rewrites how
 * those parts draw while a player renders (Mantis forearm cutout, hidden cyber limbs, shortened stumps), so a measurement taken
 * inside one of its render scopes would measure the cutout instead of the arm; Chromeware's rules are suspended while this is set.
 */
public final class GeometryCapture
{
	private static final ThreadLocal<int[]> DEPTH = ThreadLocal.withInitial(() -> new int[1]);

	private GeometryCapture()
	{
	}

	public static void begin()
	{
		DEPTH.get()[0]++;
	}

	public static void end()
	{
		final int[] depth = DEPTH.get();
		if (depth[0] > 0)
		{
			depth[0]--;
		}
	}

	public static boolean active()
	{
		return DEPTH.get()[0] > 0;
	}
}
