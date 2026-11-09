package com.diamondskybox.foreverfixes.mixin.chromewaredoppel;

import com.diamondskybox.foreverfixes.DoppelChromeLayer;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import net.bandit.darkdoppelganger.entity.renderer.DarkDoppelgangerRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the layer that draws the summoner's copied cyber limbs on the Dark Doppelganger (last, so it runs after Iron's layers). */
@Mixin(value = DarkDoppelgangerRenderer.class, remap = false)
public abstract class MixinDoppelRendererChrome extends AbstractSpellCastingMobRenderer
{
	private MixinDoppelRendererChrome(EntityRendererProvider.Context context, AbstractSpellCastingMobModel model)
	{
		super(context, model);
	}

	@Inject(method = "<init>", remap = false, require = 1, at = @At("RETURN"))
	private void foreverfixes$chromeLayer(EntityRendererProvider.Context context, CallbackInfo ci)
	{
		this.addRenderLayer(new DoppelChromeLayer(this));
	}
}
