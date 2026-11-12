package com.diamondskybox.foreverfixes.mixin.vanilla;

import com.diamondskybox.foreverfixes.SnowBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Listed biomes precipitate snow wherever they would rain (see {@link SnowBiomes}). */
@Mixin(value = Biome.class, remap = false)
public abstract class MixinBiomePrecipitationSnow
{
	@Shadow(remap = false)
	public abstract boolean hasPrecipitation();

	@Inject(method = "getPrecipitationAt", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private void foreverfixes$snowInsteadOfRain(BlockPos pos, CallbackInfoReturnable<Biome.Precipitation> cir)
	{
		if (this.hasPrecipitation() && SnowBiomes.snows((Biome) (Object) this))
		{
			cir.setReturnValue(Biome.Precipitation.SNOW);
		}
	}
}
