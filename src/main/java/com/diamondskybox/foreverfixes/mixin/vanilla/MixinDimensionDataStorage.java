package com.diamondskybox.foreverfixes.mixin.vanilla;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A level's saved-data cache is a plain HashMap that the server thread walks on every autosave, but mods look entries up from
 * world-generation threads: Immersive Petroleum's reservoir feature adds one "ImmersivePetroleum-ReservoirRegions/x_z" entry per new
 * region from C2ME's chunk workers, so during a pregen the autosave crashed with a ConcurrentModificationException in
 * DimensionDataStorage.save (and the server then hung). The cache becomes a synchronized map, and save walks a copy taken under its
 * lock. The lock is not held while the entries save themselves, so a SavedData that locks its own state while another thread inside
 * it waits for the cache cannot deadlock against the autosave.
 */
@Mixin(value = DimensionDataStorage.class, remap = false)
public abstract class MixinDimensionDataStorage
{
	@Shadow(remap = false)
	@Final
	@Mutable
	private Map<String, SavedData> cache;

	@Inject(method = "<init>", at = @At("TAIL"), remap = false, require = 1)
	private void foreverfixes$synchronizedCache(CallbackInfo ci)
	{
		this.cache = Collections.synchronizedMap(this.cache);
	}

	@WrapOperation(method = "save", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"))
	private void foreverfixes$saveSnapshot(Map<String, SavedData> map, BiConsumer<? super String, ? super SavedData> action,
			Operation<Void> original)
	{
		final List<Map.Entry<String, SavedData>> snapshot;
		synchronized (map)
		{
			snapshot = new ArrayList<>(map.size());
			for (Map.Entry<String, SavedData> entry : map.entrySet())
			{
				snapshot.add(new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), entry.getValue()));
			}
		}
		for (Map.Entry<String, SavedData> entry : snapshot)
		{
			action.accept(entry.getKey(), entry.getValue());
		}
	}
}
