package com.diamondskybox.foreverfixes.mixin.vampirism;

import com.diamondskybox.foreverfixes.VampireUndead;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Vampire players pass "#minecraft:undead" / "#minecraft:sensitive_to_smite" entity type checks (Smite). */
@Mixin(EntityPredicate.class)
public abstract class MixinEntityPredicateVampireUndead
{
	@WrapOperation(method = "matches(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/Entity;)Z",
			require = 1, at = @At(value = "INVOKE",
					target = "Lnet/minecraft/advancements/critereon/EntityTypePredicate;matches(Lnet/minecraft/world/entity/EntityType;)Z"))
	private boolean foreverfixes$vampireUndead(EntityTypePredicate predicate, EntityType<?> type, Operation<Boolean> original,
			@Local(argsOnly = true) Entity entity)
	{
		return original.call(predicate, type) || VampireUndead.matchesAsUndead(predicate, entity);
	}
}
