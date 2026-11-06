package com.diamondskybox.foreverfixes;

import de.teamlapen.vampirism.util.Helper;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

/**
 * Vampirism: vampire players count as undead wherever an entity predicate checks the undead or sensitive_to_smite tags (Smite's damage
 * bonus is one). Healing/harming stays as Vampirism has it, so inverted_healing_and_harm is left out.
 */
public final class VampireUndead
{
	private VampireUndead()
	{
	}

	public static boolean matchesAsUndead(EntityTypePredicate predicate, Entity entity)
	{
		if (!(entity instanceof Player player) || !ForeverFixesCommonConfig.VAMPIRE_PLAYERS_UNDEAD.get())
		{
			return false;
		}
		final Optional<TagKey<EntityType<?>>> tag = predicate.types().unwrapKey();
		return tag.isPresent() && (tag.get().equals(EntityTypeTags.UNDEAD) || tag.get().equals(EntityTypeTags.SENSITIVE_TO_SMITE)) && Helper.isVampire(player);
	}
}
