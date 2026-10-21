package com.diamondskybox.foreverfixes.mixin.vanilla;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Vanilla clips everything farther than 4 x the render distance (GameRenderer.getDepthFar: 512 blocks at 8 chunks), in every
 * direction, so in a 2000-block-tall world the tops of mountains vanish while their feet are in view. Terrain fog is a cylinder
 * (horizontal only), so the far plane is the only thing hiding them. This raises the far plane of the world projection to at
 * least render.farPlaneBlocks. Done on the perspective call rather than on getDepthFar so it also applies when Iris
 * replaces that call with its own distance (render distance + 1024) for shader packs.
 */
@Mixin(value = GameRenderer.class, remap = false)
public abstract class MixinGameRenderer
{
	@ModifyArg(method = "getProjectionMatrix", index = 3, remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lorg/joml/Matrix4f;perspective(FFFF)Lorg/joml/Matrix4f;"))
	private float foreverfixes$farPlane(float zFar)
	{
		if (!ForeverFixesConfig.enabled(ForeverFixesConfig.FAR_PLANE_RAISE))
		{
			return zFar;
		}
		return Math.max(zFar, (float) ForeverFixesConfig.value(ForeverFixesConfig.FAR_PLANE_BLOCKS, 4096D));
	}
}
