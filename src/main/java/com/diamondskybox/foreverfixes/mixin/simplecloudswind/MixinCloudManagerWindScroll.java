package com.diamondskybox.foreverfixes.mixin.simplecloudswind;

import com.diamondskybox.foreverfixes.ForeverFixesCommonConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.nonamecrackers2.simpleclouds.common.world.CloudManager;
import net.minecraft.util.Mth;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Simple Clouds + Living Wind: the drifting cloud shapes scroll along a fixed heading. Simple Clouds moves the noise that carves
 * the clouds along a circle of radius 100: each tick the scroll angle grows by the cloud speed and the scroll offset is that point on
 * the circle, so the drift slowly turns on its own. With the cloud wind on, the scroll angle is the heading the server set from
 * Living Wind (see {@code CloudWind}, synced by Simple Clouds): its tick increment is kept as the step instead of turning the angle,
 * and the offset moves that far along the heading (the mesh samples noise at position + scroll, so the offset moves against the
 * wind for the shapes to move with it). Both sides, identical, so nothing has to be synced but the heading.
 */
@Mixin(value = CloudManager.class, remap = false)
public abstract class MixinCloudManagerWindScroll
{
	@Shadow(remap = false)
	protected float scrollAngle;
	@Shadow(remap = false)
	protected float scrollXO;
	@Shadow(remap = false)
	protected float scrollZO;
	@Shadow(remap = false)
	protected float scrollX;
	@Shadow(remap = false)
	protected float scrollZ;

	/** Simple Clouds' circle radius: an angle step s moves its offset about 100 s */
	@Unique
	private static final float SCROLL_RADIUS = 100F;
	@Unique
	private float foreverfixes$step = Float.NaN;

	@WrapOperation(method = "tick", remap = false, require = 1,
			at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, remap = false,
					target = "Ldev/nonamecrackers2/simpleclouds/common/world/CloudManager;scrollAngle:F"))
	private void foreverfixes$keepHeading(CloudManager manager, float angle, Operation<Void> original)
	{
		if (ForeverFixesCommonConfig.CLOUD_WIND.get())
		{
			this.foreverfixes$step = angle - this.scrollAngle;
			return;
		}
		this.foreverfixes$step = Float.NaN;
		original.call(manager, angle);
	}

	@Inject(method = "tick", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Ldev/nonamecrackers2/simpleclouds/common/world/CloudManager;determineUseVanillaWeather()Z"))
	private void foreverfixes$scrollAlongHeading(CallbackInfo ci)
	{
		if (Float.isNaN(this.foreverfixes$step))
		{
			return;
		}
		final float distance = this.foreverfixes$step * SCROLL_RADIUS;
		this.scrollX = this.scrollXO - Mth.cos(this.scrollAngle) * distance;
		this.scrollZ = this.scrollZO - Mth.sin(this.scrollAngle) * distance;
		this.foreverfixes$step = Float.NaN;
	}
}
