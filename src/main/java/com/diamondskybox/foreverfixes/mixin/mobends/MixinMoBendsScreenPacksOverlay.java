package com.diamondskybox.foreverfixes.mixin.mobends;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import goblinbob.mobends.core.client.gui.MoBendsScreenBuilder;
import goblinbob.mobends.core.client.gui.view.View;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Mo' Bends 6.0.1 covers its Packs tab with a "WIP" overlay that swallows every click, so packs can't be toggled in the
 * menu even though the list, the toggles and the loader behind them work. The Packs tab is built without the overlay
 * (first withWipOverlay call in buildContent); the Customize tab keeps its overlay.
 */
@Mixin(value = MoBendsScreenBuilder.class, remap = false)
public abstract class MixinMoBendsScreenPacksOverlay
{
	@WrapOperation(method = "buildContent", remap = false, require = 1,
			at = @At(value = "INVOKE", ordinal = 0, remap = false,
					target = "Lgoblinbob/mobends/core/client/gui/MoBendsScreenBuilder;withWipOverlay(Lgoblinbob/mobends/core/client/gui/view/View;)Lgoblinbob/mobends/core/client/gui/view/View;"))
	private View foreverfixes$packsWithoutOverlay(MoBendsScreenBuilder builder, View content, Operation<View> original)
	{
		return content;
	}
}
