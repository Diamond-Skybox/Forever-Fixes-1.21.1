package com.diamondskybox.foreverfixes.mixin.bscglass;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The clouds' own depth copy (used where no glass snapshot exists, outdoors) had the same gap as the glass snapshot: batched
 * entities, chests and beacon beams were not drawn yet, so storm fog covered them even with no rain in front of them. The
 * batched geometry is drawn before the copy (see MixinCloudGlassCapture).
 */
@Mixin(targets = "dev.nonamecrackers2.simpleclouds.client.renderer.SimpleCloudsRenderer", remap = false)
public abstract class MixinSimpleCloudsDepthCopy
{
	@Inject(method = "copyDepthFromMainToClouds", remap = false, require = 1, at = @At("HEAD"))
	private void foreverfixes$flushBatched(CallbackInfo ci)
	{
		if (ForeverFixesConfig.enabled(ForeverFixesConfig.BSC_GLASS_FLUSH))
		{
			Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
		}
	}
}
