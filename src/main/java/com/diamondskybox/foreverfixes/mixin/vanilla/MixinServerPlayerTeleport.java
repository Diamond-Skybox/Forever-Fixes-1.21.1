package com.diamondskybox.foreverfixes.mixin.vanilla;

import com.diamondskybox.foreverfixes.CrossDimensionGuard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.portal.DimensionTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/** The three ways a player is moved into another level; each asks CrossDimensionGuard first. */
@Mixin(value = ServerPlayer.class, remap = false)
public abstract class MixinServerPlayerTeleport
{
	@Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
	private void foreverfixes$guardTeleport(ServerLevel level, double x, double y, double z, Set<RelativeMovement> relative, float yaw, float pitch,
			CallbackInfoReturnable<Boolean> cir)
	{
		if (CrossDimensionGuard.refuse((ServerPlayer) (Object) this, level))
		{
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
	private void foreverfixes$guardTeleportSimple(ServerLevel level, double x, double y, double z, float yaw, float pitch, CallbackInfo ci)
	{
		if (CrossDimensionGuard.refuse((ServerPlayer) (Object) this, level))
		{
			ci.cancel();
		}
	}

	@Inject(method = "changeDimension", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
	private void foreverfixes$guardChangeDimension(DimensionTransition transition, CallbackInfoReturnable<Entity> cir)
	{
		if (transition != null && CrossDimensionGuard.refuse((ServerPlayer) (Object) this, transition.newLevel()))
		{
			cir.setReturnValue(null);
		}
	}
}
