package com.diamondskybox.foreverfixes.mixin.simulatedcoasters;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Simulated Coasters clears its track placement preview outlines every client tick while no coaster track is held: 4 "start" and
 * 4095 "curve" outline keys, each a new Pair removed from Create's synchronized outliner map (spark: ~0.4% of the render thread).
 * Once cleared, the clear is skipped until one of the methods that draws those outlines runs again.
 */
@Mixin(targets = "dev.silvergold.simulatedcoasters.track.CoasterTrackPlacement", remap = false)
public abstract class MixinCoasterTrackPlacement
{
	/** No preview outline drawn since the last clear. */
	@Unique
	private static boolean foreverfixes$cleared;

	@Inject(method = {"drawCurvePreviewInner", "drawOutlineRailLine", "line", "showAnchorBlockOutline", "showTargetBlockOutline"},
			remap = false, require = 5, at = @At("HEAD"))
	private static void foreverfixes$outlineDrawn(CallbackInfo ci)
	{
		foreverfixes$cleared = false;
	}

	@Inject(method = "clearCoasterTrackPlacementPreviewOutlines", remap = false, require = 1, at = @At("HEAD"), cancellable = true)
	private static void foreverfixes$skipRepeatedClear(CallbackInfo ci)
	{
		if (foreverfixes$cleared && ForeverFixesConfig.enabled(ForeverFixesConfig.COASTERS_SKIP_REPEATED_CLEAR))
		{
			ci.cancel();
		}
	}

	@Inject(method = "clearCoasterTrackPlacementPreviewOutlines", remap = false, require = 1, at = @At("RETURN"))
	private static void foreverfixes$markCleared(CallbackInfo ci)
	{
		foreverfixes$cleared = true;
	}
}
