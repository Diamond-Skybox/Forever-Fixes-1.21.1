package com.diamondskybox.foreverfixes.invbridge;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.nukateam.chassis_core.Global;
import com.nukateam.chassis_core.client.render.utils.GuiUtils;
import com.nukateam.chassis_core.common.data.constants.Gui;
import com.nukateam.chassis_core.common.data.constants.Resources;
import com.nukateam.chassis_core.common.data.enums.ActionType;
import com.nukateam.chassis_core.common.network.PacketSender;
import com.nukateam.chassis_core.common.util.helpers.PlayerUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Items;

/**
 * NTGL: while wearing a chassis, the vanilla inventory gets two tabs on its top edge (inventory, and the chassis screen). Drawn
 * and clicked the same way at the top-left of Better Inventory's panel.
 */
final class ChassisTabsBridge
{
	private ChassisTabsBridge()
	{
	}

	static boolean active()
	{
		return PlayerUtils.isLocalWearingChassis();
	}

	static void renderBackground(GuiGraphics graphics, int left, int top)
	{
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		graphics.blit(Resources.PLAYER_INVENTORY_TABS, left, top - 28, 0, 0, 57, 32);
	}

	static void renderIcons(GuiGraphics graphics, int left, int top)
	{
		Lighting.setupFor3DItems();
		graphics.renderItem(Items.CRAFTING_TABLE.getDefaultInstance(), left + Gui.TOP_TAB_ICON_POS_1.x, top + Gui.TOP_TAB_ICON_POS_1.y);
		Lighting.setupForFlatItems();
		GuiUtils.drawChassisIcon(graphics, left + Gui.TOP_TAB_ICON_POS_2.x, top + Gui.TOP_TAB_ICON_POS_2.y);
	}

	static boolean click(int left, int top, double mouseX, double mouseY)
	{
		if (mouseX >= left + 30 && mouseX < left + 59 && mouseY >= top - 27 && mouseY < top)
		{
			Global.saveMousePos();
			PacketSender.doServerAction(ActionType.OPEN_GUI);
			return true;
		}
		return false;
	}
}
