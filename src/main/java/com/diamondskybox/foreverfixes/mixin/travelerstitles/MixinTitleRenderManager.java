package com.diamondskybox.foreverfixes.mixin.travelerstitles;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Traveler's Titles only looks for a new dimension or biome title while {@code level.isLoaded(player.blockPosition())}, and
 * vanilla reports every position above the build limit as not loaded. Create: Cosmonautics drops players into a planet
 * dimension at Y 19000 (far above Twilight Forest's ceiling at 256), so the dimension title never showed on arrival and only
 * appeared, if at all, once the player sank below the build limit. Both position checks in playerTick (the loaded test and the
 * can-see-sky test for sky-lit dimensions) now use the player's position clamped into the build height. Inside the build
 * height nothing changes. Config: travelerstitles.titlesAboveBuildHeight.
 */
@Mixin(targets = "com.yungnickyoung.minecraft.travelerstitles.render.TitleRenderManager", remap = false)
public abstract class MixinTitleRenderManager
{
	@Redirect(method = "playerTick", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/world/level/Level;isLoaded(Lnet/minecraft/core/BlockPos;)Z"))
	private boolean foreverfixes$isLoaded(Level level, BlockPos pos)
	{
		return level.isLoaded(foreverfixes$clamp(level, pos));
	}

	@Redirect(method = "playerTick", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/world/level/Level;canSeeSky(Lnet/minecraft/core/BlockPos;)Z"))
	private boolean foreverfixes$canSeeSky(Level level, BlockPos pos)
	{
		return level.canSeeSky(foreverfixes$clamp(level, pos));
	}

	private static BlockPos foreverfixes$clamp(Level level, BlockPos pos)
	{
		if (!ForeverFixesConfig.enabled(ForeverFixesConfig.TITLES_ABOVE_BUILD_HEIGHT))
		{
			return pos;
		}
		final int y = Mth.clamp(pos.getY(), level.getMinBuildHeight(), level.getMaxBuildHeight() - 1);
		return y == pos.getY() ? pos : new BlockPos(pos.getX(), y, pos.getZ());
	}
}
