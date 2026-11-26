package com.diamondskybox.foreverfixes.mixin.vanilla;

import com.diamondskybox.foreverfixes.VerticalTickRange;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Mobs far above or below every nearby player are not ticked (see VerticalTickRange). Passengers tick with their vehicle. */
@Mixin(value = ServerLevel.class, remap = false)
public abstract class MixinServerLevelVerticalTickRange
{
	@Inject(method = "tickNonPassenger(Lnet/minecraft/world/entity/Entity;)V", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private void foreverfixes$holdFarAboveOrBelow(Entity entity, CallbackInfo ci)
	{
		if (VerticalTickRange.held((ServerLevel) (Object) this, entity))
		{
			ci.cancel();
		}
	}
}
