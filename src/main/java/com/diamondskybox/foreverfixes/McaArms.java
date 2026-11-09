package com.diamondskybox.foreverfixes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * MCA's first-person arm, for mods that draw the player's arm themselves. MCA replaces the arm inside
 * {@code PlayerRenderer.renderHand}: when {@code MCAClient.renderArms(uuid, "right_arm"/"left_arm")} allows it, its skin and
 * clothing layers (fields {@code mca$skinLayer}/{@code mca$clothingLayer} merged into PlayerRenderer by MCA) copy the arm's pose
 * and draw it. Mods that draw their own arm model never reach that code; this does the same steps by reflection, with a
 * translucent render type. Any lookup failure means "not drawn", so the caller falls back to its own arm.
 */
public final class McaArms
{
	private static boolean lookedUp;
	private static Method renderArms;
	private static Field skinLayer;
	private static Field clothingLayer;
	private static Method getSkin;
	private static Method canUse;
	private static Method getColor;

	private McaArms()
	{
	}

	private static synchronized void lookUp()
	{
		if (lookedUp)
		{
			return;
		}
		lookedUp = true;
		try
		{
			renderArms = Class.forName("net.conczin.mca.MCAClient").getMethod("renderArms", UUID.class, String.class);
			final Class<?> villagerLayer = Class.forName("net.conczin.mca.client.render.layer.VillagerLayer");
			skinLayer = PlayerRenderer.class.getDeclaredField("mca$skinLayer");
			skinLayer.setAccessible(true);
			clothingLayer = PlayerRenderer.class.getDeclaredField("mca$clothingLayer");
			clothingLayer.setAccessible(true);
			getSkin = find(villagerLayer, "getSkin", LivingEntity.class);
			canUse = find(villagerLayer, "canUse", ResourceLocation.class);
			getColor = find(villagerLayer, "getColor", LivingEntity.class, float.class);
		}
		catch (ReflectiveOperationException | RuntimeException e)
		{
			ForeverFixes.LOGGER.warn("MCA first-person arms are not available ({}); other mods' arms stay as they are", e.toString());
			renderArms = null;
		}
	}

	/** Whether MCA draws this player with its own body (either arm), i.e. Chromeware's skin pieces should wear the MCA look. */
	public static boolean usesMcaLook(AbstractClientPlayer player)
	{
		return replaces(player, true) || replaces(player, false);
	}

	/** One capture per player/buffer source while a Chromeware arm is drawn; replayed into MCA's layers when it finishes. */
	private static final ThreadLocal<java.util.ArrayDeque<Object[]>> PENDING = ThreadLocal.withInitial(java.util.ArrayDeque::new);

	/**
	 * A stand-in for the player-skin buffer of geometry laid out on the player-skin UVs (Chromeware's skin copies of the forearm, Gorilla
	 * Arms): the vertices are captured and, at {@link #flushLayered()}, written the way MCA paints the body, skin layer tinted with the
	 * skin colour, then the clothing layer. Null if MCA has nothing to draw for this player.
	 */
	public static VertexConsumer layeredSkin(AbstractClientPlayer player, MultiBufferSource buffers)
	{
		lookUp();
		if (renderArms == null)
		{
			return null;
		}
		for (Object[] pending : PENDING.get())
		{
			if (pending[0] == player && pending[1] == buffers)
			{
				return (CapturedVertices) pending[2];
			}
		}
		final CapturedVertices capture = new CapturedVertices();
		PENDING.get().push(new Object[]{player, buffers, capture});
		return capture;
	}

	/** Writes every capture of this thread into MCA's skin and clothing buffers, one buffer at a time. */
	public static void flushLayered()
	{
		final java.util.ArrayDeque<Object[]> pending = PENDING.get();
		while (!pending.isEmpty())
		{
			final Object[] entry = pending.pollLast();
			final AbstractClientPlayer player = (AbstractClientPlayer) entry[0];
			final MultiBufferSource buffers = (MultiBufferSource) entry[1];
			final CapturedVertices capture = (CapturedVertices) entry[2];
			if (capture.isEmpty())
			{
				continue;
			}
			final EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
			if (!(renderer instanceof PlayerRenderer playerRenderer))
			{
				continue;
			}
			try
			{
				for (Field layer : new Field[] { skinLayer, clothingLayer })
				{
					final Object value = layer.get(playerRenderer);
					if (value == null)
					{
						continue;
					}
					final Object texture = getSkin.invoke(value, player);
					if (texture == null || !(boolean) canUse.invoke(value, texture))
					{
						continue;
					}
					final RenderType type = layer == skinLayer ? RenderType.entityCutoutNoCull((ResourceLocation) texture)
							: RenderType.entityTranslucent((ResourceLocation) texture);
					capture.replay(buffers.getBuffer(type), (int) getColor.invoke(value, player, 0F));
				}
			}
			catch (ReflectiveOperationException | RuntimeException e)
			{
				ForeverFixes.LOGGER.warn("MCA skin for Chromeware pieces failed: {}", e.toString());
			}
		}
	}

	private static Field layerModel;

