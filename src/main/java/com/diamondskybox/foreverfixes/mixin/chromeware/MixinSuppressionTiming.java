package com.diamondskybox.foreverfixes.mixin.chromeware;

import com.diamondskybox.foreverfixes.SuppressantTiming;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Immunosuppressant protection per grade comes from chromeware.suppressant*Ticks (re-doses still add it halved per dose). */
@Mixin(targets = {"com.chromeware.humanity.Suppression", "com.chromeware.humanity.ImmunosuppressantItem"}, remap = false)
public abstract class MixinSuppressionTiming
{
	@WrapOperation(method = "*", remap = false, require = 1,
			at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, remap = false, target = "Lcom/chromeware/humanity/Suppression$Grade;protection:I"))
	private int foreverfixes$protection(com.chromeware.humanity.Suppression.Grade grade, Operation<Integer> original)
	{
		return SuppressantTiming.ticks(grade.name(), original.call(grade));
	}
}
