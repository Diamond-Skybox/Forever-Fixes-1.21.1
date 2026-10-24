package com.diamondskybox.foreverfixes.mixin.waystonesuiplus;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.network.message.RequestEditWaystoneMessage;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Waystones UI Plus 1.6.0's edit button builds Waystones' edit request from the waystone's position, a constructor Waystones
 * 21.1.46 no longer has (it takes the waystone's id now): the client crashed with a NoSuchMethodError. The request is built from
 * the id of the same waystone instead.
 */
@Mixin(targets = "com.maria.waystonesuiplus.client.screen.WaystonesPlusScreen", remap = false)
public abstract class MixinWaystonesPlusScreen
{
	@WrapOperation(method = "editCurrentWaystone", remap = false, require = 1,
			at = @At(value = "NEW", remap = false, target = "(Lnet/minecraft/core/BlockPos;)Lnet/blay09/mods/waystones/network/message/RequestEditWaystoneMessage;"))
	private RequestEditWaystoneMessage foreverfixes$editById(BlockPos pos, Operation<RequestEditWaystoneMessage> original, @Local Waystone current)
	{
		return new RequestEditWaystoneMessage(current.getWaystoneUid());
	}
}
