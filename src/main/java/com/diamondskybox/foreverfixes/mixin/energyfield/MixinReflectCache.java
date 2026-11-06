package com.diamondskybox.foreverfixes.mixin.energyfield;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Energy Field's reflection helper R.call looks methods up with Class.getDeclaredMethods()/getMethods() on every call, and it runs from
 * first-person hand rendering every frame; each lookup copies the class's whole method table (3-4% of the render thread in spark).
 * The arrays are only read there, so one copy per class is cached.
 */
@Mixin(targets = "com.energyfield.mod.megafix.R", remap = false)
public abstract class MixinReflectCache
{
	@Unique
	private static final Map<Class<?>, Method[]> foreverfixes$declared = new ConcurrentHashMap<>();
	@Unique
	private static final Map<Class<?>, Method[]> foreverfixes$public = new ConcurrentHashMap<>();

	@WrapOperation(method = "call", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Ljava/lang/Class;getDeclaredMethods()[Ljava/lang/reflect/Method;"))
	private static Method[] foreverfixes$declaredMethods(Class<?> type, Operation<Method[]> original)
	{
		return foreverfixes$declared.computeIfAbsent(type, t -> original.call(t));
	}

	@WrapOperation(method = "call", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Ljava/lang/Class;getMethods()[Ljava/lang/reflect/Method;"))
	private static Method[] foreverfixes$publicMethods(Class<?> type, Operation<Method[]> original)
	{
		return foreverfixes$public.computeIfAbsent(type, t -> original.call(t));
	}
}
