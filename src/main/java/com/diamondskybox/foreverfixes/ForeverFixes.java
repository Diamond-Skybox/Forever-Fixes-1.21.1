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

/** Compatibility fixes between mods of the 1.21.1 pack. No content, no commands; a server tick listener (ShipReseat), a player tick listener (BlockedEnchantments), trade listeners (VillagerTradeFixes), loot table listeners (ChestCoins, LootFixes) and client listeners (RagdollSourceHider, InventoryBridge, StormFogFixes); each fix only applies when its target mod is installed. */
@Mod(ForeverFixes.MOD_ID)
public final class ForeverFixes
{
	public static final String MOD_ID = "foreverfixes";
	public static final Logger LOGGER = LoggerFactory.getLogger("ForeverFixes");

	public ForeverFixes(IEventBus modBus, ModContainer container)
	{
		container.registerConfig(ModConfig.Type.CLIENT, ForeverFixesConfig.SPEC);
		container.registerConfig(ModConfig.Type.COMMON, ForeverFixesCommonConfig.SPEC);
		NeoForge.EVENT_BUS.addListener(WildMagic::onDamage);
		NeoForge.EVENT_BUS.addListener(ShipReseat::onServerTick);
		NeoForge.EVENT_BUS.addListener(BlockedEnchantments::onPlayerTick);
		NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, VillagerTradeFixes::onVillagerTrades);
		NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, VillagerTradeFixes::onWandererTrades);
		NeoForge.EVENT_BUS.addListener(EventPriority.LOW, ChestCoins::onLootTableLoad);
		NeoForge.EVENT_BUS.addListener(SnowBiomes::onTagsUpdated);
		if (ModList.get().isLoaded("aerowarptics") && ModList.get().isLoaded("dimensional_sable"))
		{
			AeroWarpticsBridge.init();
		}
		if (ModList.get().isLoaded("irons_spellbooks") && (ModList.get().isLoaded("chromeware") || ModList.get().isLoaded("energyfield")))
		{
			NeoForge.EVENT_BUS.addListener(ChromeMagic::onPlayerTick);
			if (FMLEnvironment.dist == Dist.CLIENT)
			{
				NeoForge.EVENT_BUS.addListener(ChromeTooltips::onTooltip);
			}
		}
		if (ModList.get().isLoaded("chromeware"))
		{
			NeoForge.EVENT_BUS.addListener(CyberArmorFit::onPlayerTick);
		}
		if (ModList.get().isLoaded("chromeware") && ModList.get().isLoaded("darkdoppelganger"))
		{
			NeoForge.EVENT_BUS.addListener(DoppelChrome::onJoin);
			NeoForge.EVENT_BUS.addListener(DoppelChrome::onTick);
			NeoForge.EVENT_BUS.addListener(DoppelChrome::onLeave);
			NeoForge.EVENT_BUS.addListener(DoppelChrome::onDrops);
			NeoForge.EVENT_BUS.addListener(DoppelChrome::onServerStopped);
			if (FMLEnvironment.dist == Dist.CLIENT)
			{
				NeoForge.EVENT_BUS.addListener(DoppelAfterimages::onNameTag);
			}
		}
		if (ModList.get().isLoaded("vampirism"))
		{
			NeoForge.EVENT_BUS.addListener(VampireHoly::onPlayerTick);
		}
		if (ModList.get().isLoaded("lootr"))
		{
			NeoForge.EVENT_BUS.addListener(BossChests::onDamage);
			NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, BossChests::onDrops);
			NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, BossChests::onRightClick);
			NeoForge.EVENT_BUS.addListener(BossChests::onBreak);
			NeoForge.EVENT_BUS.addListener(BossChests::onServerTick);
			NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.TagsUpdatedEvent e) -> BossChests.clearCache());
		}
		if (ModList.get().isLoaded("chestdna"))
		{
			ChestDnaGear.init();
		}
		if (ModList.get().isLoaded("opencomputers"))
		{
			NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, LootFixes::onLootTableLoad);
		}
		if (FMLEnvironment.dist == Dist.CLIENT && ModList.get().isLoaded("sable_player_ragdoll"))
		{
			NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, RagdollSourceHider::onRenderPlayer);
		}
		if (FMLEnvironment.dist == Dist.CLIENT && ModList.get().isLoaded("betterinventory"))
		{
			InventoryBridge.register();
		}
		if (FMLEnvironment.dist == Dist.CLIENT)
		{
			NeoForge.EVENT_BUS.addListener(KeybindKeeper::onClientTick);
			NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, WeatherFogLight::onFogColor);
		}
		if (FMLEnvironment.dist == Dist.CLIENT && ModList.get().isLoaded("petrichor") && ModList.get().isLoaded("distanthorizons"))
		{
			PetrichorDhDepth.register();
		}
		if (FMLEnvironment.dist == Dist.CLIENT && ModList.get().isLoaded("petrichor") && ModList.get().isLoaded("hoarfrost"))
		{
			NeoForge.EVENT_BUS.addListener(SnowHaze::onClientTick);
		}
		if (FMLEnvironment.dist == Dist.CLIENT && ModList.get().isLoaded("mobends"))
		{
			BendsRecorder.register();
			BendsPackCommand.register();
		}
		if (FMLEnvironment.dist == Dist.CLIENT && ModList.get().isLoaded("chromeware") && ModList.get().isLoaded("mobends"))
		{
			ChromeFirstPersonDeferral.register();
			BendsConditions.register();
			ChromeJoints.register();
		}
		if (FMLEnvironment.dist == Dist.CLIENT && ModList.get().isLoaded("sable"))
		{
			NeoForge.EVENT_BUS.addListener(ShipRainCover::onClientTick);
			NeoForge.EVENT_BUS.addListener(SableCenterOfMass::onStage);
		}
		if (FMLEnvironment.dist == Dist.CLIENT && ModList.get().isLoaded("bettersimpleclouds") && ModList.get().isLoaded("simpleclouds"))
		{
			// lowest: the after-particles depths need JourneyMap's waypoint beams, which it draws at that stage too
			NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, StormFogFixes::onStage);
		}
		if (ModList.get().isLoaded("simpleclouds") && (ModList.get().isLoaded("petrichor") || ModList.get().isLoaded("hoarfrost")))
		{
			NeoForge.EVENT_BUS.addListener(CloudWeather::onLevelTick);
			NeoForge.EVENT_BUS.addListener(CloudWeather::onLogout);
		}
		if (ModList.get().isLoaded("simpleclouds") && ModList.get().isLoaded("livingwind"))
		{
			NeoForge.EVENT_BUS.addListener(CloudWind::onLevelTick);
			NeoForge.EVENT_BUS.addListener(CloudWind::onRegionSpawn);
		}
		LOGGER.info("Forever Fixes loaded");
	}
}
