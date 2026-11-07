package com.diamondskybox.foreverfixes;

import com.chromeware.client.cyberware.ImplantTint;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mcabp.wildfire.client.render.WildfireModelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Chromeware x Female Gender Mod: on a bare chrome torso (no Synthskin) the breasts are drawn from the cyber body's own pectoral plate
 * (the brass "Shells" part of cyber_limbs.png), through Chromeware's per-player tinted texture, so they take the player's Shell colour.
 */
public final class ChromeBreasts
{
	// pectoral plate on the 96x32 cyber_limbs sheet: front face 64,20 - 72,28; 1-px edge strips around it
	private static final float W = 96.0F, H = 32.0F;
	private static final float[] FRONT = {64, 20, 72, 28};
	private static final float[] SIDE_INNER = {71, 20, 72, 28};
	private static final float[] SIDE_OUTER = {64, 20, 65, 28};
	private static final float[] TOP = {64, 20, 72, 21};
	private static final float[] BOTTOM = {64, 27, 72, 28};

	private ChromeBreasts()
	{
	}

	public static boolean chromeTorso(Entity entity)
	{
		return entity instanceof Player player && CyberLimbBends.fleshHidden(player, com.chromeware.body.BodyPart.BODY);
	}

	/** Any cyber torso (Synthskin or not): rigid plating, so the gender mod's breast physics is held still. */
	public static boolean rigidTorso(Entity entity)
	{
		try
		{
			return entity instanceof Player player && com.chromeware.body.CyberLimbs.has(player, com.chromeware.body.BodyPart.BODY);
		}
		catch (RuntimeException e)
		{
			return false;
		}
	}

	public static VertexConsumer buffer(Player player, MultiBufferSource buffers)
	{
		return buffers.getBuffer(RenderType.entityCutoutNoCull(ImplantTint.texture(player, "cyber_limbs")));
	}

	/** GenderLayer.renderBox with every face's UVs moved onto the pectoral plate. */
	public static void render(WildfireModelRenderer.ModelBox model, PoseStack stack, VertexConsumer buffer, int light, int overlay, int color)
	{
		final Matrix4f pose = stack.last().pose();
		final Matrix3f normalMatrix = stack.last().normal();
		for (WildfireModelRenderer.TexturedQuad quad : model.quads)
		{
			final WildfireModelRenderer.PositionTextureVertex[] vs = quad.vertexPositions;
			float u0 = Float.MAX_VALUE, v0 = Float.MAX_VALUE, u1 = -Float.MAX_VALUE, v1 = -Float.MAX_VALUE;
			for (WildfireModelRenderer.PositionTextureVertex v : vs)
			{
				u0 = Math.min(u0, v.texturePositionX());
				u1 = Math.max(u1, v.texturePositionX());
				v0 = Math.min(v0, v.texturePositionY());
				v1 = Math.max(v1, v.texturePositionY());
			}
			final float[] rect = rect(quad.normal.getX(), quad.normal.getY(), quad.normal.getZ());
			final Vector3f n = new Vector3f(quad.normal.getX(), quad.normal.getY(), quad.normal.getZ()).mul(normalMatrix);
			for (WildfireModelRenderer.PositionTextureVertex v : vs)
			{
				final float fu = u1 > u0 ? (v.texturePositionX() - u0) / (u1 - u0) : 0.0F;
				final float fv = v1 > v0 ? (v.texturePositionY() - v0) / (v1 - v0) : 0.0F;
				buffer.addVertex(pose, v.x() / 16.0F, v.y() / 16.0F, v.z() / 16.0F)
						.setColor(color)
						.setUv((rect[0] + fu * (rect[2] - rect[0])) / W, (rect[1] + fv * (rect[3] - rect[1])) / H)
						.setOverlay(overlay)
						.setLight(light)
						.setNormal(n.x(), n.y(), n.z());
			}
		}
	}

	private static float[] rect(int nx, int ny, int nz)
	{
		if (nz != 0)
		{
			return FRONT;
		}
		if (ny < 0)
		{
			return TOP;
		}
		if (ny > 0)
		{
			return BOTTOM;
		}
		return nx > 0 ? SIDE_INNER : SIDE_OUTER;
	}
}
