package com.diamondskybox.foreverfixes.mixin.cbcclaims;

import com.diamondskybox.foreverfixes.CannonClaims;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Create Big Cannons: lava fluid shells don't start fires or light blocks inside claims. The burst doesn't know who fired it, so it
 * asks Flan with no player (claims refuse unless they allow placing to everyone); dousing fires is left alone.
 */
@Mixin(targets = "rbasamoyai.createbigcannons.munitions.big_cannon.fluid_shell.DefaultFluidCompat", remap = false)
public abstract class MixinCbcFluidShellClaims
{
	@WrapOperation(method = "spawnFire", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
	private static boolean foreverfixes$fire(Level level, BlockPos pos, BlockState state, Operation<Boolean> original)
	{
		return !CannonClaims.placeRefused(level, pos, null) && original.call(level, pos, state);
	}

	@WrapOperation(method = "spawnFire", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
	private static boolean foreverfixes$light(Level level, BlockPos pos, BlockState state, int flags, Operation<Boolean> original)
	{
		return !CannonClaims.placeRefused(level, pos, null) && original.call(level, pos, state, flags);
	}
}
