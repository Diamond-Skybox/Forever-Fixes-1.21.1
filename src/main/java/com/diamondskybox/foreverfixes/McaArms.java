package com.diamondskybox.foreverfixes;

import com.mojang.blaze3d.vertex.PoseStack;
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
				part.render(pose, buffers.getBuffer(RenderType.entityTranslucent((ResourceLocation) skin)), light, OverlayTexture.NO_OVERLAY, color);
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
}
