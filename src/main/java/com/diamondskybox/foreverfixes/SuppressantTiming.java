package com.diamondskybox.foreverfixes;

/** Chromeware immunosuppressant protection per grade (chromeware.suppressant*Ticks). */
public final class SuppressantTiming
{
	private SuppressantTiming()
	{
	}

	public static int ticks(String grade, int chromeware)
	{
		if (!ForeverFixesCommonConfig.SPEC.isLoaded())
		{
			return chromeware;
		}
		return switch (grade)
		{
			case "STANDARD" -> ForeverFixesCommonConfig.SUPPRESSANT_STANDARD_TICKS.get();
			case "MILITARY" -> ForeverFixesCommonConfig.SUPPRESSANT_MILITARY_TICKS.get();
			default -> chromeware;
		};
	}
}
