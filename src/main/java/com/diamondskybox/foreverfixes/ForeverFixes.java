package com.diamondskybox.foreverfixes;

import com.diamondskybox.foreverfixes.invbridge.InventoryBridge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Compatibility fixes between mods of the 1.21.1 pack. No content, no commands, a server tick listener (ShipReseat) and a client render listener (RagdollSourceHider); each fix only applies when its target mod is installed. */
@Mod(ForeverFixes.MOD_ID)
public final class ForeverFixes
{
	public static final String MOD_ID = "foreverfixes";
	public static final Logger LOGGER = LoggerFactory.getLogger("ForeverFixes");

	public ForeverFixes(IEventBus modBus, ModContainer container)
	{
		container.registerConfig(ModConfig.Type.CLIENT, ForeverFixesConfig.SPEC);
		NeoForge.EVENT_BUS.addListener(ShipReseat::onServerTick);
		if (FMLEnvironment.dist == Dist.CLIENT && ModList.get().isLoaded("sable_player_ragdoll"))
		{
			NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, RagdollSourceHider::onRenderPlayer);
		}
		if (FMLEnvironment.dist == Dist.CLIENT && ModList.get().isLoaded("betterinventory"))
		{
			InventoryBridge.register();
		}
		LOGGER.info("Forever Fixes loaded");
	}
}
