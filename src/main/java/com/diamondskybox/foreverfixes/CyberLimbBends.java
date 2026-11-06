package com.diamondskybox.foreverfixes;

import com.chromeware.body.BodyPart;
import com.chromeware.body.CyberLimbs;
import com.chromeware.body.LimbAttachments;
import com.chromeware.cyberware.ArmImplants;
import net.minecraft.world.entity.HumanoidArm;
import com.chromeware.client.body.CyberLimbRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;

import java.util.Set;

/**
 * Chromeware x Mo' Bends (client). Mo' Bends draws players with its own two-piece limbs, so Chromeware's way of hiding a
 * replaced flesh limb (cancelling the vanilla arm's draw) never reaches them, and the cyber limb mesh only follows the upper
 * arm/thigh. Here: flesh limbs replaced by unskinned cyber limbs (or missing) are hidden in Mo' Bends' model, and the cyber mesh's
 * forearm/hand and shin/foot pieces are turned by Mo' Bends' current elbow/knee transform.
 */
public final class CyberLimbBends
{
	/** Mesh pieces below the elbow (groups "Forearm" and "Hand" of cyber_limbs_mesh.json). */
	private static final Set<String> LOWER_ARM = Set.of("forearm_core", "forearm_front_shell", "outer_forearm_cheek", "inner_forearm_cheek",
			"actuator_cylinder", "actuator_rod", "wrist_band", "palm", "backhand_plate", "forearm_rear_shell_front", "forearm_rear_shell_back");
	/** Mesh pieces below the knee (groups "Shin" and "Foot"). */
	private static final Set<String> LOWER_LEG = Set.of("knee_axle", "knee_cap", "outer_knee_cap", "shin_core", "shin_front_shell",
			"shin_rear_shell", "calf_actuator", "calf_rod", "ankle_band", "foot_block", "heel_plate");

	private static final ThreadLocal<Player> DRAWING = new ThreadLocal<>();

	private CyberLimbBends()
	{
	}

	/** Mo' Bends should not draw this flesh limb: an unskinned cyber limb replaces it, or it is gone. */
	public static boolean fleshHidden(Player player, BodyPart part)
	{
		try
		{
			if (part == BodyPart.BODY)
			{
				return CyberLimbs.chrome(player, part);
			}
			if ((part == BodyPart.LEFT_ARM || part == BodyPart.RIGHT_ARM)
					&& ArmImplants.get(player, part == BodyPart.LEFT_ARM ? HumanoidArm.LEFT : HumanoidArm.RIGHT).replacesLimb())
			{
				return true; // Gorilla Arms: Chromeware draws the whole arm itself
			}
			return CyberLimbs.chrome(player, part) || LimbAttachments.get(player).isMissing(part) && !CyberLimbs.has(player, part);
		}
		catch (RuntimeException e)
		{
			return false;
		}
	}

	/** Third-person entity render scope (LivingEntityRenderer.render); returns the outer value to restore. */
	public static Player enterEntity(Player player)
	{
		final Player outer = DRAWING.get();
		DRAWING.set(player);
		return outer;
	}

	public static void leaveEntity(Player outer)
	{
		if (outer == null)
		{
			DRAWING.remove();
		}
		else
		{
			DRAWING.set(outer);
		}
	}

	/** Mantis Blades / Projectile Launcher draw their own skin copy of the forearm: Mo' Bends' flesh forearm goes. */
	public static boolean forearmReplaced(Player player, BodyPart arm)
	{
		final HumanoidArm side = arm == BodyPart.LEFT_ARM ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
		try
		{
			return com.chromeware.mantis.MantisImplant.hasHardware(player, side) || com.chromeware.launcher.ProjectileLauncher.active(player, side);
		}
		catch (RuntimeException e)
		{
			return false;
		}
	}

