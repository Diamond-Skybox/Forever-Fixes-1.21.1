package com.diamondskybox.foreverfixes;

import com.macatmovie.livingwind.api.ClientWindApi;

/** The client's Living Wind state for {@link CloudWind}; client only. */
final class CloudWindClient
{
	private CloudWindClient()
	{
	}

	static float[] vector()
	{
		return ClientWindApi.hasWindState() ? CloudWind.fromHeading(ClientWindApi.directionDegrees(), ClientWindApi.globalStrength()) : null;
	}
}
