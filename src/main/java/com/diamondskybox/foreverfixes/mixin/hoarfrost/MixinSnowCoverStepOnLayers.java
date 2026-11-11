package com.diamondskybox.foreverfixes.mixin.hoarfrost;

import com.pockyl.hoarfrost.sim.SnowCover;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Hoarfrost: footsteps dent the snow cover on vanilla snow layers. Its cover surface for a column is the block shape's top, but a
 * snow layer's collision box is one layer (an eighth of a block) lower than its visible top, so a player standing on snow layers,
 * i.e. anywhere in snowy land and on every drift, has their feet 0.125 below the surface Hoarfrost recorded, outside the 0.05 it
 * allows when looking for the surface under a step. No dent registered unless the foot landed on a lower column (at an edge) or
 * on a plant, whose column falls through to the block below. The allowance becomes 0.2.
 */
@Mixin(value = SnowCover.class, remap = false)
public abstract class MixinSnowCoverStepOnLayers
{
	@ModifyConstant(method = "surfaceBelow", remap = false, require = 1, constant = @Constant(doubleValue = 0.05D))
	private double foreverfixes$feetSinkIntoSnowLayers(double tolerance)
	{
		return 0.2D;
	}
}
