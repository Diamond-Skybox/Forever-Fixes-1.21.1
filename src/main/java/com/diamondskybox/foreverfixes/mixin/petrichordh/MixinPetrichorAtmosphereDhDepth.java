package com.diamondskybox.foreverfixes.mixin.petrichordh;

import com.diamondskybox.foreverfixes.PetrichorDhDepth;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Petrichor: its rain haze's depth copy gets Distant Horizons' LOD terrain (see {@link PetrichorDhDepth}). */
@Mixin(targets = "com.pockyl.petrichor.client.render.Atmosphere", remap = false)
public abstract class MixinPetrichorAtmosphereDhDepth
{
	@Shadow(remap = false)
	private static TextureTarget depth;

	@Inject(method = "captureDepth", remap = false, require = 1, at = @At("TAIL"))
	private static void foreverfixes$lodsAreTerrain(Minecraft minecraft, CallbackInfo ci)
	{
		PetrichorDhDepth.merge(depth);
	}
}
