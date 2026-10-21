package com.diamondskybox.foreverfixes.mixin.gyro;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Create Aeronautics: Gyroscope Stabilizers holds a ship's up axis to a fixed world up with a PID controller (restoring
 * strength, angular damping, integral). In Create: Cosmonautics deep space that goes wrong: the ship arrives rotated into the
 * orbital frame, every gyroscope throws its full restoring torque plus a winding-up integral at the sudden tilt, and with no
 * air drag to bleed the overshoot the ship spins instead of settling. "World up" has no meaning in orbit anyway. In
 * {@code rocketnautics:deep_space} the restoring and integral gains now read as zero, so the gyroscopes only damp rotation:
 * they stop a spin and hold whatever attitude the ship has. Planets and every other dimension are unchanged.
 */
@Mixin(targets = "com.raprek.gyro.content.gyroscope.GyroscopeBlockEntity", remap = false)
public abstract class MixinGyroscopeBlockEntity
{
	@Unique
	private static final ResourceLocation foreverfixes$DEEP_SPACE = ResourceLocation.fromNamespaceAndPath("rocketnautics", "deep_space");

	@WrapOperation(method = "sable$physicsTick", remap = false, require = 1,
			at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, remap = false, target = "Lcom/raprek/gyro/Config;gyroscopeStrength:D"))
	private double foreverfixes$strength(Operation<Double> original)
	{
		return foreverfixes$attitudeHoldOnly() ? 0.0D : original.call();
	}

	@WrapOperation(method = "sable$physicsTick", remap = false, require = 1,
			at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, remap = false, target = "Lcom/raprek/gyro/Config;gyroscopeIntegral:D"))
	private double foreverfixes$integral(Operation<Double> original)
	{
		return foreverfixes$attitudeHoldOnly() ? 0.0D : original.call();
	}

	@Unique
	private boolean foreverfixes$attitudeHoldOnly()
	{
		final Level level = ((BlockEntity) (Object) this).getLevel();
		return level != null && foreverfixes$DEEP_SPACE.equals(level.dimension().location());
	}
}
