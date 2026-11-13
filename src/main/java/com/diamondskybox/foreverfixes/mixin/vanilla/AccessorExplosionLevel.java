package com.diamondskybox.foreverfixes.mixin.vanilla;

import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** An explosion's level (see {@code CannonClaims}). */
@Mixin(value = Explosion.class, remap = false)
public interface AccessorExplosionLevel
{
	@Accessor(value = "level", remap = false)
	Level foreverfixes$level();
}
