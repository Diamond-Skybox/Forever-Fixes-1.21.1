package com.diamondskybox.foreverfixes.mixin.ironsspellbooks;

import com.diamondskybox.foreverfixes.ScrollForgeFocus;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.gui.scroll_forge.ScrollForgeMenu;
import io.redspace.ironsspellbooks.gui.scroll_forge.ScrollForgeScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

/** The Scroll Forge lists only the spells the current focus may make (magic.scrollForgeFocus). */
@Mixin(value = ScrollForgeScreen.class, remap = false)
public abstract class MixinScrollForgeScreenFocus
{
	@ModifyVariable(method = "generateSpellList", remap = false, require = 1, at = @At(value = "STORE", ordinal = 0))
	private List<AbstractSpell> foreverfixes$filter(List<AbstractSpell> spells)
	{
		final ItemStack focus = ((ScrollForgeMenu) ((AbstractContainerScreen<?>) (Object) this).getMenu()).getFocusSlot().getItem();
		return spells.stream().filter(spell -> !Boolean.FALSE.equals(ScrollForgeFocus.allows(spell.getSpellResource(), focus))).toList();
	}
}
