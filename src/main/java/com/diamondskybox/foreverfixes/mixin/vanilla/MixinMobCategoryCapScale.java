package com.diamondskybox.foreverfixes.mixin.vanilla;

import com.diamondskybox.foreverfixes.SpawnCaps;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Natural-spawn mob caps scaled per category (spawning.mobCapScale), e.g. half as many monsters around each player. */
@Mixin(MobCategory.class)
public abstract class MixinMobCategoryCapScale
{
	@ModifyReturnValue(method = "getMaxInstancesPerChunk", at = @At("RETURN"))
	private int foreverfixes$scaleCap(int cap)
	{
		return SpawnCaps.scale((MobCategory) (Object) this, cap);
	}
}
