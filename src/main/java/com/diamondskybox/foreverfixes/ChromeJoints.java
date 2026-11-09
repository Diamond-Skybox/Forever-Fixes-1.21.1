package com.diamondskybox.foreverfixes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import goblinbob.mobends.lib.math.Quaternion;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * How Chromeware's cyber limb meshes bend at Mo' Bends' elbows and knees, from {@code config/foreverfixes-chrome-joints.json}
 * (written with defaults when missing; {@code /ffchrome reload} re-reads it in game, {@code /ffchrome show} prints it).
 * <p>
 * The joints are Mo' Bends': the forearm turns about its pivot 4 px below the shoulder on the back edge (z +2), the shin about
 * its pivot 6 px below the hip on the front edge (z −2). By default ({@code mode: "cut"}) the chrome mesh is cut at the seam plane
 * through that pivot: every quad is split where it crosses the plane, what lies above the plane stays with the upper bone and
 * what lies below turns with the lower bone, so chrome and flesh share one joint line. {@code mode: "pieces"} keeps whole
 * pieces together and uses the {@code lower} list instead. {@code seam} (px below the upper pivot) and {@code hinge} (px from the
 * upper pivot, x y z, Java model space: x = entity's left, y down, z back, right limb) can be moved for experiments.
 * {@code pieces} holds per-piece tweaks: {@code hideAbove} / {@code hideBelow} (bend angle in degrees), {@code offset} (pixels),
 * {@code rotate} (degrees x y z about the piece's centre).
 */
public final class ChromeJoints
{
	private static final Logger LOGGER = LoggerFactory.getLogger("ForeverFixes/ChromeJoints");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Set<String> DEFAULT_LOWER_ARM = Set.of("forearm_core", "forearm_front_shell", "outer_forearm_cheek", "inner_forearm_cheek",
			"actuator_cylinder", "actuator_rod", "wrist_band", "palm", "backhand_plate", "forearm_rear_shell_front", "forearm_rear_shell_back");
	private static final Set<String> DEFAULT_LOWER_LEG = Set.of("knee_axle", "knee_cap", "outer_knee_cap", "shin_core", "shin_front_shell",
			"shin_rear_shell", "calf_actuator", "calf_rod", "ankle_band", "foot_block", "heel_plate");

	public record Rule(float hideAbove, float hideBelow, Vector3f offset, Vector3f rotate)
	{
		boolean hidden(float bendDegrees)
		{
			return bendDegrees > hideAbove || bendDegrees < hideBelow;
		}

		boolean moves()
		{
			return offset.lengthSquared() > 0.0F || rotate.lengthSquared() > 0.0F;
		}
	}

	/** hinge/seam in pixels from the limb's upper pivot; cut = split quads at the seam plane, else whole pieces by the lower list. */
	public record Limb(boolean cut, float seam, Vector3f hinge, Set<String> lower, Map<String, Rule> pieces)
	{
		public Rule rule(String piece)
		{
			return pieces.get(piece);
		}
	}

	private static volatile Map<String, Limb> limbs = defaults();
	private static String lastError;

	private ChromeJoints()
	{
	}

	public static void register()
	{
		NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> event.getDispatcher().register(Commands.literal("ffchrome")
				.then(Commands.literal("reload").executes(c -> {
					load();
					ChromeLimbMesh.invalidate();
					c.getSource().sendSuccess(() -> Component.literal(lastError == null ? "Chrome joints reloaded from " + file()
							: "Chrome joints: " + lastError).withStyle(lastError == null ? ChatFormatting.GREEN : ChatFormatting.RED), false);
					return lastError == null ? 1 : 0;
				}))
				.then(Commands.literal("show").executes(c -> {
					final StringBuilder text = new StringBuilder(file().toString());
					for (Map.Entry<String, Limb> e : limbs.entrySet())
					{
						final Limb limb = e.getValue();
						text.append("\n").append(e.getKey()).append(": ").append(limb.cut() ? "cut at seam " + limb.seam() + " px" : "whole pieces")
								.append("; hinge ").append(limb.hinge().x).append(", ").append(limb.hinge().y).append(", ").append(limb.hinge().z).append(" px");
						if (!limb.cut())
						{
							text.append("; lower: ").append(String.join(", ", new ArrayList<>(limb.lower()).stream().sorted().toList()));
						}
						for (Map.Entry<String, Rule> r : limb.pieces().entrySet())
						{
							text.append("\n  ").append(r.getKey()).append(": ").append(r.getValue());
						}
					}
					c.getSource().sendSuccess(() -> Component.literal(text.toString()), false);
					return 1;
				}))));
		load();
	}

	public static Limb limb(boolean arm)
	{
		return limbs.get(arm ? "arm" : "leg");
	}

	/** Bend angle of a joint in degrees, from Mo' Bends' smoothed orientation. */
	public static float bendDegrees(Quaternion q)
	{
		final float w = Math.min(1.0F, Math.abs(q.w) / Math.max(1.0E-6F, q.length()));
		return (float) Math.toDegrees(2.0 * Math.acos(w));
	}

	/** Extra per-piece matrix in the limb mesh's own space (blocks), or null. Offsets are in pixels; rotations are about the piece's centre. */
	public static Matrix4f pieceMatrix(boolean arm, String piece)
	{
		final Limb limb = limb(arm);
		final Rule rule = limb == null ? null : limb.rule(piece);
		if (rule == null || !rule.moves())
		{
			return null;
		}
		final Matrix4f m = new Matrix4f().translate(rule.offset().x / 16.0F, rule.offset().y / 16.0F, rule.offset().z / 16.0F);
		if (rule.rotate().lengthSquared() > 0.0F)
		{
			final Vector3f c = ChromeLimbMesh.centre(arm ? "arm" : "leg", piece);
			m.translate(c).rotateXYZ((float) Math.toRadians(rule.rotate().x), (float) Math.toRadians(rule.rotate().y), (float) Math.toRadians(rule.rotate().z)).translate(-c.x, -c.y, -c.z);
		}
		return m;
	}

	private static Path file()
	{
		return FMLPaths.CONFIGDIR.get().resolve("foreverfixes-chrome-joints.json");
	}

	/** Mo' Bends' own joints: forearm pivot (0, 4, 2), foreleg pivot (0, 6, -2), seam planes through them. */
	private static Map<String, Limb> defaults()
	{
		final Map<String, Limb> map = new LinkedHashMap<>();
		map.put("arm", new Limb(true, 4.0F, new Vector3f(0.0F, 4.0F, 2.0F), DEFAULT_LOWER_ARM, Map.of()));
		map.put("leg", new Limb(true, 6.0F, new Vector3f(0.0F, 6.0F, -2.0F), DEFAULT_LOWER_LEG, Map.of()));
		return map;
	}

	public static synchronized void load()
	{
		lastError = null;
		final Path path = file();
		try
		{
			if (!Files.exists(path))
			{
				Files.writeString(path, GSON.toJson(toJson(defaults())));
			}
			final JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
			final Map<String, Limb> parsed = new LinkedHashMap<>();
			for (String key : List.of("arm", "leg"))
			{
				final Limb fallback = defaults().get(key);
				if (!root.has(key))
				{
					parsed.put(key, fallback);
					continue;
				}
				final JsonObject o = root.getAsJsonObject(key);
				final boolean cut = !o.has("mode") || !"pieces".equalsIgnoreCase(o.get("mode").getAsString());
				final float seam = o.has("seam") ? o.get("seam").getAsFloat() : fallback.seam();
				final Vector3f hinge = o.has("hinge") ? vec(o.getAsJsonArray("hinge")) : fallback.hinge();
				final Set<String> lower = new HashSet<>();
				if (o.has("lower"))
				{
					for (JsonElement e : o.getAsJsonArray("lower"))
					{
						lower.add(e.getAsString());
					}
				}
				else
				{
					lower.addAll(fallback.lower());
				}
				final Map<String, Rule> rules = new HashMap<>();
				if (o.has("pieces"))
				{
					for (Map.Entry<String, JsonElement> e : o.getAsJsonObject("pieces").entrySet())
					{
						if (e.getKey().startsWith("_"))
						{
							continue;
						}
						final JsonObject r = e.getValue().getAsJsonObject();
						rules.put(e.getKey(), new Rule(
								r.has("hideAbove") ? r.get("hideAbove").getAsFloat() : Float.MAX_VALUE,
								r.has("hideBelow") ? r.get("hideBelow").getAsFloat() : -Float.MAX_VALUE,
								r.has("offset") ? vec(r.getAsJsonArray("offset")) : new Vector3f(),
								r.has("rotate") ? vec(r.getAsJsonArray("rotate")) : new Vector3f()));
					}
				}
				parsed.put(key, new Limb(cut, seam, hinge, Set.copyOf(lower), Map.copyOf(rules)));
			}
			limbs = parsed;
		}
		catch (IOException | RuntimeException e)
		{
			lastError = path.getFileName() + ": " + e.getMessage();
			LOGGER.error("Chrome joints config not loaded, keeping the previous values", e);
		}
	}

	private static Vector3f vec(JsonArray a)
	{
		return new Vector3f(a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat());
	}

	private static JsonObject toJson(Map<String, Limb> map)
	{
		final JsonObject root = new JsonObject();
		root.addProperty("_help", "Chromeware cyber limbs on Mo' Bends. Per limb: mode \"cut\" splits the chrome at the seam plane (seam = px below the "
				+ "upper pivot; defaults are Mo' Bends' joints, elbow 4, knee 6) so what's above stays with the upper bone and what's below turns "
				+ "with the lower; mode \"pieces\" keeps whole pieces and uses the lower list. hinge = point the lower part turns about, px from the "
				+ "upper pivot (right limb; x = entity's left, y down, z back); defaults are Mo' Bends' pivots. pieces = per-piece tweaks "
				+ "{hideAbove, hideBelow (bend degrees), offset [px], rotate [deg, about the piece's centre]}. /ffchrome reload applies edits in game.");
		for (Map.Entry<String, Limb> e : map.entrySet())
		{
			final Limb limb = e.getValue();
			final JsonObject o = new JsonObject();
			o.addProperty("mode", limb.cut() ? "cut" : "pieces");
			o.addProperty("seam", limb.seam());
			final JsonArray hinge = new JsonArray();
			hinge.add(limb.hinge().x);
			hinge.add(limb.hinge().y);
			hinge.add(limb.hinge().z);
			o.add("hinge", hinge);
			final JsonArray lower = new JsonArray();
			limb.lower().stream().sorted().forEach(lower::add);
			o.add("lower", lower);
			final JsonObject pieces = new JsonObject();
			final JsonObject example = new JsonObject();
			example.addProperty("hideAbove", 180);
			final JsonArray zero = new JsonArray();
			zero.add(0);
			zero.add(0);
			zero.add(0);
			example.add("offset", zero);
			example.add("rotate", zero.deepCopy());
			pieces.add("_example_piece_name", example);
			o.add("pieces", pieces);
			root.add(e.getKey(), o);
		}
		return root;
	}
}
