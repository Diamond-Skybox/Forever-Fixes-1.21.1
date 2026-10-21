package com.diamondskybox.foreverfixes.mixin.waveycapes;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import com.diamondskybox.foreverfixes.MoBendsBridge;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Wavey Capes tilts and lowers its cape while the player crouches. Mo' Bends' crouching torso already leans, and on 1.21.1 the cape
 * hangs from that torso (Wavey Capes anchors to {@code PlayerModel.body}, which Mo' Bends poses), so for players Mo' Bends animates
 * the cape tilted twice. Wavey Capes asks its player delegate whether the player crouches; this answers "no" for those players
 * (Wavey Capes, tr7zw Protective License, no code copied; ported from the 1.12.2 Forever Fixes patch).
 */
@Mixin(targets = "dev.tr7zw.waveycapes.delegate.PlayerDelegate", remap = false)
public abstract class MixinPlayerDelegate
{
	@Redirect(method = "isCrouching", remap = false, require = 1,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isCrouching()Z"))
	private boolean foreverfixes$crouching(AbstractClientPlayer player)
	{
		return player.isCrouching()
				&& !(ForeverFixesConfig.enabled(ForeverFixesConfig.WAVEY_NO_CROUCH_TILT) && MoBendsBridge.animates(player));
	}
}
