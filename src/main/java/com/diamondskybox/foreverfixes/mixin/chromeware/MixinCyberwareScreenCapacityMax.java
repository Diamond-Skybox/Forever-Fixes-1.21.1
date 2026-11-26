package com.diamondskybox.foreverfixes.mixin.chromeware;

import com.diamondskybox.foreverfixes.CreateAddonAdvancements;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Chromeware's capacity tooltip ("Create advancements: +X / +Y capacity"): X is the capacity bonus the player holds, which includes
 * the Create-addon advancements Forever Fixes counts (see CreateAddonAdvancements), but Y was Chromeware's own most (68 Create
 * advancements x 4 = 272), so it could read +400 / +272. Y now includes the addon cap.
 */
@Mixin(targets = "com.chromeware.client.cyberware.CyberwareScreen", remap = false)
public abstract class MixinCyberwareScreenCapacityMax
{
	@ModifyExpressionValue(method = "capacityTooltip", remap = false, require = 1,
			at = @At(value = "FIELD", remap = false, target = "Lcom/chromeware/cyberware/CapacityProgress;MAX_BONUS:I"))
	private int foreverfixes$withAddonCap(int chromewareMax)
	{
		return chromewareMax + CreateAddonAdvancements.maxBonus();
	}
}
