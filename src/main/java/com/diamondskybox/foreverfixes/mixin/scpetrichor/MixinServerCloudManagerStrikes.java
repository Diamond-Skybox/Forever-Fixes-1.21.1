package com.diamondskybox.foreverfixes.mixin.scpetrichor;

import com.diamondskybox.foreverfixes.CloudWeather;
import com.diamondskybox.foreverfixes.mixin.simpleclouds.AccessorCloudManagerLevel;
import dev.nonamecrackers2.simpleclouds.common.cloud.CloudType;
import dev.nonamecrackers2.simpleclouds.common.world.ServerCloudManager;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Simple Clouds + Petrichor: a share of Simple Clouds' lightning decisions become real Petrichor strikes near a player standing
 * in a thunder cloud's core, in place of the distant visual bolt (see {@link CloudWeather#petrichorStrike}).
 */
@Mixin(value = ServerCloudManager.class, remap = false)
public abstract class MixinServerCloudManagerStrikes
{
	@Inject(method = "spawnLightning(Ldev/nonamecrackers2/simpleclouds/common/cloud/CloudType;FIIZ)V", remap = false, require = 1,
			cancellable = true, at = @At("HEAD"))
	private void foreverfixes$petrichorStrike(CloudType type, float fade, int x, int z, boolean soundOnly, CallbackInfo ci)
	{
		if (((AccessorCloudManagerLevel) this).foreverfixes$level() instanceof ServerLevel level && CloudWeather.petrichorStrike(level))
		{
			ci.cancel();
		}
	}
}
