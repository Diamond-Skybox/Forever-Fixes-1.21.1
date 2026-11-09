package com.diamondskybox.foreverfixes.mixin.mobends;

import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.standard.mutators.BipedMutator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Mo' Bends' arm parts of a biped mutator (read by /ffchrome debug and fixarms). */
@Mixin(value = BipedMutator.class, remap = false)
public interface AccessorBipedMutatorArms
{
	@Accessor(value = "leftArm", remap = false)
	BendsModelPart foreverfixes$leftArm();

	@Accessor(value = "rightArm", remap = false)
	BendsModelPart foreverfixes$rightArm();

	@Accessor(value = "leftForeArm", remap = false)
	BendsModelPart foreverfixes$leftForeArm();

	@Accessor(value = "rightForeArm", remap = false)
	BendsModelPart foreverfixes$rightForeArm();

	@Accessor(value = "outerLeftArm", remap = false)
	BendsModelPart foreverfixes$outerLeftArm();

	@Accessor(value = "outerRightArm", remap = false)
	BendsModelPart foreverfixes$outerRightArm();

	@Accessor(value = "outerLeftForeArm", remap = false)
	BendsModelPart foreverfixes$outerLeftForeArm();

	@Accessor(value = "outerRightForeArm", remap = false)
	BendsModelPart foreverfixes$outerRightForeArm();

	@Accessor(value = "adaptivePivotsResolved", remap = false)
	boolean foreverfixes$adaptivePivotsResolved();

	@Accessor(value = "vanillaPositionsStored", remap = false)
	boolean foreverfixes$vanillaPositionsStored();

	@Accessor(value = "vanillaRightArmPos", remap = false)
	float[] foreverfixes$vanillaRightArmPos();

	@Accessor(value = "vanillaLeftArmPos", remap = false)
	float[] foreverfixes$vanillaLeftArmPos();
}
