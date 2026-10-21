package com.diamondskybox.foreverfixes;

import dev.tr7zw.waveycapes.versionless.sim.BasicSimulation;
import dev.tr7zw.waveycapes.versionless.util.CapePoint;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.AbstractList;
import java.util.List;

/**
 * Wavey Capes' cape while Mo' Bends tips the whole body, as in creative flight (port of the 1.12.2 Forever Fixes
 * MoBendsCape.flightBlend / WaveyFlat). Wavey Capes simulates its cape as a rope hanging from an upright body; rendered from a
 * torso that lies flat, the trailing rope stands off the back. Between {@code flatStartDegrees} and {@code flatFullDegrees} of
 * torso tilt the rope points Wavey Capes' renderer reads are blended towards a straight hang (every point right below the one
 * above it), so the cape lies along the back; Wavey Capes' wind sway and per-part rotation still apply to the blended points.
 *
 * Only the waveycapes mixins call this, and the mixin plugin applies them only with both mods installed, so the Mo' Bends and
 * Wavey Capes types used here are present whenever this class loads. Render thread only.
 */
public final class CapeFlight
{
	/** 0 = Wavey Capes' rope as is .. 1 = body tipped flat (cape lies straight along the back). Set per cape render. */
	private static float blend;
	private static final FlatPoints FLAT = new FlatPoints();
	private static final Quaternionf SCRATCH = new Quaternionf();
	/** Vanilla's scale(-1,-1,1) together with the 180-degree yaw of setupRotations; Mo' Bends' whole-body rotations come before it. */
	private static final Quaternionf FLIP = new Quaternionf().rotationX((float) Math.PI);
	private static final Vector3f DOWN = new Vector3f();

	private CapeFlight()
	{
	}

	/** From Wavey Capes' cape layer, before it draws this player's cape. */
	public static void begin(AbstractClientPlayer player)
	{
		blend = 0F;
		if (player == null || !ForeverFixesConfig.enabled(ForeverFixesConfig.WAVEY_FLAT_IN_FLIGHT) || !MoBendsBridge.animates(player))
		{
			return;
		}
		try
		{
			final LivingEntityData<?> data = EntityDatabase.instance.get(player);
			if (data instanceof BipedEntityData<?> biped && biped.body != null)
			{
				final float start = (float) ForeverFixesConfig.value(ForeverFixesConfig.WAVEY_FLAT_START, 20D);
				final float full = (float) ForeverFixesConfig.value(ForeverFixesConfig.WAVEY_FLAT_FULL, 70D);
				blend = Mth.clamp((Math.abs(tiltDegrees(biped)) - start) / Math.max(1F, full - start), 0F, 1F);
			}
		}
		catch (Throwable t)
		{
			blend = 0F;
		}
	}

	/** After Wavey Capes drew the cape. */
	public static void end()
	{
		blend = 0F;
	}

	/**
	 * How far real down is turned from the torso's own down, about the torso's side axis (degrees: 0 upright, towards +-90 when
	 * Mo' Bends lays the body flat). From Mo' Bends' whole-body rotations, vanilla's model flip and the torso's own rotation: the
	 * rotations the pose stack carries when Wavey Capes anchors its cape to the body part.
	 */
	static float tiltDegrees(BipedEntityData<?> biped)
	{
		final Quaternionf q = set(SCRATCH, biped.centerRotation.getSmooth());
		mul(q, biped.renderRotation.getSmooth());
		q.mul(FLIP);
		mul(q, biped.body.rotation.getSmooth());
		final Vector3f down = q.normalize().transformInverse(DOWN.set(0F, -1F, 0F)); // world down in the torso's frame
		return (float) Math.toDegrees(Math.atan2(down.z, down.y));
	}

	private static Quaternionf set(Quaternionf dest, Quaternion q)
	{
		return usable(q) ? dest.set(q.x, q.y, q.z, q.w) : dest.identity();
	}

	private static void mul(Quaternionf dest, Quaternion q)
	{
		if (usable(q))
		{
			dest.mul(q.x, q.y, q.z, q.w);
		}
	}

	/** Mo' Bends leaves out zero-length quaternions when it rotates the pose stack (GlHelper.rotate); so does this. */
	private static boolean usable(Quaternion q)
	{
		return q != null && q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w >= 1.0E-6F;
	}

	/** The rope points as Wavey Capes' renderer should read them: as they are, or blended towards the straight hang. */
	public static List<CapePoint> points(BasicSimulation simulation)
	{
		final List<CapePoint> raw = simulation.getPoints();
		if (blend <= 0F || raw == null || raw.size() < 2)
		{
			return raw;
		}
		return FLAT.set(raw, blend);
	}

	/** View of the rope that blends every point towards the straight hang; one instance, reused for each render. */
	private static final class FlatPoints extends AbstractList<CapePoint>
	{
		private List<CapePoint> source = List.of();
		private float k;
		private FlatPoint[] views = new FlatPoint[0];

		FlatPoints set(List<CapePoint> source, float k)
		{
			this.source = source;
			this.k = k;
			if (views.length < source.size())
			{
				final FlatPoint[] grown = new FlatPoint[source.size()];
				for (int i = 0; i < grown.length; i++)
				{
					grown[i] = i < views.length ? views[i] : new FlatPoint(this, i);
				}
				views = grown;
			}
			return this;
		}

		@Override
		public CapePoint get(int index)
		{
			if (index < 0 || index >= source.size())
			{
				throw new IndexOutOfBoundsException(index + " of " + source.size());
			}
			return views[index];
		}

		@Override
		public int size()
		{
			return source.size();
		}
	}

	/** One rope point, blended by k towards where it would be in a straight hang below the top point. */
	private static final class FlatPoint implements CapePoint
	{
		private final FlatPoints rope;
		private final int index;

		FlatPoint(FlatPoints rope, int index)
		{
			this.rope = rope;
			this.index = index;
		}

		@Override
		public float getLerpX(float delta)
		{
			final float raw = rope.source.get(index).getLerpX(delta);
			if (index == 0)
			{
				return raw;
			}
			return raw + (rope.source.get(0).getLerpX(delta) - raw) * rope.k; // straight hang: no trail behind the top point
		}

		@Override
		public float getLerpY(float delta)
		{
			final float raw = rope.source.get(index).getLerpY(delta);
			if (index == 0)
			{
				return raw;
			}
			return raw + (rope.source.get(0).getLerpY(delta) - index - raw) * rope.k; // one unit per point straight down from the top
		}

		@Override
		public float getLerpZ(float delta)
		{
			final float raw = rope.source.get(index).getLerpZ(delta);
			if (index == 0)
			{
				return raw;
			}
			return raw + (rope.source.get(0).getLerpZ(delta) - raw) * rope.k; // no sideways swing
		}
	}
}
