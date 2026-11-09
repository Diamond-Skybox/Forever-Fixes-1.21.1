package com.diamondskybox.foreverfixes;

import com.chromeware.body.BodyPart;
import com.chromeware.body.CyberLimbs;
import com.chromeware.cyberware.ImplantColors;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.cyberware.SandevistanImplant;
import com.chromeware.sandevistan.Sandevistan;
import com.chromeware.sandevistan.SandevistanContraptions;
import com.chromeware.sandevistan.SandevistanPackets;
import com.chromeware.sandevistan.TimeField;
import com.diamondskybox.foreverfixes.mixin.chromewaredoppel.AccessorSandevistanFields;
import net.bandit.darkdoppelganger.entity.DarkDoppelgangerEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Dark Doppelganger x Chromeware. The boss copies its summoner's gear and spell power; this makes it copy their cyber limbs and Shell
 * colours too (Chromeware's own synced attachments, which the boss renderer layer draws), drops Forever Fixes' cyberware magic
 * penalties from the spell power it copied, and gives it a Sandevistan of its own when the summoner has one.
 * <p>
 * The boss's Sandevistan is a Chromeware time field owned by the boss, so Chromeware slows everything inside it except the boss (an
 * owner is immune), draws the field walls and plays the sounds. Chromeware only manages fields of players, so this class starts,
 * moves and ends the boss's field itself.
 */
public final class DoppelChrome
{
	private static final String SANDEVISTAN_TAG = "foreverfixes:doppel_sandevistan";
	private static final ResourceLocation SPEED_ID = ResourceLocation.fromNamespaceAndPath("foreverfixes", "doppel_sandevistan_speed");
	/** Same speed boost Chromeware gives a player while their Sandevistan is on. */
	private static final double SPEED_BONUS = 0.5D;
	/** boss -> {ticks left on, cooldown ticks left} */
	private static final Map<UUID, int[]> STATE = new HashMap<>();

	private DoppelChrome()
	{
	}

	private static boolean on()
	{
		return ForeverFixesCommonConfig.SPEC.isLoaded() && ForeverFixesCommonConfig.DOPPEL_CHROME.get();
	}

	public static void onJoin(EntityJoinLevelEvent event)
	{
		if (event.getLevel().isClientSide() || !(event.getEntity() instanceof DarkDoppelgangerEntity boss) || !on())
		{
			return;
		}
		stripChromeMagic(boss);
		if (event.loadedFromDisk())
		{
			return;
		}
		final ServerPlayer summoner = boss.getSummonerPlayer();
		if (summoner == null)
		{
			return;
		}
		final CyberLimbs frames = CyberLimbs.get(summoner);
		if (frames.installed() != 0)
		{
			boss.setData(CyberLimbs.FRAMES, frames);
		}
		final ImplantColors colors = summoner.getData(PlayerData.IMPLANT_COLORS);
		if (!colors.colors().isEmpty())
		{
			boss.setData(PlayerData.IMPLANT_COLORS, colors);
		}
		if (!boss.isClone && SandevistanImplant.active(summoner))
		{
			boss.getPersistentData().putBoolean(SANDEVISTAN_TAG, true);
		}
	}

	/** Removes the per-part cyberware penalties (and the shield's) that came along when the boss copied the summoner's spell power. */
	private static void stripChromeMagic(LivingEntity boss)
	{
		for (Holder<Attribute> attribute : BuiltInRegistries.ATTRIBUTE.holders().toList())
		{
			if (!boss.getAttributes().hasAttribute(attribute))
			{
				continue;
			}
			final AttributeInstance instance = boss.getAttribute(attribute);
			if (instance == null)
			{
				continue;
			}
			for (AttributeModifier modifier : new ArrayList<>(instance.getModifiers()))
			{
				final ResourceLocation id = modifier.id();
				if ("foreverfixes".equals(id.getNamespace()) && (id.getPath().startsWith("chrome_") || id.getPath().startsWith("shield_")))
				{
					instance.removeModifier(id);
				}
			}
		}
	}

	public static void onTick(EntityTickEvent.Post event)
	{
		if (!(event.getEntity() instanceof DarkDoppelgangerEntity boss) || !(boss.level() instanceof ServerLevel level)
				|| !boss.getPersistentData().getBoolean(SANDEVISTAN_TAG))
		{
			return;
		}
		final UUID id = boss.getUUID();
		final Map<UUID, TimeField> fields = AccessorSandevistanFields.foreverfixes$fields();
		final TimeField field = fields.get(id);
		final int[] state = STATE.computeIfAbsent(id, k -> new int[] {0, ForeverFixesCommonConfig.DOPPEL_SANDY_COOLDOWN.get()});
		final boolean able = on() && boss.isAlive() && !boss.isDeadOrDying() && !boss.isClone;
		if (field != null)
		{
			if (!able || --state[0] <= 0 || !field.dimension().equals(level.dimension()))
			{
				end(boss, level, state);
				return;
			}
			fields.put(id, centered(boss, field));
			return;
		}
		if (!able)
		{
			return;
		}
		if (state[1] > 0)
		{
			state[1]--;
			return;
		}
		final LivingEntity target = boss.getTarget();
		final double range = ForeverFixesCommonConfig.DOPPEL_SANDY_RANGE.get();
		if (target == null || !target.isAlive() || target.level() != level || boss.distanceToSqr(target) > range * range)
		{
			return;
		}
		start(boss, level, state);
	}

