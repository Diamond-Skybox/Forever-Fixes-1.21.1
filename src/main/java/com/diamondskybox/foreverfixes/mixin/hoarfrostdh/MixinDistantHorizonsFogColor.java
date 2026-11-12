package com.diamondskybox.foreverfixes.mixin.hoarfrostdh;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Hoarfrost + Distant Horizons: the LODs fog to the same colour as the chunks. Hoarfrost gives DH its snow haze distances, then
 * blends DH's own fog colour toward the haze colour it put on the chunks by the haze strength, so at anything below full strength
 * (ordinary snow, a partly snowy survey, high sky view) DH's lighter colour showed through: a pale wall of LOD silhouettes against
 * the sky Hoarfrost had darkened, while the chunks in front faded to a darker grey. The blend is always full, so the LODs beyond
 * the haze take exactly the colour the chunks beyond it take.
 */
@Mixin(targets = "com.pockyl.hoarfrost.client.compat.DistantHorizonsFog", remap = false)
public abstract class MixinDistantHorizonsFogColor
{
	@WrapOperation(method = "apply", remap = false, require = 1,
			at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(FF)F"))
	private static float foreverfixes$lodsTakeTheChunkFogColor(float one, float strength, Operation<Float> original)
	{
		return 1.0F;
	}
}
