package com.diamondskybox.foreverfixes.mixin.network;

import com.diamondskybox.foreverfixes.NetworkTimeouts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** The outgoing connection's read timeout (vanilla 30 s, the client watching the server) follows {@code network.timeoutSeconds}. */
@Mixin(targets = "net.minecraft.network.Connection$1")
public abstract class MixinClientReadTimeout
{
	@ModifyArg(method = "initChannel", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lio/netty/handler/timeout/ReadTimeoutHandler;<init>(I)V"))
	private int foreverfixes$readTimeout(int seconds)
	{
		return NetworkTimeouts.readTimeoutSeconds();
	}
}
