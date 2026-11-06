package com.diamondskybox.foreverfixes.mixin.chronomancy;

import com.chronomancy.temporal.TimeMagicImmunity;
import com.diamondskybox.foreverfixes.ChromewareHook;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Temporal Stasis and Paradox do not take hold on a player whose Sandevistan keeps them outside stopped time. */
@Mixin(value = TimeMagicImmunity.class, remap = false)
public abstract class MixinTimeMagicImmunity
{
	@Inject(method = "isImmune", remap = false, require = 1, cancellable = true, at = @At("RETURN"))
	private static void foreverfixes$sandevistan(Entity entity, CallbackInfoReturnable<Boolean> cir)
	{
		if (!cir.getReturnValueZ() && entity instanceof Player player && ChromewareHook.outsideStoppedTime(player))
		{
			cir.setReturnValue(true);
		}
	}
}
