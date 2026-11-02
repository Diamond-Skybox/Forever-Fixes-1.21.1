package com.diamondskybox.foreverfixes.mixin.jmbeam;

import com.diamondskybox.foreverfixes.StormFogFixes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * JourneyMap + Better Simple Clouds: remembers where the frame's waypoint beams are. They are drawn wholly translucent, with no
 * depth, after Better Simple Clouds' depth snapshot, so the storm fog covered them wherever the sky was behind them (see
 * {@link StormFogFixes}).
 */
@Mixin(targets = "journeymap.client.render.ingame.WaypointBeaconRenderer", remap = false)
public abstract class MixinWaypointBeaconRenderer
{
	@Inject(method = "renderBeamSegment", remap = false, require = 1, at = @At("HEAD"))
	private void foreverfixes$recordBeam(GuiGraphics graphics, MultiBufferSource buffer, float partialTicks, long gameTime, int yOffset, int height,
			float[] colors, float beamRadius, float glowRadius, boolean showStaticInnerBeam, boolean showRotatingOuterBeam, CallbackInfo ci)
	{
		StormFogFixes.recordWaypointBeam(graphics.pose(), partialTicks, gameTime, yOffset, height, beamRadius, glowRadius, showStaticInnerBeam,
				showRotatingOuterBeam);
	}
}
