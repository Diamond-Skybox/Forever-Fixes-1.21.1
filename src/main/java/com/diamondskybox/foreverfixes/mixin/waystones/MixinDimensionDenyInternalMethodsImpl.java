package com.diamondskybox.foreverfixes.mixin.waystones;

import com.diamondskybox.foreverfixes.WaystoneDimensionDeny;
import net.blay09.mods.waystones.api.WaystoneTeleportContext;
import net.blay09.mods.waystones.api.requirement.WarpRequirement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Waystone warps into another dimension get WaystoneDimensionDeny as their requirement (priority 900: before the coin fare mixins). */
@Mixin(targets = "net.blay09.mods.waystones.InternalMethodsImpl", remap = false, priority = 900)
public abstract class MixinDimensionDenyInternalMethodsImpl
{
	@Inject(method = "resolveRequirements", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
	private void foreverfixes$denyDimensional(WaystoneTeleportContext context, CallbackInfoReturnable<WarpRequirement> cir)
	{
		if (WaystoneDimensionDeny.applies(context))
		{
			cir.setReturnValue(WaystoneDimensionDeny.INSTANCE);
		}
	}
}
