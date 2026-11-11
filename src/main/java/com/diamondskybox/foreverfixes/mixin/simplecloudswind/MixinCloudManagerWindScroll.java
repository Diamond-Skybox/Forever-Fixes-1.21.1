package com.diamondskybox.foreverfixes.mixin.simplecloudswind;

import com.diamondskybox.foreverfixes.CloudWind;
import dev.nonamecrackers2.simpleclouds.common.world.CloudManager;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Simple Clouds + Living Wind: the cloud shapes drift along Living Wind. Simple Clouds moves the noise that carves the clouds
 * along a circle of radius 100: each tick the scroll angle grows by the cloud speed and the scroll offset is that point on the
 * circle, so the drift direction turns slowly on its own and cannot be set without the clouds jumping. After Simple Clouds has
 * advanced the angle, this takes the step it added (the speed), moves the offset by that step along the wind instead (the mesh
 * samples noise at position + scroll, so the offset moves against the wind for the shapes to move with it) and sets the angle to
 * the wind heading, which is what {@code calculateWindDirection} (rain slant, storm fog shadow map, high cloud layer, Better
 * Simple Clouds' rain wind) reads back. A server sync or a world load sets the angle to something else; the step is clamped so
 * that moves the clouds by a block at most.
 */
@Mixin(value = CloudManager.class, remap = false)
public abstract class MixinCloudManagerWindScroll
{
	@Shadow(remap = false)
	@Final
	protected Level level;
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

	/** Simple Clouds' circle radius: a step of angle s moves the offset about 100 s */
	@Unique
	private static final float SCROLL_RADIUS = 100F;
	/** the largest angle step taken as cloud movement; the cloud speed times 1e-4 is far below this */
	@Unique
	private static final float MAX_STEP = 0.01F;
	@Unique
	private float foreverfixes$heading = Float.NaN;

	@Inject(method = "tick", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Ldev/nonamecrackers2/simpleclouds/common/world/CloudManager;determineUseVanillaWeather()Z"))
	private void foreverfixes$scrollWithWind(CallbackInfo ci)
	{
		final float[] wind = CloudWind.vector(this.level);
		if (wind == null)
		{
			this.foreverfixes$heading = Float.NaN;
			return;
		}
		final float step = Float.isNaN(this.foreverfixes$heading) ? 0F : Mth.clamp(this.scrollAngle - this.foreverfixes$heading, 0F, MAX_STEP);
		final float distance = step * SCROLL_RADIUS * wind[2];
		this.scrollX = this.scrollXO - wind[0] * distance;
		this.scrollZ = this.scrollZO - wind[1] * distance;
		this.scrollAngle = (float) Mth.atan2(wind[1], wind[0]);
		this.foreverfixes$heading = this.scrollAngle;
	}
}
