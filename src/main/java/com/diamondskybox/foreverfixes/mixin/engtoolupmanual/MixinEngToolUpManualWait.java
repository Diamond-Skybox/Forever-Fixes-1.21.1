package com.diamondskybox.foreverfixes.mixin.engtoolupmanual;

import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Engineer's Tool Upgrades: its manual indexing waits for Immersive Engineering's multiblock structures. It builds the IE manual
 * (ManualInstance.getGui) on the first client tick in a world, but the server only sends the structures (MessageMultiblockSync) when
 * the player has logged in, so every multiblock page failed ("35 entries failed to load!" in chat on each join). The retry loop is
 * left alone: skipping a tick here doesn't count as an attempt, and once the structures are in its next attempt indexes the manual.
 * TemplateMultiblock.SYNCED_CLIENT_TEMPLATES is filled by that packet and never cleared, so rejoining doesn't wait again.
 */
@Mixin(targets = "cat.joni.engtoolup.client.ManualLinkHandler", remap = false)
public abstract class MixinEngToolUpManualWait
{
	@Inject(method = "ensureManualIndexed()V", remap = false, require = 1, at = @At("HEAD"), cancellable = true)
	private static void foreverfixes$waitForTemplates(CallbackInfo ci)
	{
		if (ForeverFixesConfig.enabled(ForeverFixesConfig.IE_MANUAL_WAIT_FOR_TEMPLATES) && TemplateMultiblock.SYNCED_CLIENT_TEMPLATES.isEmpty())
		{
			ci.cancel();
		}
	}
}
