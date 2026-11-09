package com.diamondskybox.foreverfixes.mixin.chromewaremobends;

import com.chromeware.client.cyberware.GorillaAnimation;
import com.chromeware.client.launcher.LauncherAnimation;
import com.chromeware.client.mantis.MantisAnimation;
import com.chromeware.client.sandevistan.AfterimagePlayerEntity;
import com.chromeware.client.monowire.MonowireAnimation;
import com.diamondskybox.foreverfixes.ForeverFixesCommonConfig;
import com.diamondskybox.foreverfixes.PackBones;
import goblinbob.mobends.compat.PlayerAnimationLibCompat;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.lib.math.vector.Vec3f;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * Chromeware poses the arms through Player Animation Lib while Mantis Blades, the Monowire, the Projectile Launcher or Gorilla
 * Arms are out. Those clips move the whole arm and author no elbow bend, and Mo' Bends treats a Player Animation Lib arm as a
 * rigid vanilla arm: it straightens its forearm toward the (absent) bend with the arm's full influence, so the elbows lock while
 * an implant is deployed, in Mo' Bends' own animations and in Bends packs alike. The straightening (applyLimbBend) is skipped
 * while one of Chromeware's arm clips is rendering; the forearm keeps whatever Mo' Bends or the pack gave it under Chromeware's
 * upper-arm pose. Chromeware's clips don't drive legs, so the legs never reach applyLimbBend in that state. Other Player
 * Animation Lib animations (emotes and the like) are unchanged.
 */
@Mixin(value = PlayerAnimationLibCompat.class, remap = false)
public abstract class MixinPalCompatKeepForearms
{
	@Unique
	private static final ThreadLocal<LivingEntity> foreverfixes$ENTITY = new ThreadLocal<>();
	@Unique
	private static final ThreadLocal<Set<String>> foreverfixes$PACK_BONES = ThreadLocal.withInitial(Set::of);

	@Inject(method = "applyToPose", remap = false, require = 1, at = @At("HEAD"), cancellable = true)
	private static void foreverfixes$rememberEntity(BipedEntityData<?> data, float partialTicks, CallbackInfoReturnable<Boolean> cir)
	{
		foreverfixes$ENTITY.set(data == null ? null : data.getEntity());
		foreverfixes$PACK_BONES.set(data == null ? Set.of() : PackBones.take(data));
		if (data != null && data.getEntity() instanceof AfterimagePlayerEntity)
		{
			// Chromeware's Sandevistan proxy carries the real player's finished pose (MixinMutatorProxyPose); its frozen PAL layers must not re-pose it
			cir.setReturnValue(false);
		}
	}

	/**
	 * A Bends pack that poses an arm owns it while a Chromeware arm clip is the Player Animation Lib pose: Chromeware's hold pose
	 * (blades up by the face) is not copied over the pack's arm. Deploy, retract and slash still show when no pack node animates
	 * the arm, and the pack's own transitions blend to the live pose when it lets go.
	 */
	@Inject(method = "applyUpperPart", remap = false, require = 1, at = @At("HEAD"), cancellable = true)
	private static void foreverfixes$packOwnsArm(Object stack, String bone, float partialTicks, ModelPartTransform part, ModelPartTransform lowerPart,
			float[] rest, Vec3f bodyScale, boolean followsBody, CallbackInfoReturnable<Boolean> cir)
	{
		if (!("rightArm".equals(bone) || "leftArm".equals(bone)) || !ForeverFixesCommonConfig.SPEC.isLoaded() || !ForeverFixesCommonConfig.PACK_OWNS_ARMS.get())
		{
			return;
		}
		if (foreverfixes$PACK_BONES.get().contains(bone) && foreverfixes$ENTITY.get() instanceof AbstractClientPlayer player && foreverfixes$chromewareArms(player))
		{
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "applyLimbBend", remap = false, require = 1, at = @At("HEAD"), cancellable = true)
	private static void foreverfixes$keepForearm(CallbackInfo ci)
	{
		if (foreverfixes$ENTITY.get() instanceof AbstractClientPlayer player && foreverfixes$chromewareArms(player))
		{
			ci.cancel();
		}
	}

	@Unique
	private static boolean foreverfixes$chromewareArms(AbstractClientPlayer player)
	{
		try
		{
			return MantisAnimation.rendering(player) || MonowireAnimation.rendering(player) || LauncherAnimation.rendering(player)
					|| GorillaAnimation.rendering(player);
		}
		catch (RuntimeException e)
		{
			return false;
		}
	}
}
