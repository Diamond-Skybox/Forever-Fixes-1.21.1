package com.diamondskybox.foreverfixes.mixin.sable;

import com.diamondskybox.foreverfixes.SableDeferredBlockChange;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Sable's collision callbacks run inside the native Rapier step: a ship hitting glass, leaves or ice breaks it right there
 * ({@code FragileBlockCallback}), and the neighbour updates that follow can place blocks (seen: a Vista TV beside the glass).
 * Every block change goes straight back into Rapier ({@code handleBlockChange} -> {@code newVoxelCollider}) while the step is
 * still running, and the native call waits forever on the step's own lock: the server thread freezes with no crash report.
 * Block changes made during the step are now held and applied right after it returns.
 */
@Mixin(value = SubLevelPhysicsSystem.class, remap = false)
public abstract class MixinSubLevelPhysicsSystemDeferBlockChanges
{
	@Unique
	private final List<SableDeferredBlockChange> foreverfixes$deferred = new ArrayList<>();

	@Shadow(remap = false)
	public abstract void handleBlockChange(SectionPos sectionPos, LevelChunkSection section, int localX, int localY, int localZ,
			BlockState oldState, BlockState newState);

	@Inject(method = "handleBlockChange", remap = false, cancellable = true, require = 1, at = @At("HEAD"))
	private void foreverfixes$holdDuringStep(SectionPos sectionPos, LevelChunkSection section, int localX, int localY, int localZ,
			BlockState oldState, BlockState newState, CallbackInfo ci)
	{
		if (SubLevelPhysicsSystem.IN_PHYSICS_STEP)
		{
			this.foreverfixes$deferred.add(new SableDeferredBlockChange(sectionPos, section, localX, localY, localZ, oldState, newState));
			ci.cancel();
		}
	}

	@Inject(method = "tickPipelinePhysics", remap = false, require = 1,
			at = @At(value = "INVOKE", shift = At.Shift.AFTER, remap = false,
					target = "Ldev/ryanhcode/sable/api/physics/PhysicsPipeline;physicsTick(D)V"))
	private void foreverfixes$applyAfterStep(CallbackInfo ci)
	{
		if (this.foreverfixes$deferred.isEmpty())
		{
			return;
		}
		SubLevelPhysicsSystem.IN_PHYSICS_STEP = false; // Sable clears it on the next line anyway
		final List<SableDeferredBlockChange> changes = new ArrayList<>(this.foreverfixes$deferred);
		this.foreverfixes$deferred.clear();
		for (SableDeferredBlockChange c : changes)
		{
			this.handleBlockChange(c.section(), c.chunkSection(), c.x(), c.y(), c.z(), c.oldState(), c.newState());
		}
	}
}
