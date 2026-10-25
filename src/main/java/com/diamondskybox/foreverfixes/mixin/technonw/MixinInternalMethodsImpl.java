package com.diamondskybox.foreverfixes.mixin.technonw;

import com.technophobia.technonw.payment.TeleportFareCalculator;
import com.technophobia.technonw.waystones.NumismaticsWarpRequirement;
import net.blay09.mods.waystones.api.WaystoneTeleportContext;
import net.blay09.mods.waystones.api.requirement.WarpRequirement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Technophobia's Create Numismatified Waystones 1.1.0 puts its coin fare on teleports by hooking WaystonesAPI.resolveRequirements
 * (and the synchronous WaystoneTeleportManager.tryTeleport). The waystone screen goes through that API, so it showed the fare and
 * checked the player's coins, but Waystones 21.1.46 builds the server's teleport context in InternalMethodsImpl, which calls its own
 * resolveRequirements, and menu teleports run through tryTeleportAsync: the server kept Waystones' default (free) requirement and
 * never took the coins. The fare is applied where Waystones itself resolves requirements, so every path charges it.
 */
@Mixin(targets = "net.blay09.mods.waystones.InternalMethodsImpl", remap = false)
public abstract class MixinInternalMethodsImpl
{
	@Unique
	private static final TeleportFareCalculator foreverfixes$FARES = new TeleportFareCalculator();

	@Inject(method = "resolveRequirements", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
	private void foreverfixes$numismaticsFare(WaystoneTeleportContext context, CallbackInfoReturnable<WarpRequirement> cir)
	{
		cir.setReturnValue(new NumismaticsWarpRequirement(foreverfixes$FARES.calculateCost(context)));
	}
}
