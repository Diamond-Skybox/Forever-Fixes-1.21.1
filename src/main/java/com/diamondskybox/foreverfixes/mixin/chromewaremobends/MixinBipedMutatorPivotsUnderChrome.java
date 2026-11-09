package com.diamondskybox.foreverfixes.mixin.chromewaremobends;

import com.diamondskybox.foreverfixes.ChromeArms;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.world.entity.LivingEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

/**
 * Mo' Bends resolves a player's "adaptive pivots" from the vanilla model once, and refuses to while a Player Animation Lib
 * animation is on that model (an animated position would be baked into a pivot). Until they're resolved it never writes its
 * pose back into the vanilla model parts, which Chromeware's layers (implant hardware, the blade arm's skin and sleeve copies)
 * draw from. Chromeware's blade and wire clips run the whole time an implant is out and only author rotations, so under them
 * the vanilla positions are at rest and resolving is safe; the guard is lifted for those clips. Without this, a Bends pack that
 * owns the arms (or a first-person round trip) left the layers at the Player Animation Lib pose while the arm was elsewhere.
 * <p>
 * The one place the vanilla positions are not at rest is the Player Animation Lib first-person pass, where Chromeware pulls the
 * implant arms toward the camera (several pixels up and forward) before the model renders. If that pass is the first time the
 * player's renderer runs since launch (blades deployed in first person before any third-person look), Mo' Bends would bake the
 * camera-arm positions into its pivots and into the vanilla rest positions it keeps for the rest of the session: arms pushed
 * back, the flesh bicep drawn inside the body, held items at the elbow. Nothing is resolved, snapshotted or stored during that
 * pass; it all happens on the next third-person frame instead.
 */
@Mixin(value = BipedMutator.class, remap = false)
public abstract class MixinBipedMutatorPivotsUnderChrome
{
	@WrapOperation(method = "resolveAdaptivePivots", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lgoblinbob/mobends/compat/PlayerAnimationLibCompat;isAnimatingVanillaModel(Lnet/minecraft/world/entity/LivingEntity;)Z"))
	private boolean foreverfixes$resolveUnderChromeArmClips(LivingEntity entity, Operation<Boolean> original)
	{
		if (FirstPersonMode.isFirstPersonPass())
		{
			return true;
		}
		return original.call(entity) && !ChromeArms.rotationOnly(entity);
	}

	/**
	 * The rest positions come from a static per-model snapshot taken the first time Mo' Bends sees the model; it is kept for
	 * the whole session and every later sync re-reads it, so a snapshot taken during a first-person pass must not be cached.
	 */
	@WrapOperation(method = "restStateOf", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
	private static Object foreverfixes$noRestSnapshotFromFirstPerson(Map<Object, Object> cache, Object model, Object rest, Operation<Object> original)
	{
		if (FirstPersonMode.isFirstPersonPass())
		{
			return null;
		}
		return original.call(cache, model, rest);
	}

	@WrapOperation(method = "syncPosesToVanillaModel", remap = false, require = 1,
			at = @At(value = "FIELD", remap = false, opcode = Opcodes.PUTFIELD, target = "Lgoblinbob/mobends/standard/mutators/BipedMutator;vanillaPositionsStored:Z"))
	private void foreverfixes$noRestPositionsFromFirstPerson(BipedMutator<?, ?, ?> self, boolean stored, Operation<Void> original)
	{
		original.call(self, stored && !FirstPersonMode.isFirstPersonPass());
	}
}
