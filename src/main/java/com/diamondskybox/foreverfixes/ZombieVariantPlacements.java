package com.diamondskybox.foreverfixes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/**
 * Zombie Variants registers no spawn placement for its 15 zombies, and an entity type without one may spawn anywhere it fits.
 * Its natural spawns are checked as vanilla zombies before the swap, but a zombie's reinforcements are checked against its own
 * type: a hit cave, lush or dripstone zombie called reinforcements into the open air of a cavern up to 40 blocks away, in any light,
 * and they fell to their deaths (and their rotten flesh made other zombies persistent). Each variant gets the vanilla zombie's
 * placement: on the ground, monster spawn rules.
 */
public final class ZombieVariantPlacements
{
	private ZombieVariantPlacements()
	{
	}

	public static void onRegister(RegisterSpawnPlacementsEvent event)
	{
		if (!ModList.get().isLoaded("zombie_variants") || !ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.ZOMBIE_VARIANTS_GROUNDED))
		{
			return;
		}
		for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE)
		{
			final ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
			if ("zombie_variants".equals(id.getNamespace()) && type.getCategory() == MobCategory.MONSTER)
			{
				register(event, type);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void register(RegisterSpawnPlacementsEvent event, EntityType<?> type)
	{
		event.register((EntityType<Monster>) type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
	}
}
