package com.diamondskybox.foreverfixes.mixin.cbcclaims;

import com.diamondskybox.foreverfixes.ClaimedLand;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

/** Create Big Cannons: a big cannon round doesn't break through blocks in claimed land; it is spent there instead (see {@link ClaimedLand}). */
@Mixin(targets = "rbasamoyai.createbigcannons.munitions.big_cannon.AbstractBigCannonProjectile", remap = false)
public abstract class MixinCbcProjectileClaims
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
