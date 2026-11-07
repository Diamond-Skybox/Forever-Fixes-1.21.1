package com.diamondskybox.foreverfixes.mixin.vanilla;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Gamerules live in a plain TreeMap. Twilight Forest registers its rules on a background worker while other mods register theirs on the
 * mod-loading threads; two puts at once corrupt the map ("Cannot read field 'left'"), a vanilla rule goes missing and the world crashes on
 * its first tick (GameRules.getInt -> NPE). Registration takes a lock.
 */
@Mixin(GameRules.class)
public abstract class MixinGameRulesRegisterLock
{
	@WrapMethod(method = "register")
	private static <T extends GameRules.Value<T>> GameRules.Key<T> foreverfixes$lock(String name, GameRules.Category category, GameRules.Type<T> type,
			Operation<GameRules.Key<T>> original)
	{
		synchronized (GameRules.class)
		{
			return original.call(name, category, type);
		}
	}
}
