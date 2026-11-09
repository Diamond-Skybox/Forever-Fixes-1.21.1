package com.diamondskybox.foreverfixes;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Centre of mass of every nearby Sable sub-level, shown while the player holds a chosen item (default: Create Simulated's
 * Creative Physics Staff). Sable keeps the mass data on the server only, so the client measures the sub-level's plot blocks
 * itself with Sable's own {@link MassTracker} (same block masses and per-block centres Sable uses), once a second per
 * sub-level while the item is held. Drawn as a marker visible through blocks, with the total mass above it, and optionally
 * the sub-level's block bounds.
 */
public final class SableCenterOfMass extends RenderType
{
	private static final long REFRESH_TICKS = 20L;
	private static final double RANGE = 128.0D;
	private static final float HALF = 0.18F;
	private static final RenderType THROUGH = RenderType.create("foreverfixes_center_of_mass", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 1536,
			false, true, RenderType.CompositeState.builder().setShaderState(POSITION_COLOR_SHADER).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setDepthTestState(NO_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).createCompositeState(false));
	private static final Map<ClientSubLevel, Entry> CACHE = new WeakHashMap<>();

	private static final class Entry
	{
		long refreshAt;
		Vector3d centre;
		double mass;
	}

	private SableCenterOfMass()
	{
		super("foreverfixes_center_of_mass_holder", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, false, () -> {}, () -> {});
	}

	public static void onStage(RenderLevelStageEvent event)
	{
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
		{
			return;
		}
		final Minecraft mc = Minecraft.getInstance();
		final LocalPlayer player = mc.player;
		final ClientLevel level = mc.level;
		if (player == null || level == null || !holding(player))
		{
			if (!CACHE.isEmpty())
			{
				CACHE.clear();
			}
			return;
		}
		final ClientSubLevelContainer container = SubLevelContainer.getContainer(level);
		if (container == null)
		{
			return;
		}
		final Vec3 camera = event.getCamera().getPosition();
		final float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
		final long now = level.getGameTime();
		final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		final PoseStack poseStack = event.getPoseStack();
		final boolean bounds = ForeverFixesCommonConfig.SPEC.isLoaded() && ForeverFixesCommonConfig.SABLE_COM_BOUNDS.get();
		final Vector3d world = new Vector3d();
		for (ClientSubLevel subLevel : container.getAllSubLevels())
		{
			final BoundingBox3dc box = subLevel.boundingBox();
			if (box == null || box.maxX() < camera.x - RANGE || box.minX() > camera.x + RANGE || box.maxZ() < camera.z - RANGE || box.minZ() > camera.z + RANGE)
			{
				continue;
			}
			final Entry entry = CACHE.computeIfAbsent(subLevel, k -> new Entry());
			if (now >= entry.refreshAt)
			{
				entry.refreshAt = now + REFRESH_TICKS;
				measure(level, subLevel, entry);
			}
			final Pose3dc pose = subLevel.renderPose(partialTick);
			if (pose == null)
			{
				continue;
			}
			if (bounds)
			{
				drawBounds(poseStack, buffers.getBuffer(RenderType.lines()), subLevel, pose, camera);
			}
			if (entry.centre == null)
			{
				continue;
			}
			pose.transformPosition(entry.centre, world);
			poseStack.pushPose();
			poseStack.translate(world.x - camera.x, world.y - camera.y, world.z - camera.z);
			final VertexConsumer quads = buffers.getBuffer(THROUGH);
			final Matrix4f m = poseStack.last().pose();
			cube(quads, m, HALF, 255, 64, 220, 200);
			slab(quads, m, 1.5F, 0.03F, 0.03F, 255, 90, 90, 220);
			slab(quads, m, 0.03F, 1.5F, 0.03F, 90, 255, 90, 220);
			slab(quads, m, 0.03F, 0.03F, 1.5F, 90, 140, 255, 220);
			poseStack.popPose();
			buffers.endBatch(THROUGH);
			final String name = subLevel.getName();
			final String label = String.format(Locale.ROOT, "%s%,.0f", name == null || name.isEmpty() ? "" : name + "  ", entry.mass);
			DebugRenderer.renderFloatingText(poseStack, buffers, label, world.x, world.y + 0.45D, world.z, 0xFFFFFFFF, 0.02F, true, 0.0F, true);
		}
		buffers.endBatch(RenderType.lines());
	}

	private static boolean holding(LocalPlayer player)
	{
		if (!ForeverFixesCommonConfig.SPEC.isLoaded())
		{
			return false;
		}
		final String wanted = ForeverFixesCommonConfig.SABLE_COM_ITEM.get();
		if (wanted == null || wanted.isEmpty())
		{
			return false;
		}
		return is(player.getMainHandItem(), wanted) || is(player.getOffhandItem(), wanted);
	}