	/** The elbow bend in a CyberwareRig's space (centred, mirrored/slimmed, shifted by along), or null when not bending. */
	public static Matrix4f rigElbow(boolean left, boolean slim, float center, float along)
	{
		final ModelPartTransform joint = joint(left ? BodyPart.LEFT_ARM : BodyPart.RIGHT_ARM);
		if (joint == null)
		{
			return null;
		}
		final Matrix4f rig = new Matrix4f().translate(center, 0.0F, 0.0F).scale((left ? -1.0F : 1.0F) * (slim ? 0.75F : 1.0F), 1.0F, 1.0F)
				.translate(0.0F, along, 0.0F);
		return new Matrix4f(rig).invert().mul(boneSpace(joint)).mul(rig);
	}

	/** Chromeware's per-piece placement for one limb draw, with Mo' Bends' elbow/knee bend added below the joint. */
	public static CyberLimbRenderer.Fitting bend(BodyPart part, float center, boolean slim, CyberLimbRenderer.Fitting original)
	{
		final boolean arm = part == BodyPart.LEFT_ARM || part == BodyPart.RIGHT_ARM;
		final boolean left = part == BodyPart.LEFT_ARM || part == BodyPart.LEFT_LEG;
		final ModelPartTransform joint = joint(part);
		if (joint == null)
		{
			return original;
		}
		final Matrix4f lower = meshSpace(joint, center, left, slim);
		final Set<String> pieces = arm ? LOWER_ARM : LOWER_LEG;
		return new CyberLimbRenderer.Fitting()
		{
			@Override
			public Matrix4f pose(String piece)
			{
				final Matrix4f before = original == null ? null : original.pose(piece);
				if (!pieces.contains(piece))
				{
					return before;
				}
				return before == null ? new Matrix4f(lower) : new Matrix4f(lower).mul(before);
			}

			@Override
			public boolean hidden(String piece)
			{
				return original != null && original.hidden(piece);
			}
		};
	}

	/** Mo' Bends' elbow bend for the arm being drawn, in an arm mesh's space (Gorilla Arms), or null when not bending. */
	public static Matrix4f elbow(boolean left, float center)
	{
		final ModelPartTransform joint = joint(left ? BodyPart.LEFT_ARM : BodyPart.RIGHT_ARM);
		return joint == null ? null : meshSpace(joint, center, left, false);
	}

	private static ModelPartTransform joint(BodyPart part)
	{
		final Player player = DRAWING.get();
		if (player == null || MoBendsRenderContext.getCurrentEntity() != player || !MoBendsBridge.animates(player))
		{
			return null;
		}
		final Object data = EntityDatabase.instance.get((LivingEntity) player);
		if (!(data instanceof BipedEntityData<?> biped))
		{
			return null;
		}
		return switch (part)
		{
			case LEFT_ARM -> biped.leftForeArm;
			case RIGHT_ARM -> biped.rightForeArm;
			case LEFT_LEG -> biped.leftForeLeg;
			case RIGHT_LEG -> biped.rightForeLeg;
			default -> null;
		};
	}

	private static Matrix4f boneSpace(ModelPartTransform joint)
	{
		final PoseStack local = new PoseStack();
		joint.applyLocalTransform(local, 0.0625F);
		final float k = 0.0625F * joint.offsetScale;
		return new Matrix4f(local.last().pose())
				.translate(-(joint.position.x + joint.offset.x) * k, -(joint.position.y + joint.offset.y) * k, -(joint.position.z + joint.offset.z) * k);
	}

	/**
	 * The joint's bend in the limb bone's space is L * T(-pivot) (L = Mo' Bends' local transform of the lower segment, whose
	 * vertices sit relative to its pivot); Chromeware applies piece matrices after centring, mirroring and slimming the mesh,
	 * so the bend is conjugated into that space.
	 */
	private static Matrix4f meshSpace(ModelPartTransform joint, float center, boolean left, boolean slim)
	{
		final Matrix4f bone = boneSpace(joint);
		final Matrix4f mesh = new Matrix4f().translate(center, 0.0F, 0.0F).scale(left ? -1.0F : 1.0F, 1.0F, 1.0F).scale(slim ? 0.75F : 1.0F, 1.0F, 1.0F);
		return new Matrix4f(mesh).invert().mul(bone).mul(mesh);
	}
}
