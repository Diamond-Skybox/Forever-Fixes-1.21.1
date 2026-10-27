package com.diamondskybox.foreverfixes.mixin.paragliderpunchy;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import com.diamondskybox.foreverfixes.Paragliders;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Punchy places first-person items itself, so an open paraglider stayed in the hand instead of above the head, where
 * Paraglider's open-glider model (paraglider_on, firstperson display) puts it. While the glider is open Punchy treats it as a
 * blacklisted item and leaves that hand to vanilla rendering; the closed glider keeps Punchy's animations.
 */
@Mixin(targets = "punchy.config.PunchyConfig", remap = false)
public abstract class MixinPunchyConfigParaglider
{
	@Inject(method = "isItemBlacklisted", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
	private static void foreverfixes$openParaglider(ItemStack stack, CallbackInfoReturnable<Boolean> cir)
	{
		if (stack != null && ForeverFixesConfig.enabled(ForeverFixesConfig.PUNCHY_PARAGLIDER_OVERHEAD) && Paragliders.isParagliding(stack))
		{
			cir.setReturnValue(true);
		}
	}
}
