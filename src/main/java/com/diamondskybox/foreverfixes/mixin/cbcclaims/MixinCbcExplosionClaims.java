package com.diamondskybox.foreverfixes.mixin.cbcclaims;

import com.diamondskybox.foreverfixes.CannonClaims;
import com.diamondskybox.foreverfixes.mixin.vanilla.AccessorExplosionLevel;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Create Big Cannons: a shell explosion's blast skips the blocks in claims its owner can't break in (see {@link CannonClaims}). */
@Mixin(targets = "rbasamoyai.createbigcannons.remix.CustomExplosion$Impl", remap = false)
public abstract class MixinCbcExplosionClaims
{
	@Inject(method = "finalizeExplosion(Z)V", remap = false, require = 1, at = @At("HEAD"))
	private void foreverfixes$claims(boolean spawnParticles, CallbackInfo ci)
	{
		if (!CannonClaims.enabled())
		{
			return;
		}
		final Explosion self = (Explosion) (Object) this;
		final Level level = ((AccessorExplosionLevel) self).foreverfixes$level();
		self.getToBlow().removeIf(pos -> CannonClaims.breakRefused(level, pos, self.getIndirectSourceEntity()));
	}
}
