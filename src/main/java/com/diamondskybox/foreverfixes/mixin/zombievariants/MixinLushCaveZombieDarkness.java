package com.diamondskybox.foreverfixes.mixin.zombievariants;

import com.diamondskybox.foreverfixes.ForeverFixesCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Zombie Variants: zombies in lush caves need darkness again (zombieVariants.lushCaveDarkness, off by default; the server turns it
 * on). Zombie Variants lets vanilla zombies in #zombie_variants:spawns_lush_zombie biomes skip the darkness check and turns them into
 * lush zombies, so lush caves (lit by glow berries) spawn zombies everywhere at any light level. With this on they spawn there only
 * where any monster could. Runs at HEAD, before Zombie Variants' wrapped darkness check in the same method.
 */
@Mixin(Monster.class)
public abstract class MixinLushCaveZombieDarkness
{
	private static final TagKey<Biome> FOREVERFIXES$LUSH = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("zombie_variants", "spawns_lush_zombie"));

	@Inject(method = "checkMonsterSpawnRules(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)Z",
			require = 1, at = @At("HEAD"), cancellable = true)
	private static void foreverfixes$lushCaveDarkness(EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
			RandomSource random, CallbackInfoReturnable<Boolean> cir)
	{
		if (type == EntityType.ZOMBIE && ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.LUSH_CAVE_ZOMBIE_DARKNESS) && level.getBiome(pos).is(FOREVERFIXES$LUSH))
		{
			cir.setReturnValue(level.getDifficulty() != Difficulty.PEACEFUL
					&& (MobSpawnType.ignoresLightRequirements(spawnType) || Monster.isDarkEnoughToSpawn(level, pos, random))
					&& Mob.checkMobSpawnRules(type, level, spawnType, pos, random));
		}
	}
}
