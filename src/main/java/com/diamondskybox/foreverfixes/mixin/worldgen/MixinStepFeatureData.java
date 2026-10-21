package com.diamondskybox.foreverfixes.mixin.worldgen;

import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.ToIntFunction;

/**
 * One decoration step's feature list and the lookup from feature to its position in that list. Biolith's resilient feature
 * indexer (used when the vanilla sorter finds feature-order cycles) can pair a list with a lookup that answers past its end
 * ("Index 66 out of bounds for length 66" in applyBiomeDecoration). The lookup now answers -1 for any position outside the list,
 * which {@link MixinChunkGeneratorDecoration} skips: the chunk generates without that one decoration instead of failing forever.
 */
@Mixin(value = FeatureSorter.StepFeatureData.class, remap = false)
public abstract class MixinStepFeatureData
{
	@Shadow(remap = false)
	@Final
	private List<PlacedFeature> features;

	@Shadow(remap = false)
	@Final
	@Mutable
	private ToIntFunction<PlacedFeature> indexMapping;

	@Inject(method = "<init>(Ljava/util/List;Ljava/util/function/ToIntFunction;)V", remap = false, require = 1, at = @At("RETURN"))
	private void foreverfixes$boundIndexMapping(CallbackInfo ci)
	{
		final ToIntFunction<PlacedFeature> lookup = this.indexMapping;
		final int size = this.features.size();
		this.indexMapping = feature -> {
			final int index = lookup.applyAsInt(feature);
			return index < size ? index : -1;
		};
	}
}
