package com.diamondskybox.foreverfixes;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import dev.nonamecrackers2.simpleclouds.mixin.MixinPostChain;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Better Simple Clouds' wet ground on Sable ships, in the ship's own frame. Its rain shader takes the puddle noise, the raindrop
 * rings and the open-sky test at a pixel's world x, z (the sky test from a heightmap rebuilt about once a second), so on a moving
 * ship the water slid across the deck and the dry patch under the ship lagged behind it. Here the shader (Forever Fixes' copy,
 * bsc_rain_haze.fsh) gets, for the 8 nearest ships whose column tops {@link ShipRainCover} knows: a matrix from camera-relative
 * world space to the ship's plot space, the ship's size, its up direction and where its column tops sit in a float texture.
 * A pixel inside a ship then takes its water pattern and its sky test from the ship, and ground below a ship's column counts as
 * covered on the frame it is drawn. The ships are left out of the heightmap then. Render thread only.
 */
public final class ShipWetGround
{
	private static final String PROGRAM = "foreverfixes:bsc_rain_haze";
	public static final int MAX_SHIPS = 8;
	/** floats per ship in ShipData: matrix (16), size + seed (4), atlas x, z (4), up (4) */
	private static final int STRIDE = 28;
	private static final int ATLAS_WIDTH = 1024;
	private static final int ATLAS_MAX_HEIGHT = 1024;
	private static final float EMPTY = -1.0E4F;

	private static final float[] data = new float[MAX_SHIPS * STRIDE];
	private static int atlasTexture;
	/** what the atlas holds: the ships (identity) and their tops versions, in order */
	private static final List<Object> atlasKey = new ArrayList<>();
	private static final List<int[]> atlasSpots = new ArrayList<>();

	private ShipWetGround()
	{
	}

	public static boolean active()
	{
		return ForeverFixesConfig.enabled(ForeverFixesConfig.SABLE_SHIP_WET_GROUND) && ForeverFixesConfig.enabled(ForeverFixesConfig.SABLE_SHIP_RAIN);
	}

	/** before Better Simple Clouds' rain haze pass runs: hands the nearest ships to the shader */
	public static void beforeRainHaze(PostChain chain)
	{
		if (chain == null)
		{
			return;
		}
		int count = 0;
		final Minecraft mc = Minecraft.getInstance();
		if (active() && mc.gameRenderer != null)
		{
			final Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
			final List<ShipRainCover.Tops> ships = ShipRainCover.knownTops();
			ships.sort(Comparator.comparingDouble(t -> distance(t, camera)));
			final List<ShipRainCover.Tops> nearest = ships.subList(0, Math.min(MAX_SHIPS, ships.size()));
			updateAtlas(nearest);
			for (int i = 0; i < nearest.size() && count < MAX_SHIPS; i++)
			{
				final int[] spot = atlasSpots.get(i);
				final Pose3dc pose = nearest.get(i).subLevel().renderPose();
				if (spot == null || pose == null)
				{
					continue;
				}
				write(count++, nearest.get(i), pose, camera, spot);
			}
		}
		for (PostPass pass : ((MixinPostChain) chain).simpleclouds$getPostPasses())
		{
			final EffectInstance effect = pass.getEffect();
			if (!PROGRAM.equals(effect.getName()))
			{
				continue;
			}
			final Uniform countUniform = effect.getUniform("ShipCount");
			final Uniform dataUniform = effect.getUniform("ShipData");
			if (countUniform == null || dataUniform == null)
			{
				continue;
			}
			countUniform.set((float) count);
			dataUniform.set(data);
			final int texture = atlasTexture;
			effect.setSampler("ShipTopsSampler", () -> texture);
		}
	}

	private static double distance(ShipRainCover.Tops tops, Vec3 camera)
	{
		final Vector3dc p = tops.subLevel().renderPose() == null ? null : tops.subLevel().renderPose().position();
		return p == null ? Double.MAX_VALUE : camera.distanceToSqr(p.x(), p.y(), p.z());
	}

