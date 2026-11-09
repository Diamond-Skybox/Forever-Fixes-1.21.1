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
import org.joml.Vector3f;

import java.util.Set;

/**
 * Chromeware x Mo' Bends (client). Mo' Bends draws players with its own two-piece limbs, so Chromeware's way of hiding a
 * replaced flesh limb (cancelling the vanilla arm's draw) never reaches them, and the cyber limb mesh only follows the upper
 * arm/thigh. Here: flesh limbs replaced by unskinned cyber limbs (or missing) are hidden in Mo' Bends' model, and the cyber mesh's
 * forearm/hand and shin/foot pieces are turned by Mo' Bends' current elbow/knee transform.
 */
public final class CyberLimbBends
{
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

	/** The player whose third-person render is running, or null. */
	public static Player drawing()
	{
		return DRAWING.get();
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

	/** Mantis Blades / Projectile Launcher / Extendo Grip draw their own skin copy of the forearm: Mo' Bends' flesh forearm goes. */
	public static boolean forearmReplaced(Player player, BodyPart arm)
	{
		final HumanoidArm side = arm == BodyPart.LEFT_ARM ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
		try
		{
			return com.chromeware.mantis.MantisImplant.hasHardware(player, side) || com.chromeware.launcher.ProjectileLauncher.active(player, side)
					|| com.chromeware.extendo.ExtendoGrip.active(player, side);
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
		return new Matrix4f(rig).invert().mul(hingeSpace(joint, null)).mul(rig);
	}

	/** Chromeware's per-piece placement for one limb draw, with Mo' Bends' elbow/knee bend added below the joint (see ChromeJoints). */
	public static CyberLimbRenderer.Fitting bend(BodyPart part, float center, boolean slim, CyberLimbRenderer.Fitting original)
	{
		final boolean arm = part == BodyPart.LEFT_ARM || part == BodyPart.RIGHT_ARM;
		final boolean left = part == BodyPart.LEFT_ARM || part == BodyPart.LEFT_LEG;
		final ModelPartTransform joint = joint(part);
		final ChromeJoints.Limb limb = ChromeJoints.limb(arm);
		if (joint == null || limb == null)
		{
			return original;
		}
		if (limb.cut())
		{
			return original; // cutDraw draws this limb split at the seam
		}
		final Matrix4f lower = meshSpace(joint, center, left, slim, limb.hinge());
		final float bend = ChromeJoints.bendDegrees(joint.rotation.getSmooth());
		return new CyberLimbRenderer.Fitting()
		{
			@Override
			public Matrix4f pose(String piece)
			{
				Matrix4f result = original == null ? null : original.pose(piece);
				final Matrix4f tweak = ChromeJoints.pieceMatrix(arm, piece);
				if (tweak != null)
				{
					result = result == null ? new Matrix4f(tweak) : new Matrix4f(tweak).mul(result);
				}
				if (!limb.lower().contains(piece))
				{
					return result;
				}
				return result == null ? new Matrix4f(lower) : new Matrix4f(lower).mul(result);
			}

			@Override
			public boolean hidden(String piece)
			{
				final ChromeJoints.Rule rule = limb.rule(piece);
				return rule != null && rule.hidden(bend) || original != null && original.hidden(piece);
			}
		};
	}

	/**
	 * Draws a cyber arm or leg split at Mo' Bends' joint (ChromeJoints mode "cut"): each quad of Chromeware's mesh is clipped at the
	 * seam plane y = seam (mesh space, Java y down); what lies above the plane is drawn with the upper bone, what lies below is turned
	 * by Mo' Bends' bend about the hinge. Mirrors Chromeware's draw otherwise (centre, mirror, slim, per-piece fittings, white
	 * vertex colour). Returns false when not applicable, so Chromeware's own draw runs.
	 */
	public static boolean cutDraw(BodyPart part, net.minecraft.client.model.geom.ModelPart bone, float center, boolean slim, PoseStack stack,
			com.mojang.blaze3d.vertex.VertexConsumer buffer, int light, int overlay, CyberLimbRenderer.Fitting fitting)
	{
		final boolean arm = part == BodyPart.LEFT_ARM || part == BodyPart.RIGHT_ARM;
		final boolean leg = part == BodyPart.LEFT_LEG || part == BodyPart.RIGHT_LEG;
		if (!arm && !leg)
		{
			return false;
		}
		final ModelPartTransform joint = joint(part);
		final ChromeJoints.Limb limb = ChromeJoints.limb(arm);
		if (joint == null || limb == null || !limb.cut())
		{
			return false;
		}
		final java.util.List<ChromeLimbMesh.Quad> quads = ChromeLimbMesh.quads(arm ? "arm" : "leg");
		if (quads == null || quads.isEmpty())
		{
			return false;
		}
		if (!bone.visible)
		{
			return true;
		}
		final boolean left = part == BodyPart.LEFT_ARM || part == BodyPart.LEFT_LEG;
		final Matrix4f lower = meshSpace(joint, center, left, slim, limb.hinge());
		final float bend = ChromeJoints.bendDegrees(joint.rotation.getSmooth());
		final float seam = limb.seam() / 16.0F;
		stack.pushPose();
		try
		{
			bone.translateAndRotate(stack);
			stack.translate(center, 0.0F, 0.0F);
			if (left)
			{
				stack.scale(-1.0F, 1.0F, 1.0F);
			}
			if (slim)
			{
				stack.scale(0.75F, 1.0F, 1.0F);
			}
			final PoseStack.Pose pose = stack.last();
			final float[] above = new float[5 * 6];
			final float[] below = new float[5 * 6];
			for (ChromeLimbMesh.Quad quad : quads)
			{
				final ChromeJoints.Rule rule = limb.rule(quad.piece());
				if (rule != null && rule.hidden(bend) || fitting != null && fitting.hidden(quad.piece()))
				{
					continue;
				}
				final Matrix4f base = fitting == null ? null : fitting.pose(quad.piece());
				final Matrix4f tweak = ChromeJoints.pieceMatrix(arm, quad.piece());
				final Matrix4f pre = tweak == null ? base : base == null ? tweak : new Matrix4f(tweak).mul(base);
				final int[] counts = split(quad, seam, above, below);
				if (counts[0] > 0)
				{
					emit(buffer, pose, pre, quad.normal(), above, counts[0], light, overlay);
				}
				if (counts[1] > 0)
				{
					final Matrix4f moved = pre == null ? lower : new Matrix4f(lower).mul(pre);
					emit(buffer, pose, moved, quad.normal(), below, counts[1], light, overlay);
				}
			}
		}
		finally
		{
			stack.popPose();
		}
		return true;
	}

	/** Clips a quad (x y z u v per vertex) at the plane y = seam into the polygon above (y <= seam) and below; returns their vertex counts. */
	private static int[] split(ChromeLimbMesh.Quad quad, float seam, float[] above, float[] below)
	{
		final Vector3f[] p = quad.positions();
		final float[] uv = quad.uvs();
		final int n = p.length;
		boolean anyAbove = false, anyBelow = false;
		for (Vector3f v : p)
		{
			anyAbove |= v.y < seam - 1.0E-5F;
			anyBelow |= v.y > seam + 1.0E-5F;
		}
		if (!anyAbove || !anyBelow)
		{
			final float[] out = anyBelow ? below : above;
			for (int i = 0; i < n; i++)
			{
				put(out, i, p[i].x, p[i].y, p[i].z, uv[i * 2], uv[i * 2 + 1]);
			}
			return anyBelow ? new int[] {0, n} : new int[] {n, 0};
		}
		int a = 0, b = 0;
		for (int i = 0; i < n; i++)
		{
			final int j = (i + 1) % n;
			final float di = p[i].y - seam, dj = p[j].y - seam;
			if (di <= 0.0F)
			{
				put(above, a++, p[i].x, p[i].y, p[i].z, uv[i * 2], uv[i * 2 + 1]);
			}
			if (di >= 0.0F)
			{
				put(below, b++, p[i].x, p[i].y, p[i].z, uv[i * 2], uv[i * 2 + 1]);
			}
			if (di < 0.0F && dj > 0.0F || di > 0.0F && dj < 0.0F)
			{
				final float t = di / (di - dj);
				final float x = p[i].x + (p[j].x - p[i].x) * t, z = p[i].z + (p[j].z - p[i].z) * t;
				final float u = uv[i * 2] + (uv[j * 2] - uv[i * 2]) * t, v = uv[i * 2 + 1] + (uv[j * 2 + 1] - uv[i * 2 + 1]) * t;
				put(above, a++, x, seam, z, u, v);
				put(below, b++, x, seam, z, u, v);
			}
		}
		return new int[] {a, b};
	}

	private static void put(float[] out, int index, float x, float y, float z, float u, float v)
	{
		final int o = index * 5;
		out[o] = x;
		out[o + 1] = y;
		out[o + 2] = z;
		out[o + 3] = u;
		out[o + 4] = v;
	}

	/** Emits a convex polygon (3-5 vertices) as quads, the way Chromeware emits its quads. */
	private static void emit(com.mojang.blaze3d.vertex.VertexConsumer buffer, PoseStack.Pose pose, Matrix4f moved, Vector3f normal, float[] poly, int count,
			int light, int overlay)
	{
		final Matrix4f matrix = moved == null ? pose.pose() : new Matrix4f(pose.pose()).mul(moved);
		final Vector3f n = moved == null ? pose.transformNormal(normal, new Vector3f())
				: pose.transformNormal(moved.transformDirection(normal, new Vector3f()).normalize(), new Vector3f());
		final Vector3f p = new Vector3f();
		for (int start = 1; start + 1 < count; start += 2)
		{
			final int[] idx = {0, start, start + 1, Math.min(start + 2, count - 1)};
			for (int k : idx)
			{
				matrix.transformPosition(poly[k * 5], poly[k * 5 + 1], poly[k * 5 + 2], p);
				buffer.addVertex(p.x, p.y, p.z, -1, poly[k * 5 + 3], poly[k * 5 + 4], overlay, light, n.x, n.y, n.z);
			}
		}
	}

	/** Mo' Bends' elbow bend for the arm being drawn, in an arm mesh's space (Gorilla Arms), or null when not bending. */
	public static Matrix4f elbow(boolean left, float center)
	{
		final ModelPartTransform joint = joint(left ? BodyPart.LEFT_ARM : BodyPart.RIGHT_ARM);
		return joint == null ? null : meshSpace(joint, center, left, false, null);
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
	 * The joint's bend in the limb bone's space, hinged at {@code hinge} (pixels from the limb pivot, Java space; null = Mo' Bends' own
	 * pivot); Chromeware applies piece matrices after centring, mirroring and slimming the mesh, so the bend is conjugated into that space.
	 */
	private static Matrix4f meshSpace(ModelPartTransform joint, float center, boolean left, boolean slim, Vector3f hinge)
	{
		final Matrix4f bone = hingeSpace(joint, hinge);
		final Matrix4f mesh = new Matrix4f().translate(center, 0.0F, 0.0F).scale(left ? -1.0F : 1.0F, 1.0F, 1.0F).scale(slim ? 0.75F : 1.0F, 1.0F, 1.0F);
		return new Matrix4f(mesh).invert().mul(bone).mul(mesh);
	}

	/**
	 * Mo' Bends turns the lower segment about its pivot p: A = T(p) R T(-p). The same turn about a hinge h, keeping the segment's
	 * animated offset o, is T(h + o) R T(-h) = T(h + o - p) A T(p - h). With h = p this is A plus the offset.
	 */
	private static Matrix4f hingeSpace(ModelPartTransform joint, Vector3f hinge)
	{
		final Matrix4f a = boneSpace(joint);
		final float k = 0.0625F * joint.offsetScale;
		final float px = joint.position.x * k, py = joint.position.y * k, pz = joint.position.z * k;
		final float ox = joint.offset.x * k, oy = joint.offset.y * k, oz = joint.offset.z * k;
		final float hx = hinge == null ? px : hinge.x / 16.0F, hy = hinge == null ? py : hinge.y / 16.0F, hz = hinge == null ? pz : hinge.z / 16.0F;
		return new Matrix4f().translate(hx + ox - px, hy + oy - py, hz + oz - pz).mul(a).translate(px - hx, py - hy, pz - hz);
	}

}
