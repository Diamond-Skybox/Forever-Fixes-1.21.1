package com.diamondskybox.foreverfixes.mixin.petrichorhoarfrost;

import com.diamondskybox.foreverfixes.SnowHaze;
import com.diamondskybox.foreverfixes.SnowHazeState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Petrichor: its haze also covers snow, with Hoarfrost's visibility (see {@link SnowHaze}). */
@Mixin(targets = "com.pockyl.petrichor.client.render.Atmosphere", remap = false)
public abstract class MixinAtmosphereSnowHaze
{
	@WrapOperation(method = { "active", "haze", "render" }, remap = false, require = 3,
			at = @At(value = "INVOKE", remap = false, target = "Lcom/pockyl/petrichor/client/ClientWeather;rain()F"))
	private static float foreverfixes$rainOrSnow(Operation<Float> original)
	{
		return SnowHaze.precipitation(original.call());
	}

	@WrapOperation(method = "haze", remap = false, require = 1,
			at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, remap = false,
					target = "Lcom/pockyl/petrichor/client/ClientWeather;visibility:F"))
	private static float foreverfixes$snowVisibility(Operation<Float> original)
	{
		return SnowHaze.visibility(original.call());
	}

	@Inject(method = "active", remap = false, require = 1, at = @At("RETURN"))
	private static void foreverfixes$recordSnowHaze(CallbackInfoReturnable<Boolean> cir)
	{
		SnowHazeState.recordCovered(cir.getReturnValueZ() && SnowHaze.snowShare() > 0.01F);
	}
}
