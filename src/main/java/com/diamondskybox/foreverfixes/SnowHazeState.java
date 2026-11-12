package com.diamondskybox.foreverfixes;

/**
 * Whether Petrichor's haze drew with snow in it last frame (see {@code SnowHaze}), for Hoarfrost's fog, which then keeps out of the
 * way. Kept free of Petrichor and Hoarfrost classes so either mod's mixins can read it.
 */
public final class SnowHazeState
{
	/** when Petrichor's haze last drew with a share of snow in it (System.nanoTime), 0 if not */
	private static volatile long coveredAt;

	public static void recordCovered(boolean covered)
	{
		coveredAt = covered ? System.nanoTime() : 0L;
	}

	/** the haze covered snow within the last 200 ms (a frame or few; stale when Petrichor stops drawing its haze at all) */
	public static boolean hazeCoversSnow()
	{
		final long at = coveredAt;
		return at != 0L && System.nanoTime() - at < 200_000_000L;
	}

	private SnowHazeState()
	{
	}
}
