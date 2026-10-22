package com.diamondskybox.foreverfixes.mixin.traveloptics;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * T.O Magic 'n Extras 4.4.0.1 (1.21.1 alpha) registers its KeyLootModifier codec object twice, as {@code traveloptics:key_loot} and
 * {@code traveloptics:universal_loot}; registries reject a value registered under two names, so the game stopped at mod loading
 * ("Adding duplicate value"). {@code universal_loot} now gets its own codec object that reads and writes the same data.
 */
@Mixin(targets = "com.gametechbc.traveloptics.loot.TOLootModifiers", remap = false)
public abstract class MixinTOLootModifiers
{
	@WrapOperation(method = "<clinit>", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/neoforged/neoforge/registries/DeferredRegister;register(Ljava/lang/String;Ljava/util/function/Supplier;)Lnet/neoforged/neoforge/registries/DeferredHolder;"))
	private static DeferredHolder<?, ?> foreverfixes$ownUniversalCodec(DeferredRegister<Object> register, String name, Supplier<Object> value,
			Operation<DeferredHolder<?, ?>> original)
	{
		if ("universal_loot".equals(name))
		{
			final Supplier<Object> shared = value;
			return original.call(register, name, (Supplier<Object>) () -> ((MapCodec<?>) shared.get()).xmap(Function.identity(), Function.identity()));
		}
		return original.call(register, name, value);
	}
}
