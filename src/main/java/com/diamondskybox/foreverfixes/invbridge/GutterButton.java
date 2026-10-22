package com.diamondskybox.foreverfixes.invbridge;

import com.diamondskybox.foreverfixes.ForeverFixes;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** An 18 x 18 button with another mod's inventory-button art, for Better Inventory's gutter ({@link InventoryButtons}). */
final class GutterButton extends AbstractButton
{
	private static final ResourceLocation FRAME = ResourceLocation.withDefaultNamespace("widget/button");
	private static final ResourceLocation FRAME_HOVERED = ResourceLocation.withDefaultNamespace("widget/button_highlighted");
	private static final int SIZE = 18;

	/** Draws the button face at x, y (18 x 18). */
	interface Face
	{
		void draw(GuiGraphics graphics, int x, int y, boolean hovered);
	}

	private final Face face;
	private final Runnable action;
	private boolean broken;

	GutterButton(int x, int y, String labelKey, Face face, Runnable action)
	{
		super(x, y, SIZE, SIZE, Component.translatable(labelKey));
		this.face = face;
		this.action = action;
		setTooltip(Tooltip.create(Component.translatable(labelKey)));
	}

	/** The mod's own button sprites (normal, highlighted), which carry their own frame. */
	static Face ownSprite(ResourceLocation normal, ResourceLocation hovered)
	{
		return (graphics, x, y, isHovered) -> graphics.blitSprite(isHovered ? hovered : normal, x, y, SIZE, SIZE);
	}

	/** A vanilla button frame with a region of the given texture centred on it. */
	static Face icon(ResourceLocation texture, int u, int v, int width, int height, int textureWidth, int textureHeight)
	{
		return (graphics, x, y, isHovered) ->
		{
			graphics.blitSprite(isHovered ? FRAME_HOVERED : FRAME, x, y, SIZE, SIZE);
			graphics.blit(texture, x + (SIZE - width) / 2, y + (SIZE - height) / 2, u, v, width, height, textureWidth, textureHeight);
		};
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		if (broken)
		{
			return;
		}
		try
		{
			RenderSystem.enableBlend();
			face.draw(graphics, getX(), getY(), isHoveredOrFocused());
			RenderSystem.disableBlend();
		}
		catch (LinkageError | RuntimeException e)
		{
			broken = true;
			ForeverFixes.LOGGER.warn("Better Inventory button {} could not be drawn: {}", getMessage().getString(), e.toString());
		}
	}

	@Override
	public void onPress()
	{
		if (broken)
		{
			return;
		}
		try
		{
			action.run();
		}
		catch (LinkageError | RuntimeException e)
		{
			broken = true;
			ForeverFixes.LOGGER.warn("Better Inventory button {} no longer matches its mod and is disabled: {}", getMessage().getString(), e.toString());
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output)
	{
		defaultButtonNarrationText(output);
	}
}
