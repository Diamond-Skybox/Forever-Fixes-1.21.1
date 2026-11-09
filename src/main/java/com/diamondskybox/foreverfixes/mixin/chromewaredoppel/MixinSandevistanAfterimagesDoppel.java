package com.diamondskybox.foreverfixes.mixin.chromewaredoppel;

import com.chromeware.client.sandevistan.SandevistanAfterimages;
import com.diamondskybox.foreverfixes.DoppelAfterimages;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Captures and draws the Dark Doppelganger's afterimage trail alongside the players' (see DoppelAfterimages). */
@Mixin(value = SandevistanAfterimages.class, remap = false)
public abstract class MixinSandevistanAfterimagesDoppel
{
	@Shadow(remap = false)
	@Final
	private static MultiBufferSource.BufferSource BUFFERS;

	@Inject(method = "prepare", remap = false, require = 1, at = @At("TAIL"))
	private static void foreverfixes$prepareDoppel(RenderLevelStageEvent event, CallbackInfo ci)
	{
		DoppelAfterimages.prepare(event);
	}

	@ModifyReturnValue(method = "renderEchoes", remap = false, require = 1, at = @At("RETURN"))
	private static boolean foreverfixes$renderDoppel(boolean drawn, @Local(argsOnly = true) RenderLevelStageEvent event)
	{
		return DoppelAfterimages.render(event, BUFFERS) || drawn;
	}

	/** Keeps Chromeware's afterimage pass running while the boss's trail fades out after its field ends. */
	@ModifyReturnValue(method = "hasTrails", remap = false, require = 1, at = @At("RETURN"))
	private static boolean foreverfixes$doppelTrails(boolean trails)
	{
		return trails || DoppelAfterimages.hasTrails();
	}
}
