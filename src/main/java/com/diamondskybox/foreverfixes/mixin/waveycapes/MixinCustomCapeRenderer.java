package com.diamondskybox.foreverfixes.mixin.waveycapes;

import com.diamondskybox.foreverfixes.CapeFlight;
import dev.tr7zw.waveycapes.versionless.sim.BasicSimulation;
import dev.tr7zw.waveycapes.versionless.util.CapePoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * Wavey Capes' renderer places and turns each cape part from the simulated rope points (offsets in modifyPoseStackSimulation,
 * the part angle in getRotation). While Mo' Bends has the body tipped, it gets the points blended towards a straight hang
 * instead, see {@link CapeFlight} (Wavey Capes, tr7zw Protective License, no code copied; port of the 1.12.2 WaveyFlat redirects).
 */
@Mixin(targets = "dev.tr7zw.waveycapes.render.CustomCapeRenderer", remap = false)
public abstract class MixinCustomCapeRenderer
{
	@Redirect(method = {"modifyPoseStackSimulation", "getRotation"}, remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Ldev/tr7zw/waveycapes/versionless/sim/BasicSimulation;getPoints()Ljava/util/List;"))
	private static List<CapePoint> foreverfixes$points(BasicSimulation simulation)
	{
		return CapeFlight.points(simulation);
	}
}
