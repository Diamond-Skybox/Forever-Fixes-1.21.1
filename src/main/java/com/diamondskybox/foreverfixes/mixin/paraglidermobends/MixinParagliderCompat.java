package com.diamondskybox.foreverfixes.mixin.paraglidermobends;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import com.diamondskybox.foreverfixes.Paragliders;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mo' Bends has a paragliding pose (both arms up holding the glider), but its compat looks the glider up through
 * tictim.paraglider.api.item.Paraglider, which Paraglider 21.1 no longer has; the failed lookup marks Paraglider as missing and
 * the pose never plays. Answered from Paraglider 21.1's own open-glider test instead ({@link Paragliders}).
 */
@Mixin(targets = "goblinbob.mobends.compat.ParagliderCompat", remap = false)
public abstract class MixinParagliderCompat
{
	@Inject(method = "isParagliding", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
	private static void foreverfixes$isParagliding(LivingEntity entity, CallbackInfoReturnable<Boolean> cir)
	{
		if (ForeverFixesConfig.enabled(ForeverFixesConfig.MOBENDS_PARAGLIDER_POSE))
		{
			cir.setReturnValue(entity != null && Paragliders.isParagliding(entity));
		}
	}
}
