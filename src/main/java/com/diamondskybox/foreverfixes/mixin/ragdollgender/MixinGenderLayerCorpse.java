package com.diamondskybox.foreverfixes.mixin.ragdollgender;

import com.diamondskybox.foreverfixes.RagdollCorpseMca;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MCA: female gender physics takes the breast texture (skin and clothing) from the MCA look it resolves here; a corpse doll of an
 * MCA player resolves to the owner's look (see {@link RagdollCorpseMca}).
 */
@Mixin(targets = "com.mcabp.wildfire.client.render.GenderLayer", remap = false)
public abstract class MixinGenderLayerCorpse
{
	@Inject(method = "villagerAppearance", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private static void foreverfixes$corpseOwner(Entity entity, CallbackInfoReturnable<Object> cir)
	{
		final Object look = RagdollCorpseMca.ownerLook(entity);
		if (look != null)
		{
			cir.setReturnValue(look);
		}
	}
}
