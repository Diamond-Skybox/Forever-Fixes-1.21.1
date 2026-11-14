package com.diamondskybox.foreverfixes.mixin.vanilla;

import com.diamondskybox.foreverfixes.ForeverFixes;
import com.diamondskybox.foreverfixes.ForeverFixesCommonConfig;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Diagnostic (debug.mobPersistenceTrace, off by default): why hostile mobs still turn permanent with spawning.noPersistenceFromJunk.
 * Logs a hostile mob's persistence after a pickup, and a stack trace whenever something calls setPersistenceRequired on one
 * (rate limited). Remove once the cause is found.
 */
@Mixin(Mob.class)
public abstract class MixinMobPersistenceTrace
{
	@Shadow private boolean persistenceRequired;
	private static int foreverfixes$traces;

	@Inject(method = "pickUpItem(Lnet/minecraft/world/entity/item/ItemEntity;)V", require = 1, at = @At("RETURN"))
	private void foreverfixes$tracePickup(ItemEntity item, CallbackInfo ci)
	{
		if ((Object) this instanceof Enemy && ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.DEBUG_PERSISTENCE_TRACE) && foreverfixes$traces < 200)
		{
			foreverfixes$traces++;
			ForeverFixes.LOGGER.info("[persistence-trace] {} picked up {} -> persistent={}", ((Mob) (Object) this).getType(), item.getItem(), this.persistenceRequired);
		}
	}

	@Inject(method = "setPersistenceRequired()V", require = 1, at = @At("HEAD"))
	private void foreverfixes$traceSet(CallbackInfo ci)
	{
		if ((Object) this instanceof Enemy && ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.DEBUG_PERSISTENCE_TRACE) && foreverfixes$traces < 200)
		{
			foreverfixes$traces++;
			ForeverFixes.LOGGER.info("[persistence-trace] setPersistenceRequired on {}", ((Mob) (Object) this).getType(), new Throwable("who"));
		}
	}
}
