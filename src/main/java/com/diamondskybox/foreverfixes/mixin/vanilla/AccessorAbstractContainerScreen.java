package com.diamondskybox.foreverfixes.mixin.vanilla;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Sets a container screen's panel geometry (used for the stand-in inventory screen of {@code invbridge.InventoryBridge}). */
@Mixin(AbstractContainerScreen.class)
public interface AccessorAbstractContainerScreen
{
	@Accessor("leftPos")
	void foreverfixes$setLeftPos(int value);

	@Accessor("topPos")
	void foreverfixes$setTopPos(int value);

	@Accessor("imageWidth")
	void foreverfixes$setImageWidth(int value);

	@Accessor("imageHeight")
	void foreverfixes$setImageHeight(int value);
}
