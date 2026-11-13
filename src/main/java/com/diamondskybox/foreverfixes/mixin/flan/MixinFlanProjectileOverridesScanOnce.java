package com.diamondskybox.foreverfixes.mixin.flan;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.flemmli97.flan.api.permission.InteractionOverrideManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Flan: the projectile permission defaults are worked out once per data load, not on every projectile impact. Its lookup builds a
 * dummy of every registered entity type (every mob in the pack) to sort them into projectile permissions, whenever its table reads
 * as resolved; reading the table resolves it again, so the next impact repeated the whole scan. A bouncing grenade calls it every
 * few ticks and the server stalled for seconds. The scan now runs once per table (a reload makes a new one); its results stay in
 * Flan's table as before.
 */
@Mixin(value = InteractionOverrideManager.class, remap = false)
public abstract class MixinFlanProjectileOverridesScanOnce
{
	@Unique
	private static final Set<Object> foreverfixes$scanned = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

	@WrapOperation(method = "getProjectileEntityInteract", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lio/github/flemmli97/flan/api/permission/InteractionOverrideManager$InteractionHolder;unresolved()Z"))
	private boolean foreverfixes$scanOnce(InteractionOverrideManager.InteractionHolder<?> holder, Operation<Boolean> original)
	{
		final boolean unresolved = original.call(holder);
		if (unresolved)
		{
			return true;
		}
		// the first time the table is resolved, let the scan run; afterwards report it unresolved so the scan is skipped
		return !foreverfixes$scanned.add(holder);
	}
}
