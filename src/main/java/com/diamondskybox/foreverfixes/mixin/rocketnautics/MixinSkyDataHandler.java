package com.diamondskybox.foreverfixes.mixin.rocketnautics;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Create: Cosmonautics starts its edge-of-space effects at a fixed camera Y 1000: the planet drawn beneath (full at 1500), the space
 * fog and black sky, daylight stars, the vanilla sun and moon swap, high wind and the altitude render-distance ramp. Every one of them
 * reads the altitude as camera Y plus SkyDataHandler.getHeightOffsetForLevel, which is always 0 (its override map is never filled).
 * In the 2096-block-tall JJThunder overworld Y 1000 is mountain terrain, so the planet render appeared while standing on it.
 * For dimensions taller than Y 1000 the offset now moves all of these up to the dimension's build top (or a configured Y).
 * Client only; the switch to deep space (dimension_transfer_height) is server-side and unchanged.
 */
@Mixin(targets = "dev.devce.rocketnautics.SkyDataHandler", remap = false)
public abstract class MixinSkyDataHandler
{
	/** Cosmonautics' own threshold. */
	private static final double COSMONAUTICS_START_Y = 1000D;

	@ModifyReturnValue(method = "getHeightOffsetForLevel", remap = false, require = 1, at = @At("RETURN"))
	private static double foreverfixes$raiseSpaceEffects(double offset, @Local(argsOnly = true) ResourceKey<Level> dimension)
	{
		if (!ForeverFixesConfig.enabled(ForeverFixesConfig.RAISE_SPACE_EFFECTS))
		{
			return offset;
		}
		final ClientLevel level = Minecraft.getInstance().level;
		if (level == null || level.dimension() != dimension || level.getMaxBuildHeight() <= COSMONAUTICS_START_Y)
		{
			return offset;
		}
		final double configured = ForeverFixesConfig.value(ForeverFixesConfig.SPACE_EFFECTS_START_Y, 0D);
		final double start = configured > 0D ? configured : level.getMaxBuildHeight();
		return start > COSMONAUTICS_START_Y ? offset - (start - COSMONAUTICS_START_Y) : offset;
	}
}
