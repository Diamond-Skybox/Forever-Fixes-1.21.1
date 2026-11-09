package com.diamondskybox.foreverfixes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Forever Fixes' own copy of Chromeware's cyber limb mesh (assets/chromeware/models/cyberware/cyber_limbs_mesh.json): quads
 * with their piece name, in mesh units (blocks) relative to the limb's upper pivot, Java model space (y down). Read through
 * the resource manager, so resource packs that replace the mesh are honoured. Used to draw the limbs cut at Mo' Bends' joints.
 */
public final class ChromeLimbMesh
{
	private static final Logger LOGGER = LoggerFactory.getLogger("ForeverFixes/ChromeLimbMesh");
	private static final ResourceLocation MESH = ResourceLocation.fromNamespaceAndPath("chromeware", "models/cyberware/cyber_limbs_mesh.json");

	public record Quad(String piece, Vector3f normal, Vector3f[] positions, float[] uvs)
	{
	}

	private static volatile Map<String, List<Quad>> limbs;
	private static volatile Map<String, Map<String, Vector3f>> centres = Map.of();

	private ChromeLimbMesh()
	{
	}

	public static void invalidate()
	{
		limbs = null;
	}

	/** Quads of "arm", "leg" or "torso", or null when the mesh can't be read. */
	public static List<Quad> quads(String limb)
	{
		Map<String, List<Quad>> map = limbs;
		if (map == null)
		{
			map = load();
			limbs = map;
		}
		return map.get(limb);
	}

	/** Centre of a piece's bounding box, in mesh units. */
	public static Vector3f centre(String limb, String piece)
	{
		if (limbs == null)
		{
			limbs = load();
		}
		return centres.getOrDefault(limb, Map.of()).getOrDefault(piece, new Vector3f());
	}

	private static Map<String, List<Quad>> load()
	{
		final Map<String, List<Quad>> result = new HashMap<>();
		final Map<String, Map<String, Vector3f>> newCentres = new HashMap<>();
		try
		{
			final var resource = Minecraft.getInstance().getResourceManager().getResource(MESH);
			if (resource.isEmpty())
			{
				LOGGER.warn("{} not found; cyber limbs keep Chromeware's own draw", MESH);
				return result;
			}
			try (InputStreamReader reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8))
			{
				final JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
				if (root.get("format").getAsInt() != 1)
				{
					LOGGER.warn("Unsupported cyber limb mesh format {}; cyber limbs keep Chromeware's own draw", root.get("format"));
					return result;
				}
				for (Map.Entry<String, JsonElement> limb : root.getAsJsonObject("limbs").entrySet())
				{
					final List<Quad> quads = new ArrayList<>();
					final Map<String, float[]> bounds = new HashMap<>();
					for (JsonElement element : limb.getValue().getAsJsonArray())
					{
						final JsonObject q = element.getAsJsonObject();
						final String piece = q.has("piece") ? q.get("piece").getAsString() : "";
						final JsonArray n = q.getAsJsonArray("normal");
						final JsonArray vertices = q.getAsJsonArray("vertices");
						final Vector3f[] positions = new Vector3f[vertices.size()];
						final float[] uvs = new float[vertices.size() * 2];
						final float[] b = bounds.computeIfAbsent(piece, k -> new float[] {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE});
						for (int i = 0; i < vertices.size(); i++)
						{
							final JsonArray v = vertices.get(i).getAsJsonArray();
							positions[i] = new Vector3f(v.get(0).getAsFloat(), v.get(1).getAsFloat(), v.get(2).getAsFloat());
							uvs[i * 2] = v.get(3).getAsFloat();
							uvs[i * 2 + 1] = v.get(4).getAsFloat();
							for (int k = 0; k < 3; k++)
							{
								b[k] = Math.min(b[k], positions[i].get(k));
								b[k + 3] = Math.max(b[k + 3], positions[i].get(k));
							}
						}
						quads.add(new Quad(piece, new Vector3f(n.get(0).getAsFloat(), n.get(1).getAsFloat(), n.get(2).getAsFloat()), positions, uvs));
					}
					result.put(limb.getKey(), List.copyOf(quads));
					final Map<String, Vector3f> c = new HashMap<>();
					bounds.forEach((piece, b) -> c.put(piece, new Vector3f((b[0] + b[3]) / 2.0F, (b[1] + b[4]) / 2.0F, (b[2] + b[5]) / 2.0F)));
					newCentres.put(limb.getKey(), c);
				}
			}
			centres = newCentres;
		}
		catch (IOException | RuntimeException e)
		{
			LOGGER.warn("Cyber limb mesh not readable; cyber limbs keep Chromeware's own draw", e);
		}
		return result;
	}
}
