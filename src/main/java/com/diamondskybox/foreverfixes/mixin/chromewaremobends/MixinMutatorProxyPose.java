package com.diamondskybox.foreverfixes.mixin.chromewaremobends;

import com.chromeware.client.sandevistan.AfterimagePlayerEntity;
import com.diamondskybox.foreverfixes.ProxyPose;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.mutators.Mutator;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Chromeware's Sandevistan renders the player again through a proxy entity (same entity id, a snapshot of the real player)
 * for the live copy in its post effect and for the afterimage captures. Mo' Bends animated that proxy as a player of its own,
 * from scratch, so the copy walked (and never swung) while the real player attacked. The proxy is given the real player's
 * pose of this frame instead of being animated: every part, offsets and whole-model rotations are copied from the real
 * player's data, which already holds the pack, the Player Animation Lib copy and every Forever Fixes adjustment.
 */
@Mixin(value = Mutator.class, remap = false)
public abstract class MixinMutatorProxyPose
{
	@Inject(method = "performAnimations", remap = false, require = 1, at = @At("HEAD"), cancellable = true)
	private void foreverfixes$proxyTakesRealPose(LivingEntityData<?> data, String animatedEntityKey, LivingEntityRenderer<?, ?> renderer, float partialTicks, CallbackInfo ci)
	{
		if (!(data instanceof BipedEntityData<?> proxyData) || !(proxyData.getEntity() instanceof AfterimagePlayerEntity proxy))
		{
			return;
		}
		if (!(proxy.level().getEntity(proxy.getId()) instanceof LivingEntity real) || real == proxy)
		{
			return;
		}
		final Object realData = EntityDatabase.instance.get(real);
		if (realData instanceof BipedEntityData<?> source && source != proxyData)
		{
			ProxyPose.copy(source, proxyData);
			ci.cancel();
		}
	}
}
