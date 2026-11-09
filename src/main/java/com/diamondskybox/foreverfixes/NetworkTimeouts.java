package com.diamondskybox.foreverfixes;

/**
 * The "Timed out" disconnect limits. Vanilla hard-codes two: Netty drops a connection that has been silent for 30 s (server and
 * client each watch the other), and the server drops a player who hasn't answered a keep-alive within 15 s. A client that stalls
 * for half a minute (shader compile, a big Sable assembly, a long GC) is thrown off the server and has to log back in. Both limits
 * follow {@code network.timeoutSeconds}; 0 turns them off entirely (a dead connection then lasts until TCP itself gives up).
 */
public final class NetworkTimeouts
{
	private static final int VANILLA_READ_SECONDS = 30;
	private static final long VANILLA_KEEPALIVE_MILLIS = 15000L;

	private NetworkTimeouts()
	{
	}

	private static int configured()
	{
		try
		{
			if (ForeverFixesCommonConfig.SPEC.isLoaded())
			{
				return Math.max(0, ForeverFixesCommonConfig.NETWORK_TIMEOUT_SECONDS.get());
			}
		}
		catch (RuntimeException ignored)
		{
			// config not available yet: vanilla values
		}
		return -1;
	}

	/** Seconds for Netty's read timeout; 0 disables it. */
	public static int readTimeoutSeconds()
	{
		final int seconds = configured();
		return seconds < 0 ? VANILLA_READ_SECONDS : seconds;
	}

	/** Milliseconds the server waits for a keep-alive answer. */
	public static long keepAliveLimitMillis()
	{
		final int seconds = configured();
		if (seconds < 0)
		{
			return VANILLA_KEEPALIVE_MILLIS;
		}
		return seconds == 0 ? Long.MAX_VALUE / 4 : Math.max(VANILLA_KEEPALIVE_MILLIS, seconds * 1000L);
	}
}
