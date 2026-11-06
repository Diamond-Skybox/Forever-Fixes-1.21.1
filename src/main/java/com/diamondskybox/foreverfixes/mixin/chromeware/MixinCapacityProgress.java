package com.diamondskybox.foreverfixes.mixin.chromeware;

import com.diamondskybox.foreverfixes.CreateAddonAdvancements;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Advancements of other Create-based mods also raise the cyberware capacity limit (see CreateAddonAdvancements). */
@Mixin(targets = "com.chromeware.cyberware.CapacityProgress", remap = false)
public abstract class MixinCapacityProgress
{
	@ModifyVariable(method = "refresh", remap = false, require = 1, at = @At(value = "STORE", ordinal = 0))
	private static int foreverfixes$addonBonus(int bonus, @Local(argsOnly = true) ServerPlayer player)
	{
		return bonus + CreateAddonAdvancements.bonus(player);
	}

	/** Earning one of those advancements recomputes the limit too. */
	@Inject(method = "counts", remap = false, require = 1, cancellable = true, at = @At("RETURN"))
	private static void foreverfixes$addonCounts(ResourceLocation id, CallbackInfoReturnable<Boolean> cir)
	{
		if (!cir.getReturnValueZ() && CreateAddonAdvancements.counts(id))
		{
			cir.setReturnValue(true);
		}
	}
}
