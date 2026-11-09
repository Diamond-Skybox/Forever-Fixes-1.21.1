package com.diamondskybox.foreverfixes.mixin.network;

import com.diamondskybox.foreverfixes.NetworkTimeouts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** The server's per-connection read timeout (vanilla 30 s) follows {@code network.timeoutSeconds}; see {@link NetworkTimeouts}. */
@Mixin(targets = "net.minecraft.server.network.ServerConnectionListener$1")
public abstract class MixinServerReadTimeout
{
	@ModifyArg(method = "initChannel", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lio/netty/handler/timeout/ReadTimeoutHandler;<init>(I)V"))
	private int foreverfixes$readTimeout(int seconds)
	{
		return NetworkTimeouts.readTimeoutSeconds();
	}
}
