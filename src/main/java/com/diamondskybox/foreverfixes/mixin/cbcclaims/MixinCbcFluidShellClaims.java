package com.diamondskybox.foreverfixes.mixin.cbcclaims;

import com.diamondskybox.foreverfixes.ClaimedLand;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Create Big Cannons: lava fluid shells don't start fires or light blocks in claimed land (see {@link ClaimedLand}); dousing fires is left alone. */
@Mixin(targets = "rbasamoyai.createbigcannons.munitions.big_cannon.fluid_shell.DefaultFluidCompat", remap = false)
public abstract class MixinCbcFluidShellClaims
{
	@WrapOperation(method = "spawnFire", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
	private static boolean foreverfixes$fire(Level level, BlockPos pos, BlockState state, Operation<Boolean> original)
	{
		return !ClaimedLand.protects(level, pos) && original.call(level, pos, state);
	}

	@WrapOperation(method = "spawnFire", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
	private static boolean foreverfixes$light(Level level, BlockPos pos, BlockState state, int flags, Operation<Boolean> original)
	{
		return !ClaimedLand.protects(level, pos) && original.call(level, pos, state, flags);
	}
}
