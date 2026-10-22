package com.diamondskybox.foreverfixes;

import com.google.gson.JsonParser;
import com.mcabp.wildfire.api.IBreastArmorTexture;
import com.mcabp.wildfire.api.IGenderArmor;
import com.mcabp.wildfire.api.impl.BreastArmorTexture;
import com.mcabp.wildfire.api.impl.GenderArmor;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.rpg_foundation.armor_api.client.ArmorOverrides;
import net.rpg_foundation.armor_api.client.ArmorRenderers;
import net.rpg_foundation.armor_api.client.GeoArmorRenderer;
import net.rpg_foundation.armor_api.client.GeoModelCache;
import net.rpg_foundation.armor_api.client.geo.GeoModel;
import net.rpg_foundation.armor_api.client.geo.GeoParser;
import org.joml.Vector2i;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The breast layer of MCA: female gender physics (a Wildfire's Female Gender Mod port) draws a chestplate on the breasts with the
 * vanilla armor texture, {@code <material>_layer_1.png}, sampled at the vanilla chest UVs. Armor drawn by Armor Model API (the RPG
 * series: Paladins, Wizards, Archers, Rogues, Armory) ships no such texture, so the breasts showed the missing texture. For those
 * items the breasts now use the API's own texture for the item, sampled at the front of the torso cube of the item's geo model
 * (bone {@code armorBody}). Items with their own gender armor data or capability keep it.
 */
public final class RpgBreastArmor
{
	/** Geo-model heights (pixels up from the feet) of the top of the body and of the breasts' top and bottom. */
	private static final float BODY_TOP_Y = 24;
	private static final float BREAST_TOP_Y = 22;
	private static final float BREAST_BOTTOM_Y = 18;

	private static final Map<String, Optional<IBreastArmorTexture>> UVS = new HashMap<>();
	private static int generation = -1;

	private RpgBreastArmor()
	{
	}

	/** The texture Armor Model API draws this item with, or null if the API does not draw it. */
	public static ResourceLocation texture(ItemStack stack)
	{
		final GeoArmorRenderer renderer = ArmorRenderers.get(stack.getItem());
		return renderer == null ? null : ArmorOverrides.of(stack).textureOr(renderer.config().texture());
	}

	/** The gender armor settings with breast UVs from the item's geo model, if the mod has nothing more specific for the item. */
	public static IGenderArmor config(ItemStack stack, IGenderArmor original)
	{
		if (original != GenderArmor.DEFAULT)
		{
			return original;
		}
		final GeoArmorRenderer renderer = ArmorRenderers.get(stack.getItem());
		if (renderer == null)
		{
			return original;
		}
		final ArmorOverrides overrides = ArmorOverrides.of(stack);
		final IBreastArmorTexture uvs = uvs(overrides.modelOr(renderer.config().modelId()), overrides.textureOr(renderer.config().texture()));
		return uvs == null ? original : new IGenderArmor()
		{
			@Override
			public IBreastArmorTexture texture()
			{
				return uvs;
			}
		};
	}

	private static synchronized IBreastArmorTexture uvs(ResourceLocation modelId, ResourceLocation texture)
	{
		if (generation != GeoModelCache.generation())
		{
			UVS.clear();
			generation = GeoModelCache.generation();
		}
		return UVS.computeIfAbsent(modelId + "|" + texture, key -> load(modelId, texture)).orElse(null);
	}

	private static Optional<IBreastArmorTexture> load(ResourceLocation modelId, ResourceLocation textureId)
	{
		final ResourceManager resources = Minecraft.getInstance().getResourceManager();
		final Optional<Resource> resource = resources.getResource(modelId);
		if (resource.isEmpty())
		{
			return Optional.empty();
		}
		try (Reader reader = resource.get().openAsReader())
		{
			final GeoModel model = GeoParser.parse(JsonParser.parseReader(reader).getAsJsonObject(), modelId.toString());
			final Optional<Resource> png = resources.getResource(textureId);
			if (png.isEmpty())
			{
				return Optional.ofNullable(fromModel(model, null));
			}
			try (InputStream in = png.get().open(); NativeImage texture = NativeImage.read(in))
			{
				return Optional.ofNullable(fromModel(model, texture));
			}
		}
		catch (Exception e)
		{
			ForeverFixes.LOGGER.warn("Breast armor UVs for {} are not available ({}); the default ones are used", modelId, e.toString());
			return Optional.empty();
		}
	}

	/**
	 * A breast box (box UV, front face {@code dimensions} at UV + 4, 4) samples the chest the way the defaults do on the vanilla
	 * body (front face at 20, 20; breast fronts side by side from its centre, one row below its top). The cube sampled is the
	 * frontmost {@code armorBody} cube in front of the breasts whose texture there is mostly opaque, so a breastplate drawn over the
	 * torso wins and a sparse overlay does not; a cube narrower than both breasts (a robe's centre panel) gives both its middle.
	 */
	private static IBreastArmorTexture fromModel(GeoModel model, NativeImage texture)
	{
		final List<GeoModel.GeoCube> cubes = new ArrayList<>();
		for (GeoModel.GeoBone bone : model.bones())
		{
			if ("armorBody".equals(bone.name()))
			{
				for (GeoModel.GeoCube cube : bone.cubes())
				{
					final float[] origin = cube.origin();
					final float[] size = cube.size();
					if (size[0] >= 4 && origin[0] < 0 && origin[0] + size[0] > 0 && origin[1] <= BREAST_BOTTOM_Y && origin[1] + size[1] >= BREAST_TOP_Y)
					{
						cubes.add(cube);
					}
				}
			}
		}
		cubes.sort(Comparator.comparingDouble(cube -> cube.origin()[2] - cube.inflate()));
		final Vector2i size = new Vector2i(model.textureWidth(), model.textureHeight());
		final Vector2i dimensions = new Vector2i(IBreastArmorTexture.DEFAULT_DIMENSIONS);
		BreastArmorTexture fallback = null;
		for (GeoModel.GeoCube cube : cubes)
		{
			final float depth = cube.size()[2];
			final int centreU = Math.round(cube.uv()[0] + depth + cube.size()[0] / 2);
			final int topV = Math.round(cube.uv()[1] + depth) + 1 + Math.max(0, Math.round(cube.origin()[1] + cube.size()[1] - BODY_TOP_Y));
			final boolean narrow = cube.size()[0] < 2 * dimensions.x();
			final Vector2i left = new Vector2i(centreU - (narrow ? dimensions.x() / 2 : dimensions.x()) - 4, topV - 4);
			final Vector2i right = narrow ? new Vector2i(left) : new Vector2i(left.x() + dimensions.x(), left.y());
			final BreastArmorTexture uvs = new BreastArmorTexture(size, left, right, dimensions);
			fallback = uvs;
			if (texture == null || opaque(texture, size, left.x() + 4, topV, narrow ? dimensions.x() : 2 * dimensions.x(), dimensions.y()))
			{
				return uvs;
			}
		}
		return fallback;
	}

	/** Whether at least half of the given region (in model texture pixels) of the texture is opaque. */
	private static boolean opaque(NativeImage texture, Vector2i size, int u, int v, int width, int height)
	{
		final float scaleX = texture.getWidth() / (float) size.x();
		final float scaleY = texture.getHeight() / (float) size.y();
		int solid = 0;
		int total = 0;
		for (int y = v; y < v + height; y++)
		{
			for (int x = u; x < u + width; x++)
			{
				final int px = (int) (x * scaleX);
				final int py = (int) (y * scaleY);
				if (px < 0 || py < 0 || px >= texture.getWidth() || py >= texture.getHeight())
				{
					continue;
				}
				total++;
				if ((texture.getPixelRGBA(px, py) >>> 24) >= 128)
				{
					solid++;
				}
			}
		}
		return total > 0 && solid * 2 >= total;
	}
}
