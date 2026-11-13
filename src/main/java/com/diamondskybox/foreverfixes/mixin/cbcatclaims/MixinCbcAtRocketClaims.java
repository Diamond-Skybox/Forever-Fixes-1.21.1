package com.diamondskybox.foreverfixes.mixin.cbcatclaims;

import com.diamondskybox.foreverfixes.ClaimedLand;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

/** CBC: Advanced Technologies: rocket pod rockets don't punch through blocks in claimed land; the rocket is spent there instead (see {@link ClaimedLand}). */
@Mixin(targets = { "com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocket", "com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocket" },
		remap = false)
public abstract class MixinCbcAtRocketClaims
{
	@WrapOperation(method = "calculateBlockPenetration", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
	private boolean foreverfixes$claims(Level level, BlockPos pos, BlockState state, int flags, Operation<Boolean> original)
	{
		if (ClaimedLand.protects(level, pos))
		{
			((AbstractCannonProjectile) (Object) this).setProjectileMass(0F);
			return false;
		}
		return original.call(level, pos, state, flags);
	}
}