	private static void start(DarkDoppelgangerEntity boss, ServerLevel level, int[] state)
	{
		final Vector3d pos = SandevistanContraptions.worldPosition(boss);
		AccessorSandevistanFields.foreverfixes$fields().put(boss.getUUID(), new TimeField(boss.getUUID(), level.dimension(), pos.x,
				pos.y + boss.getBbHeight() * 0.5D, pos.z, ForeverFixesCommonConfig.DOPPEL_SANDY_RADIUS.get(),
				ForeverFixesCommonConfig.DOPPEL_SANDY_SLOWDOWN.get(), Sandevistan.serverTicks()));
		state[0] = ForeverFixesCommonConfig.DOPPEL_SANDY_DURATION.get();
		final AttributeInstance speed = boss.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed != null)
		{
			speed.addOrUpdateTransientModifier(new AttributeModifier(SPEED_ID, SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
		}
		SandevistanContraptions.refresh(level);
		SandevistanPackets.broadcast(level);
	}

	private static void end(DarkDoppelgangerEntity boss, ServerLevel level, int[] state)
	{
		AccessorSandevistanFields.foreverfixes$fields().remove(boss.getUUID());
		state[0] = 0;
		state[1] = ForeverFixesCommonConfig.DOPPEL_SANDY_COOLDOWN.get();
		final AttributeInstance speed = boss.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed != null)
		{
			speed.removeModifier(SPEED_ID);
		}
		SandevistanContraptions.refresh(level);
		SandevistanPackets.broadcast(level);
	}

	private static TimeField centered(Entity boss, TimeField old)
	{
		final Vector3d pos = SandevistanContraptions.worldPosition(boss);
		return new TimeField(old.owner(), old.dimension(), pos.x, pos.y + boss.getBbHeight() * 0.5D, pos.z, old.radius(), old.slowdown(),
				old.origin(), old.mode());
	}

	public static void onLeave(EntityLeaveLevelEvent event)
	{
		if (!(event.getEntity() instanceof DarkDoppelgangerEntity boss) || !(event.getLevel() instanceof ServerLevel level))
		{
			return;
		}
		STATE.remove(boss.getUUID());
		if (AccessorSandevistanFields.foreverfixes$fields().remove(boss.getUUID()) != null)
		{
			final AttributeInstance speed = boss.getAttribute(Attributes.MOVEMENT_SPEED);
			if (speed != null)
			{
				speed.removeModifier(SPEED_ID);
			}
			SandevistanContraptions.refresh(level);
			SandevistanPackets.broadcast(level);
		}
	}

	/**
	 * One roll for one Clot of Time when the real boss (not a clone or echo) dies after being summoned by a Sandevistan user.
	 * Looting doesn't change the count. This is the death-drops event, so loot-table simulators (Drygmy charms and the like) never
	 * produce it.
	 */
	public static void onDrops(LivingDropsEvent event)
	{
		if (!(event.getEntity() instanceof DarkDoppelgangerEntity boss) || boss.isClone || !on()
				|| !boss.getPersistentData().getBoolean(SANDEVISTAN_TAG) || !(boss.level() instanceof ServerLevel level))
		{
			return;
		}
		final double chance = ForeverFixesCommonConfig.DOPPEL_CLOT_CHANCE.get();
		if (chance <= 0.0D || level.getRandom().nextDouble() >= chance)
		{
			return;
		}
		final ResourceLocation id = ResourceLocation.tryParse(ForeverFixesCommonConfig.DOPPEL_CLOT_ITEM.get());
		if (id == null)
		{
			return;
		}
		BuiltInRegistries.ITEM.getOptional(id).ifPresent(item ->
				event.getDrops().add(new ItemEntity(level, boss.getX(), boss.getY(), boss.getZ(), new ItemStack(item))));
	}

	public static void onServerStopped(ServerStoppedEvent event)
	{
		STATE.clear();
	}

	/** Which model parts of the armour in this slot the boss's copied cyber limbs hide (body, left arm, right arm, left leg, right leg). */
	public static boolean[] hiddenArmor(LivingEntity entity, EquipmentSlot slot)
	{
		final boolean[] hide = new boolean[5];
		if (!(entity instanceof DarkDoppelgangerEntity) || !on() || !ForeverFixesCommonConfig.CYBER_ARMOR_FIT.get()
				|| !entity.hasData(CyberLimbs.FRAMES))
		{
			return hide;
		}
		final CyberLimbs frames = entity.getData(CyberLimbs.FRAMES);
		if (slot == EquipmentSlot.CHEST)
		{
			hide[0] = frames.has(BodyPart.BODY);
			hide[1] = frames.has(BodyPart.LEFT_ARM);
			hide[2] = frames.has(BodyPart.RIGHT_ARM);
		}
		else if (slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET)
		{
			hide[3] = frames.has(BodyPart.LEFT_LEG);
			hide[4] = frames.has(BodyPart.RIGHT_LEG);
		}
		return hide;
	}
}
