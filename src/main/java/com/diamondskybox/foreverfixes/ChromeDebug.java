package com.diamondskybox.foreverfixes;

import com.chromeware.client.cyberware.ArmFirstPerson;
import com.chromeware.client.mantis.MantisAnimation;
import com.diamondskybox.foreverfixes.mixin.mobends.AccessorBipedMutatorArms;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import goblinbob.mobends.api.animation.MoBendsAnimationControl;
import goblinbob.mobends.compat.PlayerAnimationLibCompat;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * {@code /ffchrome debug} prints every piece of state that can leave an arm stuck after Chromeware's first-person implant view
 * (Chromeware flags, Mo' Bends' deferral and part flags, the vanilla and MCA model parts, the Player Animation Lib stack);
 * {@code /ffchrome fixarms} resets those groups and reports which were stale; {@code /ffchrome rebuild} re-measures the Mo' Bends player model. Diagnostics for the Chromeware x Mo' Bends work.
 */
public final class ChromeDebug
{
	private static final Logger LOGGER = LoggerFactory.getLogger("ForeverFixes/ChromeDebug");

	private ChromeDebug()
	{
	}

	public static void register()
	{
		NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> event.getDispatcher().register(Commands.literal("ffchrome")
				.then(Commands.literal("debug").executes(c -> report(c.getSource(), false)))
				.then(Commands.literal("fixarms").executes(c -> report(c.getSource(), true)))
				.then(Commands.literal("rebuild").executes(c -> rebuild(c.getSource())))));
	}

	private static int report(CommandSourceStack source, boolean fix)
	{
		final Minecraft mc = Minecraft.getInstance();
		final AbstractClientPlayer player = mc.player;
		if (player == null)
		{
			return 0;
		}
		final List<String> lines = new ArrayList<>();
		final List<String> fixed = new ArrayList<>();
		lines.add("camera " + mc.options.getCameraType() + ", firstPersonPass " + FirstPersonMode.isFirstPersonPass() + ", ArmFirstPerson.applies "
				+ ArmFirstPerson.applies(player) + ", hardwareRendering " + ArmFirstPerson.hardwareRendering(player));
		final float tick = mc.getTimer().getGameTimeDeltaPartialTick(true);
		lines.add("mantis rendering R/L " + MantisAnimation.rendering(player, HumanoidArm.RIGHT) + "/" + MantisAnimation.rendering(player, HumanoidArm.LEFT)
				+ ", drawWeight R/L " + f(MantisAnimation.drawWeight(player, HumanoidArm.RIGHT, tick)) + "/" + f(MantisAnimation.drawWeight(player, HumanoidArm.LEFT, tick)));
		lines.add("mobends deferred " + MoBendsAnimationControl.isAnimationDeferred(player) + ", poseOverridden " + MoBendsAnimationControl.isPoseOverridden(player)
				+ ", externalAnimation " + MoBendsAnimationControl.hasExternalAnimation(player) + ", staticallyPosed " + MoBendsAnimationControl.isStaticallyPosed(player));
		lines.add("PAL: hasActiveAnimation " + PlayerAnimationLibCompat.hasActiveAnimation(player) + ", isPosingBones " + PlayerAnimationLibCompat.isPosingBones(player)
				+ ", isAnimatingVanillaModel " + PlayerAnimationLibCompat.isAnimatingVanillaModel(player));

		// Mo' Bends data (what its animations and packs produced)
		final Object data = EntityDatabase.instance.get((LivingEntity) player);
		if (data instanceof BipedEntityData<?> biped)
		{
			lines.add("mobends data: externalPoseAdopted " + biped.externalPoseAdopted
					+ " | rightArm " + q(biped.rightArm) + " | leftArm " + q(biped.leftArm) + " | rightForeArm " + q(biped.rightForeArm) + " | leftForeArm " + q(biped.leftForeArm));
			if (fix && biped.externalPoseAdopted)
			{
				biped.externalPoseAdopted = false;
				fixed.add("mobends data externalPoseAdopted");
			}
		}
		else
		{
			lines.add("mobends data: none for the player");
		}

		// Mo' Bends mutator parts
		final EntityRenderer<?> renderer = mc.getEntityRenderDispatcher().getRenderer(player);
		if (renderer instanceof LivingEntityRenderer<?, ?> living)
		{
			final EntityBender<AbstractClientPlayer> bender = EntityBenderRegistry.instance.getForEntity(player);
			final Object mutator = bender == null ? null : bender.getMutator(living);
			if (mutator instanceof AccessorBipedMutatorArms arms)
			{
				final String[] names = {"rightArm", "leftArm", "rightForeArm", "leftForeArm", "outerRightArm", "outerLeftArm", "outerRightForeArm", "outerLeftForeArm"};
				final BendsModelPart[] parts = {arms.foreverfixes$rightArm(), arms.foreverfixes$leftArm(), arms.foreverfixes$rightForeArm(), arms.foreverfixes$leftForeArm(),
						arms.foreverfixes$outerRightArm(), arms.foreverfixes$outerLeftArm(), arms.foreverfixes$outerRightForeArm(), arms.foreverfixes$outerLeftForeArm()};
				final StringBuilder b = new StringBuilder("mobends parts (visible/hidden/concealed):");
				for (int i = 0; i < names.length; i++)
				{
					final BendsModelPart p = parts[i];
					if (p == null)
					{
						continue;
					}
					b.append(' ').append(names[i]).append('=').append(p.visible ? 'V' : 'v').append(p.hidden ? 'H' : 'h').append(p.concealed ? 'C' : 'c');
					if (fix && (!p.visible || p.hidden || p.concealed))
					{
						p.visible = true;
						p.hidden = false;
						p.concealed = false;
						fixed.add("mobends part " + names[i]);
					}
				}
				lines.add(b.toString());
				lines.add("mobends pivots: resolved " + arms.foreverfixes$adaptivePivotsResolved() + ", restStored " + arms.foreverfixes$vanillaPositionsStored()
						+ ", rest R/L " + px(arms.foreverfixes$vanillaRightArmPos()) + "/" + px(arms.foreverfixes$vanillaLeftArmPos())
						+ " | rightArm " + pos(arms.foreverfixes$rightArm()) + " leftArm " + pos(arms.foreverfixes$leftArm())
						+ " rightForeArm " + pos(arms.foreverfixes$rightForeArm()) + " leftForeArm " + pos(arms.foreverfixes$leftForeArm()));
			}
			else
			{
				lines.add("mobends mutator: " + (mutator == null ? "none" : mutator.getClass().getSimpleName()));
			}
			// vanilla player model
			if (living.getModel() instanceof PlayerModel<?> model)
			{
				lines.add("vanilla model: rightArm " + part(model.rightArm) + ", leftArm " + part(model.leftArm) + ", rightSleeve vis " + model.rightSleeve.visible + ", leftSleeve vis " + model.leftSleeve.visible);
				if (fix && (!model.rightArm.visible || !model.leftArm.visible || !model.rightSleeve.visible || !model.leftSleeve.visible))
				{
					model.setAllVisible(true);
					fixed.add("vanilla model visibility");
				}
			}
		}
		// MCA layer models
		try
		{
			int i = 0;
			for (HumanoidModel<?> layer : McaArms.layerModels(player))
			{
				lines.add("mca layer model " + i + ": rightArm " + part(layer.rightArm) + ", leftArm " + part(layer.leftArm) + ", body vis " + layer.body.visible + ", head vis " + layer.head.visible);
				if (fix && (!layer.rightArm.visible || !layer.leftArm.visible || !layer.body.visible || !layer.head.visible))
				{
					layer.setAllVisible(true);
					fixed.add("mca layer model " + i + " visibility");
				}
				i++;
			}
		}
		catch (RuntimeException e)
		{
			lines.add("mca layer models: " + e);
		}
		if (fix)
		{
			lines.add(fixed.isEmpty() ? "fixarms: nothing was stale in these groups" : "fixarms reset: " + String.join(", ", fixed));
		}
		final String text = String.join("\n", lines);
		LOGGER.info("[ffchrome {}]\n{}", fix ? "fixarms" : "debug", text);
		source.sendSuccess(() -> Component.literal(text).withStyle(ChatFormatting.GRAY), false);
		source.sendSuccess(() -> Component.literal("(also written to latest.log as [ffchrome " + (fix ? "fixarms" : "debug") + "])").withStyle(ChatFormatting.DARK_GRAY), false);
		return 1;
	}

	/** Throws away Mo' Bends' player model (and its cached geometry) and measures it again, outside any render scope. */
	private static int rebuild(CommandSourceStack source)
	{
		final AbstractClientPlayer player = Minecraft.getInstance().player;
		if (player == null)
		{
			return 0;
		}
		final EntityBender<AbstractClientPlayer> bender = EntityBenderRegistry.instance.getForEntity(player);
		if (bender == null)
		{
			source.sendFailure(Component.literal("Mo' Bends has no bender for the player"));
			return 0;
		}
		bender.refreshMutation();
		source.sendSuccess(() -> Component.literal("Mo' Bends player model rebuilt and re-measured").withStyle(ChatFormatting.GRAY), false);
		return 1;
	}

	private static String part(ModelPart p)
	{
		return String.format(Locale.ROOT, "vis %s pos %.1f/%.1f/%.1f rot %.0f/%.0f/%.0f", p.visible, p.x, p.y, p.z, Math.toDegrees(p.xRot), Math.toDegrees(p.yRot), Math.toDegrees(p.zRot));
	}

	private static String pos(BendsModelPart p)
	{
		return p == null ? "-" : String.format(Locale.ROOT, "(%.1f %.1f %.1f)", p.position.x, p.position.y, p.position.z);
	}

	private static String px(float[] v)
	{
		return v == null || v.length < 3 ? "-" : String.format(Locale.ROOT, "(%.1f %.1f %.1f)", v[0], v[1], v[2]);
	}

	private static String q(ModelPartTransform t)
	{
		final Quaternion s = t.rotation.getSmooth();
		return String.format(Locale.ROOT, "q(%.2f %.2f %.2f %.2f) off(%.1f %.1f %.1f)", s.x, s.y, s.z, s.w, t.offset.x, t.offset.y, t.offset.z);
	}

	private static String f(float v)
	{
		return String.format(Locale.ROOT, "%.2f", v);
	}
}
