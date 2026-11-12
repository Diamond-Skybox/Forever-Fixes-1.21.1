package com.diamondskybox.foreverfixes.mixin.dhfog;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Distant Horizons: the LOD fog colour gets the sunrise and sunset tint. For "use world fog colour" Distant Horizons recomputes the
 * fog colour with FogRenderer.setupColor and a render distance of 1 chunk, and vanilla only blends the sunrise colour into the fog (when
 * looking toward the sun) at 4 chunks and more, so the LODs fogged to a plain grey-blue under a red dawn sky while the chunks in front
 * took the sunrise colour; the recomputation also fires the fog colour event again, so Hoarfrost's LOD fog colour lost it too. The
 * real render distance is passed, so the LODs get exactly the colour the chunks get.
 */
@Mixin(targets = "com.seibel.distanthorizons.common.wrappers.minecraft.MinecraftRenderWrapper_neoforge", remap = false)
public abstract class MixinDhFogColorRenderDistance
{
	@ModifyArg(method = "getFogColor(F)Ljava/awt/Color;", remap = false, require = 1, index = 3,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/client/renderer/FogRenderer;setupColor(Lnet/minecraft/client/Camera;FLnet/minecraft/client/multiplayer/ClientLevel;IF)V"))
	private int foreverfixes$realRenderDistance(int renderDistance)
	{
		return Math.max(renderDistance, Minecraft.getInstance().options.getEffectiveRenderDistance());
	}
}
