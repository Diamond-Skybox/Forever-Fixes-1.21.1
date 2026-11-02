package com.diamondskybox.foreverfixes.mixin.immersivepetroleum;

import com.diamondskybox.foreverfixes.ForeverFixesCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Immersive Petroleum places oil reservoirs during world generation while holding one lock for all reservoirs, and looks up each
 * column's biome through the server level - a full chunk load from inside world generation. With two generators running (Chunky's
 * pregen and Distant Horizons generating LODs for a player) the thread holding the lock waited for a chunk that the other worker
 * threads, all waiting for the lock, were to generate: the server froze. The biome is read from the world's biome source, which
 * places the biomes in the first place, so no chunk is needed.
 */
@Mixin(targets = "flaxbeard.immersivepetroleum.common.world.FeatureReservoir", remap = false)
public abstract class MixinFeatureReservoir
{
	@Redirect(method = "scanChunkForNewReservoirs", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/server/level/ServerLevel;getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;"))
	private static Holder<Biome> foreverfixes$generatorBiome(ServerLevel level, BlockPos pos)
	{
		if (!ForeverFixesCommonConfig.enabled(ForeverFixesCommonConfig.IP_RESERVOIR_GENERATOR_BIOMES))
		{
			return level.getBiome(pos);
		}
		final ServerChunkCache chunks = level.getChunkSource();
		return chunks.getGenerator().getBiomeSource().getNoiseBiome(QuartPos.fromBlock(pos.getX()), QuartPos.fromBlock(pos.getY()),
				QuartPos.fromBlock(pos.getZ()), chunks.randomState().sampler());
	}
}
