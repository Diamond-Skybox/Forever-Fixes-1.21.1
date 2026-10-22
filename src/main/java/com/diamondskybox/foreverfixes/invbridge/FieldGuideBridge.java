package com.diamondskybox.foreverfixes.invbridge;

import com.evandev.fieldguide.client.FieldGuideClient;
import com.evandev.fieldguide.config.ClientConfig;

/** Field Guide: its inventory button. */
final class FieldGuideBridge
{
	private FieldGuideBridge()
	{
	}

	static boolean buttonEnabled()
	{
		return ClientConfig.get().showInventoryButton;
	}

	static void open()
	{
		if (FieldGuideClient.canOpenGuide())
		{
			FieldGuideClient.openDefaultScreen();
		}
	}
}
