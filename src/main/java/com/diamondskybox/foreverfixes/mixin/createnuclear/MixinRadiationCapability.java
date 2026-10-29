package com.diamondskybox.foreverfixes.mixin.createnuclear;

import com.diamondskybox.foreverfixes.ForeverFixesCommonConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Create: Nuclear's radiation tick removes its radiation effect from every living entity on every tick, whether or not the entity
 * has it, and each removal fires NeoForge's effect-removed event (spark on the server: ~0.6% of the server thread). Removing an
 * effect the entity does not have returns false either way, so the call is skipped then.
 */
@Mixin(targets = "net.nuclearteam.createnuclear.content.radiation.capability.RadiationCapability", remap = false)
public abstract class MixinRadiationCapability
{
	@WrapOperation(method = "applyEffects", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/world/entity/LivingEntity;removeEffect(Lnet/minecraft/core/Holder;)Z"))
	private static boolean foreverfixes$removeOnlyIfPresent(LivingEntity entity, Holder<MobEffect> effect, Operation<Boolean> original)
	{
		if (!entity.hasEffect(effect) && ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.NUCLEAR_SKIP_ABSENT_EFFECT_REMOVAL))
		{
			return false;
		}
		return original.call(entity, effect);
	}
}
