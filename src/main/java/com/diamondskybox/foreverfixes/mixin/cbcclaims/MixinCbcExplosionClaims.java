package com.diamondskybox.foreverfixes.mixin.cbcclaims;

import com.diamondskybox.foreverfixes.ClaimedLand;
import com.diamondskybox.foreverfixes.mixin.vanilla.AccessorExplosionLevel;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Create Big Cannons: a shell explosion's blast skips the blocks in claimed land (see {@link ClaimedLand}). Covers every CBC shell,
 * and the CBC: Advanced Technologies rockets, which blow up through the same explosions.
 */
@Mixin(targets = "rbasamoyai.createbigcannons.remix.CustomExplosion$Impl", remap = false)
public abstract class MixinCbcExplosionClaims
{
	@Inject(method = "finalizeExplosion(Z)V", remap = false, require = 1, at = @At("HEAD"))
	private void foreverfixes$claims(boolean spawnParticles, CallbackInfo ci)
	{
		if (!ClaimedLand.enabled())
		{
			return;
		}
		final Explosion self = (Explosion) (Object) this;
		final Level level = ((AccessorExplosionLevel) self).foreverfixes$level();
		self.getToBlow().removeIf(pos -> ClaimedLand.protects(level, pos));
	}
}
