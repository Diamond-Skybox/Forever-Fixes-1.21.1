package com.diamondskybox.foreverfixes.mixin.ntgl;

import com.diamondskybox.foreverfixes.DrawTimeTracked;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** DynamicGeoItemRenderer.defaultRender hands every animator its stack right before drawing it; that marks it as drawn. */
@Mixin(targets = "com.nukateam.geo.render.ItemAnimator", remap = false)
public abstract class MixinItemAnimator implements DrawTimeTracked
{
	@Unique
	private long foreverfixes$lastDrawn;

	@Inject(method = "setStack", remap = false, require = 1, at = @At("HEAD"))
	private void foreverfixes$markDrawn(ItemStack stack, CallbackInfo ci)
	{
		foreverfixes$lastDrawn = System.currentTimeMillis();
	}

	@Override
	public long foreverfixes$lastDrawnMillis()
	{
		return foreverfixes$lastDrawn;
	}
}