	/** ship i's matrix (camera-relative world -> plot, relative to the tops' minimum), size, seed, atlas spot and up */
	private static void write(int i, ShipRainCover.Tops tops, Pose3dc pose, Vec3 camera, int[] spot)
	{
		final Vector3dc position = pose.position();
		final Vector3dc rotationPoint = pose.rotationPoint();
		final Vector3dc scale = pose.scale();
		final Quaterniond inverse = new Quaterniond(pose.orientation()).conjugate();
		final Matrix4d matrix = new Matrix4d()
				.translation(rotationPoint.x() - tops.minX(), rotationPoint.y() - tops.minY(), rotationPoint.z() - tops.minZ())
				.scale(1.0 / scale.x(), 1.0 / scale.y(), 1.0 / scale.z())
				.rotate(inverse)
				.translate(camera.x - position.x(), camera.y - position.y(), camera.z - position.z());
		final int base = i * STRIDE;
		final double[] m = new double[16];
		matrix.get(m);
		for (int k = 0; k < 16; k++)
		{
			data[base + k] = (float) m[k];
		}
		data[base + 16] = tops.sizeX();
		data[base + 17] = tops.sizeY();
		data[base + 18] = tops.sizeZ();
		// a per-ship offset of the water pattern, so two ships do not share their puddles
		data[base + 19] = (float) Math.floorMod(System.identityHashCode(tops.subLevel()), 997);
		data[base + 20] = spot[0];
		data[base + 21] = spot[1];
		data[base + 22] = 0.0F;
		data[base + 23] = 0.0F;
		final Vector3d up = pose.orientation().transform(new Vector3d(0.0, 1.0, 0.0));
		data[base + 24] = (float) up.x;
		data[base + 25] = (float) up.y;
		data[base + 26] = (float) up.z;
		data[base + 27] = 0.0F;
	}

	/** packs the ships' column tops into rows of a float texture (first free y per column, EMPTY where the column is empty) */
	private static void updateAtlas(List<ShipRainCover.Tops> ships)
	{
		final List<Object> key = new ArrayList<>();
		for (ShipRainCover.Tops tops : ships)
		{
			key.add(tops.subLevel());
			key.add(tops.version());
		}
		if (key.equals(atlasKey) && atlasTexture != 0)
		{
			return;
		}
		atlasKey.clear();
		atlasKey.addAll(key);
		atlasSpots.clear();
		int x = 0;
		int y = 0;
		int rowHeight = 0;
		for (ShipRainCover.Tops tops : ships)
		{
			if (tops.sizeX() > ATLAS_WIDTH)
			{
				atlasSpots.add(null);
				continue;
			}
			if (x + tops.sizeX() > ATLAS_WIDTH)
			{
				x = 0;
				y += rowHeight;
				rowHeight = 0;
			}
			if (y + tops.sizeZ() > ATLAS_MAX_HEIGHT)
			{
				atlasSpots.add(null);
				continue;
			}
			atlasSpots.add(new int[] {x, y});
			x += tops.sizeX();
			rowHeight = Math.max(rowHeight, tops.sizeZ());
		}
		final int height = Math.max(1, y + rowHeight);
		final FloatBuffer pixels = MemoryUtil.memAllocFloat(ATLAS_WIDTH * height);
		try
		{
			for (int i = 0; i < ATLAS_WIDTH * height; i++)
			{
				pixels.put(i, EMPTY);
			}
			for (int s = 0; s < ships.size(); s++)
			{
				final int[] spot = atlasSpots.get(s);
				if (spot == null)
				{
					continue;
				}
				final ShipRainCover.Tops tops = ships.get(s);
				final int[] t = tops.tops();
				for (int i = 0; i < tops.count(); i += 3)
				{
					pixels.put((spot[1] + t[i + 2]) * ATLAS_WIDTH + spot[0] + t[i], t[i + 1]);
				}
			}
			final int previous = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
			if (atlasTexture == 0)
			{
				atlasTexture = GlStateManager._genTexture();
			}
			GlStateManager._bindTexture(atlasTexture);
			GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
			GlStateManager._pixelStore(GL11.GL_UNPACK_ROW_LENGTH, 0);
			GlStateManager._pixelStore(GL11.GL_UNPACK_SKIP_PIXELS, 0);
			GlStateManager._pixelStore(GL11.GL_UNPACK_SKIP_ROWS, 0);
			GlStateManager._pixelStore(GL11.GL_UNPACK_ALIGNMENT, 4);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_R32F, ATLAS_WIDTH, height, 0, GL11.GL_RED, GL11.GL_FLOAT, pixels);
			GlStateManager._bindTexture(previous);
		}
		finally
		{
			MemoryUtil.memFree(pixels);
		}
	}
}
