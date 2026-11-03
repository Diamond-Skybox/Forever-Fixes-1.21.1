package com.diamondskybox.foreverfixes.mixin.architectury;

import com.diamondskybox.foreverfixes.ForeverFixesCommonConfig;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;

/**
 * Architectury keeps an event's listeners in one ArrayList and its invoker iterates that same list, so a listener registered or
 * removed while the event runs (from inside a listener, or from a mod-loading worker thread while the client ticks during the
 * loading screen) threw a ConcurrentModificationException: the client crashed at the title screen in the client tick event.
 * Changes now go to a copy of the list (copy-on-write), one at a time; an invoker that is running keeps iterating the list it
 * started with, and the next invoker is built from the new one.
 */
@Mixin(targets = "dev.architectury.event.EventFactory$EventImpl", remap = false)
public abstract class MixinEventImpl
{
	@Shadow(remap = false)
	private ArrayList<Object> listeners;

	@WrapMethod(method = "register", remap = false)
	private void foreverfixes$registerOnCopy(Object listener, Operation<Void> original)
	{
		synchronized (this)
		{
			foreverfixes$copyListeners();
			original.call(listener);
		}
	}

	@WrapMethod(method = "unregister", remap = false)
	private void foreverfixes$unregisterOnCopy(Object listener, Operation<Void> original)
	{
		synchronized (this)
		{
			foreverfixes$copyListeners();
			original.call(listener);
		}
	}

	@WrapMethod(method = "clearListeners", remap = false)
	private void foreverfixes$clearOnCopy(Operation<Void> original)
	{
		synchronized (this)
		{
			foreverfixes$copyListeners();
			original.call();
		}
	}

	private void foreverfixes$copyListeners()
	{
		if (ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.ARCHITECTURY_COPY_ON_WRITE_EVENTS))
		{
			this.listeners = new ArrayList<>(this.listeners);
		}
	}
}
