package com.diamondskybox.foreverfixes;

import com.diamondskybox.foreverfixes.mixin.mobends.AccessorEntityDataParts;
import com.mojang.brigadier.arguments.StringArgumentType;
import goblinbob.mobends.lib.client.model.IAnimatedPart;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.lib.math.vector.IVec3f;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.lib.math.SmoothOrientation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Records Mo' Bends' live pose of an entity, one keyframe per client tick, into a .bendsanim clip (the format Bends packs
 * and the Blockbench plugin use). Mo' Bends' own player animations are Java code, not files, so this is the way to get them
 * as editable clips: {@code /bendsrec start <name> [self|look]}, do the thing, {@code /bendsrec stop}. Files land in
 * {@code <game dir>/bendspacks/recorded/}.
 * <p>
 * Bones follow Mo' Bends' part names. A part's keyframe rotation is its smoothed orientation; its keyframe position is the
 * negated offset (Mo' Bends applies keyframe positions as -P). "root" carries the global offset, "centerRotation" and
 * "renderRotation" the whole-model rotations.
 */
public final class BendsRecorder
{
	private static final int MAX_FRAMES = 20 * 60;
	private static final List<String> SKIPPED = List.of("renderRightItemRotation", "renderLeftItemRotation");

	private static Entity target;
	private static String name;
	private static LinkedHashMap<String, List<float[]>> bones;
	private static int frames;

	private BendsRecorder()
	{
	}

	public static void register()
	{
		NeoForge.EVENT_BUS.addListener(BendsRecorder::onRegisterCommands);
		NeoForge.EVENT_BUS.addListener(BendsRecorder::onClientTick);
	}

	private static void onRegisterCommands(RegisterClientCommandsEvent event)
	{
		event.getDispatcher().register(Commands.literal("bendsrec")
				.then(Commands.literal("start")
						.then(Commands.argument("name", StringArgumentType.word())
								.executes(c -> start(c.getSource(), StringArgumentType.getString(c, "name"), false))
								.then(Commands.literal("self").executes(c -> start(c.getSource(), StringArgumentType.getString(c, "name"), false)))
								.then(Commands.literal("look").executes(c -> start(c.getSource(), StringArgumentType.getString(c, "name"), true)))))
				.then(Commands.literal("stop").executes(c -> stop(c.getSource())))
				.then(Commands.literal("status").executes(c -> status(c.getSource()))));
	}

	private static int start(CommandSourceStack source, String clipName, boolean lookedAt)
	{
		final Minecraft mc = Minecraft.getInstance();
		Entity entity = lookedAt ? mc.crosshairPickEntity : mc.player;
		if (lookedAt && !(entity instanceof LivingEntity))
		{
			source.sendFailure(Component.literal("Look at a living entity first."));
			return 0;
		}
		if (entity == null || data((LivingEntity) entity) == null)
		{
			source.sendFailure(Component.literal("Mo' Bends has no animation data for that entity yet (it has to have been rendered)."));
			return 0;
		}
		if (target != null)
		{
			finish(source, "stopped the previous recording");
		}
		target = entity;
		name = clipName.replaceAll("[^A-Za-z0-9_.-]", "_");
		bones = new LinkedHashMap<>();
		frames = 0;
		source.sendSuccess(() -> Component.literal("Recording \"" + name + "\" from " + entity.getName().getString()
				+ " at 20 frames/s. /bendsrec stop to save (auto-stops after 60 s).").withStyle(ChatFormatting.GREEN), false);
		return 1;
	}

	private static int stop(CommandSourceStack source)
	{
		if (target == null)
		{
			source.sendFailure(Component.literal("Not recording."));
			return 0;
		}
		finish(source, null);
		return 1;
	}

	private static int status(CommandSourceStack source)
	{
		source.sendSuccess(() -> Component.literal(target == null ? "Not recording." : "Recording \"" + name + "\": " + frames + " frames."), false);
		return 1;
	}

	@SuppressWarnings("unchecked")
	private static LivingEntityData<?> data(LivingEntity entity)
	{
		try
		{
			return EntityDatabase.instance.get(entity);
		}
		catch (RuntimeException e)
		{
			return null;
		}
	}

	private static void onClientTick(ClientTickEvent.Post event)
	{
		if (target == null)
		{
			return;
		}
		final Minecraft mc = Minecraft.getInstance();
		final LivingEntityData<?> data = target instanceof LivingEntity living && !living.isRemoved() && mc.level == living.level() ? data(living) : null;
		if (data == null)
		{
			finish(null, "the entity went away");
			return;
		}
		final Map<String, Object> parts = ((AccessorEntityDataParts) data).foreverfixes$parts();
		track("root").add(new float[] {data.globalOffset.getX(), data.globalOffset.getY(), data.globalOffset.getZ(), 0, 0, 0, 1});
		for (Map.Entry<String, Object> entry : parts.entrySet())
		{
			final String part = entry.getKey();
			if (SKIPPED.contains(part))
			{
				continue;
			}
			if (entry.getValue() instanceof IAnimatedPart animated)
			{
				final IVec3f offset = animated.getOffset();
				track(part).add(frame(animated.getRotation(), -offset.getX(), -offset.getY(), -offset.getZ()));
			}
			else if (entry.getValue() instanceof SmoothOrientation orientation)
			{
				track(part).add(frame(orientation, 0, 0, 0));
			}
		}
		frames++;
		if (frames >= MAX_FRAMES)
		{
			finish(null, "60 s reached");
		}
	}

	private static float[] frame(SmoothOrientation orientation, float x, float y, float z)
	{
		final Quaternion q = orientation.getSmooth();
		return new float[] {x, y, z, q.x, q.y, q.z, q.w};
	}

	private static List<float[]> track(String part)
	{
		List<float[]> track = bones.get(part);
		if (track == null)
		{
			track = new ArrayList<>();
			// a bone that appears late gets rest frames for the ticks before it
			for (int i = 0; i < frames; i++)
			{
				track.add(new float[] {0, 0, 0, 0, 0, 0, 1});
			}
			bones.put(part, track);
		}
		return track;
	}

	private static void finish(CommandSourceStack source, String reason)
	{
		final Minecraft mc = Minecraft.getInstance();
		final String why = reason == null ? "" : " (" + reason + ")";
		String message;
		ChatFormatting colour = ChatFormatting.GREEN;
		try
		{
			final File file = write(mc);
			message = "Saved \"" + name + "\": " + frames + " frames, " + bones.size() + " bones" + why + " → " + file.getPath();
		}
		catch (IOException e)
		{
			message = "Couldn't save \"" + name + "\"" + why + ": " + e.getMessage();
			colour = ChatFormatting.RED;
		}
		final String text = message;
		final ChatFormatting c = colour;
		if (source != null)
		{
			source.sendSuccess(() -> Component.literal(text).withStyle(c), false);
		}
		else if (mc.player != null)
		{
			mc.player.displayClientMessage(Component.literal(text).withStyle(c), false);
		}
		target = null;
		name = null;
		bones = null;
		frames = 0;
	}

	private static File write(Minecraft mc) throws IOException
	{
		final File dir = new File(mc.gameDirectory, "bendspacks/recorded");
		if (!dir.isDirectory() && !dir.mkdirs())
		{
			throw new IOException("can't create " + dir);
		}
		final File file = new File(dir, name + ".bendsanim");
		try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file)))
		{
			out.write("BENDSANIM".getBytes(StandardCharsets.UTF_8));
			out.writeInt(2);
			out.writeInt(frames);
			out.writeInt(bones.size());
			for (Map.Entry<String, List<float[]>> bone : bones.entrySet())
			{
				out.write(bone.getKey().getBytes(StandardCharsets.UTF_8));
				out.writeByte(0);
				final List<float[]> track = bone.getValue();
				for (int i = 0; i < frames; i++)
				{
					final float[] f = i < track.size() ? track.get(i) : track.get(track.size() - 1);
					out.writeByte(3);
					out.writeFloat(f[0]);
					out.writeFloat(f[1]);
					out.writeFloat(f[2]);
					out.writeFloat(f[3]);
					out.writeFloat(f[4]);
					out.writeFloat(f[5]);
					out.writeFloat(f[6]);
				}
			}
		}
		return file;
	}
}
