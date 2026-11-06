package com.diamondskybox.foreverfixes.mixin.ironsspellbooks;

import com.diamondskybox.foreverfixes.ScrollForgeFocus;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.gui.scroll_forge.ScrollForgeMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/** The Scroll Forge only makes a scroll when the focus is allowed for that spell (magic.scrollForgeFocus). */
@Mixin(value = ScrollForgeMenu.class, remap = false)
public abstract class MixinScrollForgeMenuFocus
{
	@WrapOperation(method = "setupResultSlot", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lio/redspace/ironsspellbooks/api/registry/SchoolRegistry;getSchoolsFromFocus(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"))
	private List<SchoolType> foreverfixes$focus(ItemStack focus, Operation<List<SchoolType>> original, @Local(argsOnly = true) AbstractSpell spell)
	{
		final Boolean allowed = ScrollForgeFocus.allows(spell.getSpellResource(), focus);
		if (allowed == null)
		{
			return original.call(focus);
		}
		return allowed ? List.of(spell.getSchoolType()) : List.of();
	}
}
