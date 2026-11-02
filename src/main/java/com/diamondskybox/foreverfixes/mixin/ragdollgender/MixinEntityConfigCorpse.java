package com.diamondskybox.foreverfixes.mixin.ragdollgender;

import com.diamondskybox.foreverfixes.RagdollCorpseMca;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MCA: female gender physics sizes the breasts from the MCA look it resolves here; a corpse doll of an MCA player resolves to the
 * owner's look (see {@link RagdollCorpseMca}).
 */
@Mixin(targets = "com.mcabp.wildfire.main.entitydata.EntityConfig", remap = false)
public abstract class MixinEntityConfigCorpse
{
	@Inject(method = "resolveVillagerLike", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private static void foreverfixes$corpseOwner(LivingEntity entity, CallbackInfoReturnable<Object> cir)
	{
		final Object look = RagdollCorpseMca.ownerLook(entity);
		if (look != null)
		{
			cir.setReturnValue(look);
		}
	}
}
