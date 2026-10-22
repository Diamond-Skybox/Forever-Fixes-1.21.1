package com.diamondskybox.foreverfixes.mixin.ragdollcape;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sable: Ragdolls draws its own stiff cape on the ragdoll's torso and also runs the player renderer's layers, which with Wavey
 * Capes include the Wavey cape; the ragdoll showed both. With Wavey Capes installed the ragdoll's own cape is skipped.
 */
@Mixin(targets = "dev.leo.sableplayerragdoll.neoforge.client.RagdollPartBlockEntityRenderer", remap = false)
public abstract class MixinRagdollPartRendererCape
{
	@Inject(method = "renderCape", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private void foreverfixes$leaveCapeToWaveyCapes(RagdollPartBlockEntity part, LivingEntity entity, PoseStack pose,
			MultiBufferSource buffers, int light, CallbackInfo ci)
	{
		if (ForeverFixesConfig.enabled(ForeverFixesConfig.RAGDOLL_WAVEY_CAPE))
		{
			ci.cancel();
		}
	}
}
