package com.diamondskybox.foreverfixes.mixin.vegandelight;

import com.diamondskybox.foreverfixes.ForeverFixesCommonConfig;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vegan Delight: items dropped by an entity are not stamped "not vegan". Its only stamp is set on every food, drink or sugar any
 * entity drops (a zombie's potato, a villager's crop, a mod machine that drops through the entity), whatever the item is, and the
 * stamp kept those stacks apart from the rest. Its labels from its vegan and not-vegan tags and its recipe scan stay.
 */
@Mixin(targets = "net.player005.vegandelightfabric.labels.VeganLabels", remap = false)
public abstract class MixinVeganLabelsNoDropStamp
{
	@Inject(method = "setIsVegan", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private static void foreverfixes$noDropStamp(ItemStack stack, boolean vegan, CallbackInfo ci)
	{
		if (ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.VEGANDELIGHT_NO_DROP_LABELS))
		{
			ci.cancel();
		}
	}
}
