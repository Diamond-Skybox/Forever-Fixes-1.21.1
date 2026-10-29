package com.diamondskybox.foreverfixes.mixin.ntgl;

import com.diamondskybox.foreverfixes.DrawTimeTracked;
import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NTGL's renderer keeps one animator per (entity, display context) for every weapon ever drawn, and each animator registers itself
 * in ClientTickHandler's plain HashMap, never removed. Every gun ever seen in a hand, on a back, in an inventory or in a recipe
 * viewer kept running its GeckoLib animation tick for the rest of the session (spark: ~1.4% of the render thread, growing over
 * time; some Create Gunsmithing animators also scan the inventory for backtank air each tick). Animators not drawn for
 * animatorIdleSeconds are skipped until they are drawn again.
 */
@Mixin(targets = "com.nukateam.ntgl.client.handlers.ClientTickHandler", remap = false)
public abstract class MixinClientTickHandler
{
	@Inject(method = "lambda$clientTick$0", remap = false, require = 1, at = @At("HEAD"), cancellable = true)
	private static void foreverfixes$skipUndrawn(@Coerce Object animator, Runnable ticker, CallbackInfo ci)
	{
		if (animator instanceof DrawTimeTracked drawn && ForeverFixesConfig.enabled(ForeverFixesConfig.NTGL_TICK_DRAWN_ONLY)
				&& System.currentTimeMillis() - drawn.foreverfixes$lastDrawnMillis()
						> 1000D * ForeverFixesConfig.value(ForeverFixesConfig.NTGL_IDLE_SECONDS, 5D))
		{
			ci.cancel();
		}
	}
}
