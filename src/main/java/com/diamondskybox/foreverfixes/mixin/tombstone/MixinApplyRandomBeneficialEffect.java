package com.diamondskybox.foreverfixes.mixin.tombstone;

import com.diamondskybox.foreverfixes.BlessingPool;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.function.Predicate;

/** Blessing (the only user of this effect type) draws its effect from holy.blessingEffects. */
@Mixin(targets = "ovh.corail.tombstone.enchantment.ApplyRandomBeneficialEffect", remap = false)
public abstract class MixinApplyRandomBeneficialEffect
{
	@WrapOperation(method = "apply", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lovh/corail/tombstone/helper/RegistryHelper;findRandomHolder(Lnet/minecraft/core/Registry;Ljava/util/function/Predicate;)Ljava/util/Optional;"))
	private Optional<Holder<Object>> foreverfixes$holyPool(Registry<Object> registry, Predicate<Object> allowed, Operation<Optional<Holder<Object>>> original)
	{
		final Optional<Holder<Object>> pick = BlessingPool.pick(registry);
		return pick != null ? pick : original.call(registry, allowed);
	}
}
