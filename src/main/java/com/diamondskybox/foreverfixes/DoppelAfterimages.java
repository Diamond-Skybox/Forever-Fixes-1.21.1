package com.diamondskybox.foreverfixes;

import com.chromeware.client.sandevistan.AfterimageRenderContext;
import com.chromeware.client.sandevistan.AfterimageTimeline;
import com.chromeware.client.sandevistan.ContraptionMesh;
import com.chromeware.client.sandevistan.SandevistanClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.bandit.darkdoppelganger.entity.DarkDoppelgangerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Sandevistan afterimages for the Dark Doppelganger. Chromeware only leaves afterimages behind players: it captures the player's
 * rendered model into a mesh every so far it moves and replays the meshes in its cycling colours until the field ends. The boss's
 * field is a Chromeware field too (DoppelChrome), so the boss is added to Chromeware's afterimage timeline (same slots, colours and
 * fade-out) and its trail is captured from its own renderer (chrome limbs included) and drawn with Chromeware's palette mesh draw.
 */
public final class DoppelAfterimages
{
	/** Same spacing as Chromeware's player trails. */
	private static final double SPACING = 0.86D;
	private static final Map<Integer, Trail> TRAILS = new HashMap<>();
	private static AfterimageTimeline timeline;
	private static ClientLevel level;
	private static boolean capturing;

	private DoppelAfterimages()
	{
	}

	private static boolean active(Entity entity)
	{
		return entity instanceof DarkDoppelgangerEntity && !entity.isInvisible() && SandevistanClient.isActive(entity);
	}

	/** Chromeware's timeline only opens sessions for the owners it is given each frame. */
	public static void addOwners(Set<UUID> owners, AfterimageTimeline chromewareTimeline)
	{
		timeline = chromewareTimeline;
		final ClientLevel world = Minecraft.getInstance().level;
		if (world == null)
		{
			return;
		}
		for (Entity entity : world.entitiesForRendering())
		{
			if (active(entity))
			{
				owners.add(entity.getUUID());
			}
		}
	}

	public static void prepare(RenderLevelStageEvent event)
	{
		final Minecraft mc = Minecraft.getInstance();
		if (mc.level != level)
		{
			TRAILS.clear();
			level = mc.level;
		}
		if (level == null || timeline == null)
		{
			return;
		}
		final float tick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
		final Set<Integer> live = new HashSet<>();
		for (Entity entity : level.entitiesForRendering())
		{
			if (active(entity) && entity instanceof DarkDoppelgangerEntity boss)
			{
				live.add(boss.getId());
				TRAILS.computeIfAbsent(boss.getId(), id -> new Trail()).tick(boss, boss.getPosition(tick), tick);
			}
		}
		TRAILS.values().forEach(t -> t.echoes.removeIf(e -> !e.frame.alive()));
		TRAILS.entrySet().removeIf(e -> !live.contains(e.getKey()) && e.getValue().echoes.isEmpty());
	}

	public static boolean hasTrails()
	{
		return !TRAILS.isEmpty();
	}

	public static boolean render(RenderLevelStageEvent event, MultiBufferSource.BufferSource buffers)
	{
		if (TRAILS.isEmpty())
		{
			return false;
		}
		boolean drawn = false;
		final Vec3 camera = event.getCamera().getPosition();
		final PoseStack stack = event.getPoseStack();
		for (Trail trail : TRAILS.values())
		{
			trail.echoes.removeIf(e -> !e.frame.alive());
			for (Echo echo : trail.echoes)
			{
				final Vec3 p = echo.position;
				final double r = echo.mesh.radius() + 0.5D;
				if (echo.mesh.vertexCount() == 0 || !event.getFrustum().isVisible(new AABB(p.x - r, p.y - r, p.z - r, p.x + r, p.y + r, p.z + r)))
				{
					continue;
				}
				stack.pushPose();
				try
				{
					stack.translate(p.x - camera.x, p.y - camera.y, p.z - camera.z);
					// Chromeware's own palette draw (what its echo cache falls back to); the cache class is package-private
					echo.mesh.drawPalette(stack.last(), buffers, echo.frame.color());
					drawn = true;
				}
				finally
				{
					stack.popPose();
				}
			}
		}
		buffers.endBatch();
		return drawn;
	}

	/** The capture renders the boss through its renderer, which would also bake its name tag into the afterimage. */
	public static void onNameTag(RenderNameTagEvent event)
	{
		if (capturing)
		{
			event.setCanRender(TriState.FALSE);
		}
	}

	@SuppressWarnings("unchecked")
	private static ContraptionMesh capture(DarkDoppelgangerEntity boss, int color, float tick)
	{
		final ContraptionMesh.Builder builder = new ContraptionMesh.Builder();
		capturing = true;
		try (AfterimageRenderContext.Scope scope = AfterimageRenderContext.pushPalette(color))
		{
			final EntityRenderer<DarkDoppelgangerEntity> renderer =
					(EntityRenderer<DarkDoppelgangerEntity>) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(boss);
			renderer.render(boss, boss.getYRot(), tick, new PoseStack(), builder, 15728880);
		}
		finally
		{
			capturing = false;
		}
		return builder.build();
	}

	private record Echo(Vec3 position, AfterimageTimeline.Frame frame, ContraptionMesh mesh)
	{
	}

	private static final class Trail
	{
		private final Deque<Echo> echoes = new ArrayDeque<>();
		private long generation = -1L;
		private Vec3 previous;
		private Vec3 direction = Vec3.ZERO;
		private double distance;
		private long emitted = -1L;

		private void tick(DarkDoppelgangerEntity boss, Vec3 position, float tick)
		{
			final UUID owner = boss.getUUID();
			final long slot = timeline.slot(owner);
			final long gen = timeline.generation(owner);
			if (this.generation != gen)
			{
				this.generation = gen;
				this.previous = null;
				this.direction = Vec3.ZERO;
				this.distance = 0.0D;
				this.emitted = -1L;
			}
			final Vec3 placed = this.sample(slot, position);
			if (placed == null)
			{
				return;
			}
			final AfterimageTimeline.Frame frame = timeline.frame(owner);
			if (frame == null)
			{
				return;
			}
			final ContraptionMesh mesh = capture(boss, frame.color(), tick);
			this.echoes.addLast(new Echo(placed, frame, mesh));
			timeline.add(frame, mesh.vertexCount());
		}

		/** Chromeware's spacing: one afterimage per timeline slot once the owner has moved far enough, set a little behind them. */
		private Vec3 sample(long slot, Vec3 position)
		{
			if (this.previous == null)
			{
				this.previous = position;
				return null;
			}
			final Vec3 movement = position.subtract(this.previous);
			this.previous = position;
			final double step = movement.length();
			if (step > 8.0D)
			{
				this.distance = 0.0D;
				this.direction = Vec3.ZERO;
				return null;
			}
			if (step > 1.0E-5D)
			{
				this.distance += step;
				this.direction = movement.scale(1.0D / step);
			}
			if (slot < 0L || slot == this.emitted || this.distance < SPACING)
			{
				return null;
			}
			this.emitted = slot;
			this.distance %= SPACING;
			return position.subtract(this.direction.scale(0.4D));
		}
	}
}
