package com.diamondskybox.foreverfixes.mixin.simulatedcoasters;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.silvergold.simulatedcoasters.balloon.BalloonResolve;
import dev.silvergold.simulatedcoasters.client.cart.CoasterCartClientMembership;
import dev.silvergold.simulatedcoasters.track.anchor.ChainLiftClientIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Simulated Coasters draws carts, cart couplings, chain lifts and balloon tethers into the shared world buffer after block entities
 * and then flushes that buffer, every frame. With Iris the shared buffer holds everything buffered so far, so the flush sorted and
 * drew it all a second time per frame (spark: ~1.5% of the render thread) even where no coasters exist. The flush is skipped when
 * none of the four renderers has anything to draw; the frame's own later flush then draws the rest as usual. The placement and
 * outline previews drawn before it go through Create's outliner, not this buffer.
 */
@Mixin(targets = "dev.silvergold.simulatedcoasters.SimulatedCoastersClient", remap = false)
public abstract class MixinSimulatedCoastersClient
{
	@WrapOperation(method = "onRenderLevelAfterBlockEntities", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V"))
	private static void foreverfixes$flushOnlyWhenDrawn(MultiBufferSource.BufferSource buffers, Operation<Void> original)
	{
		if (!ForeverFixesConfig.enabled(ForeverFixesConfig.COASTERS_SKIP_IDLE_FLUSH) || foreverfixes$anythingDrawn())
		{
			original.call(buffers);
		}
	}

	/** The renderers' own sources: carts (couplings only exist between carts), chain lift anchors, balloon sub-levels. */
	@Unique
	private static boolean foreverfixes$anythingDrawn()
	{
		if (!(Minecraft.getInstance().level instanceof ClientLevel level))
		{
			return false;
		}
		final boolean[] cart = {false};
		CoasterCartClientMembership.forEachCart(level, subLevel -> cart[0] = true);
		if (cart[0] || ChainLiftClientIndex.anchors().iterator().hasNext())
		{
			return true;
		}
		final SubLevelContainer container = SubLevelContainer.getContainer((Level) level);
		if (container != null)
		{
			for (SubLevel subLevel : container.getAllSubLevels())
			{
				if (!subLevel.isRemoved() && BalloonResolve.isBalloonSubLevel(subLevel))
				{
					return true;
				}
			}
		}
		return false;
	}
}
