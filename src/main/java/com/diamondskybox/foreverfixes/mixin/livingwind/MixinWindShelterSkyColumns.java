package com.diamondskybox.foreverfixes.mixin.livingwind;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import com.diamondskybox.foreverfixes.LivingWindSkyColumns;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Living Wind: its sky check (shelter survey, cave regions, particle wind exposure) is answered from a per-column cache instead of
 * a block-by-block climb per probe (see {@link LivingWindSkyColumns}). Under a floating island far overhead the climb read the
 * whole gap up to the island for every probe: about half of the render thread in spark. Calls from other threads (async particle
 * ticking) keep Living Wind's own climb.
 */
@Mixin(targets = "com.macatmovie.livingwind.client.WindShelter", remap = false)
public abstract class MixinWindShelterSkyColumns
{
	@Inject(method = "canSeeSkyIgnoringLeaves(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/core/BlockPos;)Z",
			remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private static void foreverfixes$skyFromColumnCache(ClientLevel level, BlockPos at, CallbackInfoReturnable<Boolean> cir)
	{
		if (ForeverFixesConfig.enabled(ForeverFixesConfig.LIVINGWIND_SKY_COLUMN_CACHE) && Minecraft.getInstance().isSameThread())
		{
			cir.setReturnValue(LivingWindSkyColumns.canSeeSkyIgnoringLeaves(level, at));
		}
	}
}
