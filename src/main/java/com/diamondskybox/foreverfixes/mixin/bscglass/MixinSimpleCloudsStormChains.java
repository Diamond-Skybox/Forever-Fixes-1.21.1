package com.diamondskybox.foreverfixes.mixin.bscglass;

import com.diamondskybox.foreverfixes.StormFogFixes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Simple Clouds: the storm fog's blur chain, dropping Forever Fixes' storm fog shaders on a reload, and marking the storm fog
 * pass, during which Better Simple Clouds' depth is the one with the particles in it (see {@link StormFogFixes}).
 */
@Mixin(targets = "dev.nonamecrackers2.simpleclouds.client.renderer.SimpleCloudsRenderer", remap = false)
public abstract class MixinSimpleCloudsStormChains
{
	@ModifyArg(method = "onResourceManagerReload", remap = false, require = 1, index = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Ldev/nonamecrackers2/simpleclouds/client/renderer/SimpleCloudsRenderer;createPostChain(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/pipeline/RenderTarget;)Lnet/minecraft/client/renderer/PostChain;"))
	private ResourceLocation foreverfixes$vanillaStormBlur(ResourceLocation chain)
	{
		return StormFogFixes.stormBlurChain(chain);
	}

	@Inject(method = "onResourceManagerReload", remap = false, require = 1, at = @At("RETURN"))
	private void foreverfixes$reload(ResourceManager manager, CallbackInfo ci)
	{
		StormFogFixes.onResourceReload();
	}

	@Inject(method = "doStormPostProcessing", remap = false, require = 1, at = @At("HEAD"))
	private void foreverfixes$stormFogBegin(Matrix4f camMat, float partialTick, Matrix4f projMat, double camX, double camY, double camZ,
			float r, float g, float b, CallbackInfo ci)
	{
		StormFogFixes.setInStormFog(true);
	}

	@Inject(method = "doStormPostProcessing", remap = false, require = 1, at = @At("RETURN"))
	private void foreverfixes$stormFogEnd(Matrix4f camMat, float partialTick, Matrix4f projMat, double camX, double camY, double camZ,
			float r, float g, float b, CallbackInfo ci)
	{
		StormFogFixes.setInStormFog(false);
	}
}
