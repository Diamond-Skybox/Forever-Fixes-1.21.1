package com.diamondskybox.foreverfixes.mixin.ragdollmobs;

import com.diamondskybox.foreverfixes.ForeverFixesCommonConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Sable: Ragdolls 0.8: no mob ragdolls. It ragdolls every mob a player kills (and mobs hit with its ragdoll items): the whitelist
 * bundled in its jar allows every entity type, and there is no setting for it. Its whitelist answers no for every mob type while
 * ragdoll.mobRagdolls is off; player ragdolls and Ragdoll Corpse's player corpses are a separate path and stay.
 */
@Mixin(targets = "dev.leo.sableplayerragdoll.mob.MobRagdollWhitelist", remap = false)
public abstract class MixinMobRagdollWhitelistOff
{
	@Inject(method = "isAllowed(Lnet/minecraft/world/entity/EntityType;)Z", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private static void foreverfixes$noMobRagdolls(EntityType<?> type, CallbackInfoReturnable<Boolean> cir)
	{
		if (!ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.RAGDOLL_MOB_RAGDOLLS_OFF))
		{
			return;
		}
		cir.setReturnValue(false);
	}

	@Inject(method = "isAllowed(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/EntityType;)Z", remap = false, require = 1,
			cancellable = true, at = @At("HEAD"))
	private static void foreverfixes$noMobRagdollsInLevel(ServerLevel level, EntityType<?> type, CallbackInfoReturnable<Boolean> cir)
	{
		if (!ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.RAGDOLL_MOB_RAGDOLLS_OFF))
		{
			return;
		}
		cir.setReturnValue(false);
	}
}