	private static boolean is(ItemStack stack, String id)
	{
		return !stack.isEmpty() && id.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
	}

	private static void measure(ClientLevel level, ClientSubLevel subLevel, Entry entry)
	{
		entry.centre = null;
		try
		{
			final LevelPlot plot = subLevel.getPlot();
			final BoundingBox3ic blocks = plot == null ? null : plot.getBoundingBox();
			if (blocks == null)
			{
				return;
			}
			final MassTracker tracker = MassTracker.build(level, blocks);
			if (tracker == null || tracker.isInvalid() || tracker.getMass() <= 0.0D || tracker.getCenterOfMass() == null)
			{
				return;
			}
			entry.centre = new Vector3d(tracker.getCenterOfMass());
			entry.mass = tracker.getMass();
		}
		catch (RuntimeException e)
		{
			ForeverFixes.LOGGER.debug("Sable centre of mass: could not measure {}", subLevel, e);
		}
	}

	/** The sub-level's block bounds (plot space) as a wire box in the world, through the sub-level's pose. */
	private static void drawBounds(PoseStack poseStack, VertexConsumer lines, ClientSubLevel subLevel, Pose3dc pose, Vec3 camera)
	{
		final LevelPlot plot = subLevel.getPlot();
		final BoundingBox3ic b = plot == null ? null : plot.getBoundingBox();
		if (b == null)
		{
			return;
		}
		final double[] xs = {b.minX(), b.maxX() + 1.0D};
		final double[] ys = {b.minY(), b.maxY() + 1.0D};
		final double[] zs = {b.minZ(), b.maxZ() + 1.0D};
		final Vector3d[] corners = new Vector3d[8];
		for (int i = 0; i < 8; i++)
		{
			corners[i] = pose.transformPosition(new Vector3d(xs[i & 1], ys[(i >> 1) & 1], zs[(i >> 2) & 1]), new Vector3d());
			corners[i].sub(camera.x, camera.y, camera.z);
		}
		final Matrix4f m = poseStack.last().pose();
		final int[][] edges = {{0, 1}, {2, 3}, {4, 5}, {6, 7}, {0, 2}, {1, 3}, {4, 6}, {5, 7}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
		for (int[] e : edges)
		{
			final Vector3d a = corners[e[0]];
			final Vector3d c = corners[e[1]];
			final float dx = (float) (c.x - a.x);
			final float dy = (float) (c.y - a.y);
			final float dz = (float) (c.z - a.z);
			final float len = Math.max(1.0E-6F, (float) Math.sqrt(dx * dx + dy * dy + dz * dz));
			lines.addVertex(m, (float) a.x, (float) a.y, (float) a.z).setColor(255, 220, 80, 255).setNormal(poseStack.last(), dx / len, dy / len, dz / len);
			lines.addVertex(m, (float) c.x, (float) c.y, (float) c.z).setColor(255, 220, 80, 255).setNormal(poseStack.last(), dx / len, dy / len, dz / len);
		}
	}

	private static void cube(VertexConsumer v, Matrix4f m, float h, int r, int g, int b, int a)
	{
		slab(v, m, h, h, h, r, g, b, a);
	}

	/** An axis-aligned box centred on the origin with half extents hx, hy, hz, all six faces. */
	private static void slab(VertexConsumer v, Matrix4f m, float hx, float hy, float hz, int r, int g, int b, int a)
	{
		quad(v, m, -hx, -hy, -hz, -hx, hy, -hz, hx, hy, -hz, hx, -hy, -hz, r, g, b, a);
		quad(v, m, hx, -hy, hz, hx, hy, hz, -hx, hy, hz, -hx, -hy, hz, r, g, b, a);
		quad(v, m, -hx, -hy, hz, -hx, hy, hz, -hx, hy, -hz, -hx, -hy, -hz, r, g, b, a);
		quad(v, m, hx, -hy, -hz, hx, hy, -hz, hx, hy, hz, hx, -hy, hz, r, g, b, a);
		quad(v, m, -hx, hy, -hz, -hx, hy, hz, hx, hy, hz, hx, hy, -hz, r, g, b, a);
		quad(v, m, -hx, -hy, hz, -hx, -hy, -hz, hx, -hy, -hz, hx, -hy, hz, r, g, b, a);
	}

	private static void quad(VertexConsumer v, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3,
			float x4, float y4, float z4, int r, int g, int b, int a)
	{
		v.addVertex(m, x1, y1, z1).setColor(r, g, b, a);
		v.addVertex(m, x2, y2, z2).setColor(r, g, b, a);
		v.addVertex(m, x3, y3, z3).setColor(r, g, b, a);
		v.addVertex(m, x4, y4, z4).setColor(r, g, b, a);
	}
}
