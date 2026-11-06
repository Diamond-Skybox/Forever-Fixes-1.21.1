package com.diamondskybox.foreverfixes.mixin.chromeware;

import com.diamondskybox.foreverfixes.invbridge.CyberwareButtonBridge;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Chromeware's full-size "Cyberware" button is left off Better Inventory's screen, where a gutter icon button opens the same screen. */
@Mixin(targets = "com.chromeware.client.cyberware.CyberwareMenuClient", remap = false)
public abstract class MixinCyberwareMenuClientButton
{
	@Inject(method = "inventoryButton", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private static void foreverfixes$gutterInstead(ScreenEvent.Init.Post event, CallbackInfo ci)
	{
		if (CyberwareButtonBridge.replacesOwnButton(event.getScreen()))
		{
			ci.cancel();
		}
	}
}
