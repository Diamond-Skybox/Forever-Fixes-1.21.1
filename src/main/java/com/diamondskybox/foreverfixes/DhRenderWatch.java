package com.diamondskybox.foreverfixes;

import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.enums.rendering.EDhApiRenderPass;
import com.seibel.distanthorizons.api.methods.events.DhApiEventRegister;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeApplyShaderRenderEvent;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiCancelableEventParam;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import com.seibel.distanthorizons.api.objects.DhApiResult;

/** Whether Distant Horizons is drawing its LODs right now: rendering is on and its opaque pass ran within the last quarter second. Client only. */
public final class DhRenderWatch
{
	private static final long RECENT_NANOS = 250_000_000L;
	private static volatile long lastRender;
	private static boolean registered;

	private DhRenderWatch()
	{
	}

	public static synchronized void register()
	{
		if (registered)
		{
			return;
		}
		registered = true;
		final DhApiResult<Void> result = DhApiEventRegister.on(DhApiBeforeApplyShaderRenderEvent.class, new DhApiBeforeApplyShaderRenderEvent()
		{
			@Override
			public void beforeRender(DhApiCancelableEventParam<DhApiRenderParam> event)
			{
				final DhApiRenderParam param = event.value;
				if (param != null && param.renderPass != EDhApiRenderPass.TRANSPARENT)
				{
					lastRender = System.nanoTime();
				}
			}
		});
		if (!result.success)
		{
			ForeverFixes.LOGGER.warn("Could not watch Distant Horizons' rendering ({})", result.message);
		}
	}

	public static boolean rendering()
	{
		if (System.nanoTime() - lastRender > RECENT_NANOS)
		{
			return false;
		}
		try
		{
			return DhApi.Delayed.configs == null || DhApi.Delayed.configs.graphics().renderingEnabled().getValue();
		}
		catch (Throwable t)
		{
			return true;
		}
	}
}
