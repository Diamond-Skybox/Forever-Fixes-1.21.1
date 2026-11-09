package com.diamondskybox.foreverfixes.mixin.network;

import com.diamondskybox.foreverfixes.NetworkTimeouts;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** How long the server waits for a keep-alive answer before "Timed out" (vanilla 15 s) follows {@code network.timeoutSeconds}. */
@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class MixinKeepAliveLimit
{
	@ModifyConstant(method = {"keepConnectionAlive", "checkIfClosed"}, constant = @Constant(longValue = 15000L), require = 2)
	private long foreverfixes$keepAliveLimit(long vanilla)
	{
		return NetworkTimeouts.keepAliveLimitMillis();
	}
}