	/** MCA's skin and clothing layer models for this player (empty if MCA doesn't draw them), so Chromeware's limb rules can cover them. */
	public static java.util.List<net.minecraft.client.model.HumanoidModel<?>> layerModels(AbstractClientPlayer player)
	{
		if (!usesMcaLook(player))
		{
			return java.util.List.of();
		}
		final EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
		if (!(renderer instanceof PlayerRenderer playerRenderer))
		{
			return java.util.List.of();
		}
		try
		{
			final java.util.List<net.minecraft.client.model.HumanoidModel<?>> models = new java.util.ArrayList<>(2);
			for (Field layer : new Field[] { skinLayer, clothingLayer })
			{
				final Object value = layer.get(playerRenderer);
				if (value == null)
				{
					continue;
				}
				if (layerModel == null)
				{
					layerModel = Class.forName("net.conczin.mca.client.render.layer.VillagerLayer").getField("model");
				}
				if (layerModel.get(value) instanceof net.minecraft.client.model.HumanoidModel<?> model)
				{
					models.add(model);
				}
			}
			return models;
		}
		catch (ReflectiveOperationException | RuntimeException e)
		{
			return java.util.List.of();
		}
	}

	/** Whether MCA draws this arm of this player in first person. */
	public static boolean replaces(AbstractClientPlayer player, boolean right)
	{
		lookUp();
		if (renderArms == null)
		{
			return false;
		}
		try
		{
			return (boolean) renderArms.invoke(null, player.getUUID(), right ? "right_arm" : "left_arm");
		}
		catch (ReflectiveOperationException | RuntimeException e)
		{
			return false;
		}
	}

	/**
	 * Draws MCA's look on the calling mod's own posed arm part: the skin layer's texture (tinted with the skin colour) and then the
	 * clothing layer's texture for the arm, only the clothing texture for the sleeve. MCA's own layer-model arm drew nothing when
	 * used this way (1.18.0 to 1.18.4), while the caller's arm is known to show. MCA skins use the player-skin layout. Returns
	 * false if MCA does not draw this arm or a call failed, so the caller draws its own.
	 * <p>
	 * The skin layer is cutout and the clothing layer translucent, as MCA's own first-person arm does. Both layers sit on the same
	 * geometry, so whichever is drawn last shows; Iris batches the hand pass by render type and, with no ordering between two
	 * types, draws them in reverse order of first use, which put the skin over the clothing (bare arms with shaders on). Iris
	 * always draws opaque/cutout types before translucent ones, so the clothing stays on top.
	 */
	public static boolean draw(AbstractClientPlayer player, boolean right, ModelPart part, boolean sleeve, PoseStack pose, MultiBufferSource buffers, int light)
	{
		if (!replaces(player, right))
		{
			return false;
		}
		final EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
		if (!(renderer instanceof PlayerRenderer playerRenderer))
		{
			return false;
		}
		try
		{
			for (Field layer : sleeve ? new Field[] { clothingLayer } : new Field[] { skinLayer, clothingLayer })
			{
				final Object value = layer.get(playerRenderer);
				if (value == null)
				{
					continue;
				}
				final Object skin = getSkin.invoke(value, player);
				if (skin == null || !(boolean) canUse.invoke(value, skin))
				{
					continue;
				}
				final int color = (int) getColor.invoke(value, player, 0F);
				final RenderType type = layer == skinLayer ? RenderType.entityCutoutNoCull((ResourceLocation) skin)
						: RenderType.entityTranslucent((ResourceLocation) skin);
				part.render(pose, buffers.getBuffer(type), light, OverlayTexture.NO_OVERLAY, color);
			}
			return true;
		}
		catch (ReflectiveOperationException | RuntimeException e)
		{
			ForeverFixes.LOGGER.warn("MCA first-person arm failed: {}", e.toString());
			return false;
		}
	}

	private static Method find(Class<?> c, String name, Class<?>... args) throws NoSuchMethodException
	{
		for (Class<?> k = c; k != null; k = k.getSuperclass())
		{
			try
			{
				final Method m = k.getDeclaredMethod(name, args);
				m.setAccessible(true);
				return m;
			}
			catch (NoSuchMethodException ignored)
			{
			}
		}
		throw new NoSuchMethodException(name);
	}

	private static java.lang.reflect.Field villagerModel;
	private static boolean villagerLookedUp;

	/**
	 * The arm MCA actually shows in first person: its villager-animation model's arm (MCA copies that arm's pose onto the visible
	 * skin/clothing arms). Null when MCA is absent or the field cannot be found.
	 */
	public static ModelPart shownArm(Object playerRenderer, boolean right)
	{
		if (!villagerLookedUp)
		{
			villagerLookedUp = true;
			try
			{
				villagerModel = net.minecraft.client.renderer.entity.player.PlayerRenderer.class.getDeclaredField("mca$villagerAnimationModel");
				villagerModel.setAccessible(true);
			}
			catch (Throwable t)
			{
				villagerModel = null;
			}
		}
		if (villagerModel == null)
		{
			return null;
		}
		try
		{
			final Object model = villagerModel.get(playerRenderer);
			return model instanceof net.minecraft.client.model.HumanoidModel<?> humanoid ? (right ? humanoid.rightArm : humanoid.leftArm) : null;
		}
		catch (Throwable t)
		{
			return null;
		}
	}
}
