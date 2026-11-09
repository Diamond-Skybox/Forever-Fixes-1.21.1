package com.diamondskybox.foreverfixes;

import com.chromeware.client.mantis.MantisAnimation;
import com.chromeware.client.sandevistan.SandevistanClient;
import com.chromeware.cyberware.ArmHardware;
import com.chromeware.mantis.MantisImplant;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Chromeware states as Mo' Bends pack trigger conditions, so a Bends pack's animator can switch nodes on them:
 * {@code {"type": "foreverfixes:mantis_deployed"}}, {@code {"type": "foreverfixes:mantis_drawing"}} (a blade is still
 * extending, the first 16 ticks after deploying), {@code foreverfixes:mantis_striking_right} / {@code _left} (that arm's blade is
 * mid-swing, the 10 ticks of a slash) and {@code {"type": "foreverfixes:sandevistan_active"}}. Wrap in
 * {@code core:not} / {@code core:and} like any other condition. Registered before Mo' Bends loads packs (mod construction).
 */
public final class BendsConditions
{
	private BendsConditions()
	{
	}

	public static void register()
	{
		TriggerConditionRegistry.instance.register("foreverfixes:mantis_deployed", context -> player(context) instanceof Player p && MantisImplant.deployed(p));
		TriggerConditionRegistry.instance.register("foreverfixes:mantis_drawing", context -> player(context) instanceof AbstractClientPlayer p && drawing(p));
		TriggerConditionRegistry.instance.register("foreverfixes:mantis_striking_right", context -> player(context) instanceof Player p && ArmHardware.striking(p, HumanoidArm.RIGHT));
		TriggerConditionRegistry.instance.register("foreverfixes:mantis_striking_left", context -> player(context) instanceof Player p && ArmHardware.striking(p, HumanoidArm.LEFT));
		TriggerConditionRegistry.instance.register("foreverfixes:sandevistan_active", context -> entity(context) instanceof LivingEntity e && SandevistanClient.isActive(e));
	}

	/** A blade is still extending (Chromeware's 16-tick deploy, draw weight below 1 on a deployed arm). */
	private static boolean drawing(AbstractClientPlayer p)
	{
		for (HumanoidArm side : HumanoidArm.values())
		{
			if (MantisImplant.deployed(p, side) && MantisAnimation.drawWeight(p, side, 0.0F) < 1.0F)
			{
				return true;
			}
		}
		return false;
	}

	private static LivingEntity entity(ITriggerConditionContext context)
	{
		return context.getEntityData() instanceof EntityData<?> data && data.getEntity() instanceof LivingEntity living ? living : null;
	}

	private static Player player(ITriggerConditionContext context)
	{
		return entity(context) instanceof Player p ? p : null;
	}
}
