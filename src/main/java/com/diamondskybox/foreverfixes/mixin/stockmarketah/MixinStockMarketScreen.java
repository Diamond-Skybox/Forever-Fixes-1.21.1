package com.diamondskybox.foreverfixes.mixin.stockmarketah;

import by.deokma.stockmarket.neoforge.client.GuiTextures;
import by.deokma.stockmarket.neoforge.client.UIHelper;
import com.diamondskybox.foreverfixes.AuctionHouseTab;
import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Create: Stock Market's screen (Market Terminal, Market Remote, keybind) gets a fourth tab after Shops, Market and Top Sellers that
 * opens Create: Auction House. The tab is laid out exactly like Stock Market's own three, whose sizes depend on their labels.
 */
@Mixin(targets = "by.deokma.stockmarket.neoforge.client.StockMarketScreen", remap = false)
public abstract class MixinStockMarketScreen extends Screen
{
	@Unique
	private static final String[] foreverfixes$TAB_KEYS = { "screen.stockmarket.tab_shops", "screen.stockmarket.tab_market", "screen.stockmarket.tab_top_sellers" };
	@Unique
	private static final ResourceLocation foreverfixes$AUCTION_HOUSE_BLOCK = ResourceLocation.fromNamespaceAndPath("create_auction_house", "auction_house_block");

	protected MixinStockMarketScreen(Component title)
	{
		super(title);
	}

	@Shadow
	private int panelX()
	{
		throw new AssertionError();
	}

	@Shadow
	private int panelY()
	{
		throw new AssertionError();
	}

	@Shadow
	private int tabHeight()
	{
		throw new AssertionError();
	}

	@Inject(method = "drawTabs", at = @At("TAIL"), remap = false, require = 1)
	private void foreverfixes$drawAuctionHouseTab(GuiGraphics gfx, int px, int py, int mx, int my, CallbackInfo ci)
	{
		if (!ForeverFixesConfig.enabled(ForeverFixesConfig.STOCKMARKET_AUCTION_HOUSE_TAB))
		{
			return;
		}
		final int tabH = this.tabHeight();
		final int iconSize = this.foreverfixes$iconSize();
		final int tx = this.foreverfixes$tabX();
		UIHelper.blitScaled(gfx, GuiTextures.TAB_INACTIVE, tx, py + 5, this.foreverfixes$tabWidth(), tabH - 4, 64, 14);
		final int iconY = py + 5 + (tabH - 4 - iconSize) / 2;
		gfx.pose().pushPose();
		gfx.pose().translate(tx + 5, iconY, 0);
		gfx.pose().scale(iconSize / 16F, iconSize / 16F, 1F);
		gfx.renderFakeItem(new ItemStack(BuiltInRegistries.ITEM.get(foreverfixes$AUCTION_HOUSE_BLOCK)), 0, 0);
		gfx.pose().popPose();
		gfx.drawString(this.font, foreverfixes$label(), tx + 5 + iconSize + 4, py + 5 + (tabH - 4 - 8) / 2, -15658735, false);
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
	private void foreverfixes$clickAuctionHouseTab(double mx, double my, int button, CallbackInfoReturnable<Boolean> cir)
	{
		if (!ForeverFixesConfig.enabled(ForeverFixesConfig.STOCKMARKET_AUCTION_HOUSE_TAB))
		{
			return;
		}
		final int tx = this.foreverfixes$tabX();
		final int py = this.panelY();
		if (mx >= tx && mx < tx + this.foreverfixes$tabWidth() && my >= py + 5 && my < py + this.tabHeight() - 1)
		{
			AuctionHouseTab.open();
			cir.setReturnValue(true);
		}
	}

	@Unique
	private int foreverfixes$iconSize()
	{
		return Math.min(16, this.tabHeight() - 6);
	}

	/** Left edge of the fourth tab: after Stock Market's three tabs and their 4-pixel gaps. */
	@Unique
	private int foreverfixes$tabX()
	{
		int tx = this.panelX() + 8;
		for (String key : foreverfixes$TAB_KEYS)
		{
			tx += this.foreverfixes$iconSize() + 4 + this.font.width(I18n.get(key)) + 10 + 4;
		}
		return tx;
	}

	@Unique
	private int foreverfixes$tabWidth()
	{
		return this.foreverfixes$iconSize() + 4 + this.font.width(foreverfixes$label()) + 10;
	}

	@Unique
	private static String foreverfixes$label()
	{
		return I18n.get("foreverfixes.stockmarket.tab_auction_house");
	}
}
