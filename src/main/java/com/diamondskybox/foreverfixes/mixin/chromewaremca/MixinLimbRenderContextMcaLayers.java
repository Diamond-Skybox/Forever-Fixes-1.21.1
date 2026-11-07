package com.diamondskybox.foreverfixes.mixin.chromewaremca;

import com.chromeware.client.body.LimbRenderContext;
import com.diamondskybox.foreverfixes.McaArms;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Chromeware cuts the flesh it draws over (forearm slots for Mantis / Launcher, Extendo clip, Monowire wrist, whole cyber or missing
 * limbs) only on the player model and armour it registers. MCA draws the player's body with its own skin and clothing layer models,
 * which were never registered, so MCA's forearm covered the implants. They are added to every limb scope Chromeware opens.
 */
@Mixin(value = LimbRenderContext.class, remap = false)
public abstract class MixinLimbRenderContextMcaLayers
{
	@Inject(method = "open", remap = false, require = 1, at = @At("RETURN"))
	private static void foreverfixes$mcaLayers(Player player, HumanoidModel<?> model, boolean inherit, CallbackInfoReturnable<LimbRenderContext> cir)
	{
		if (player instanceof AbstractClientPlayer clientPlayer && cir.getReturnValue() != null)
		{
			for (HumanoidModel<?> layer : McaArms.layerModels(clientPlayer))
			{
				if (layer != model)
				{
					cir.getReturnValue().addModel(layer);
				}
			}
		}
	}
}
