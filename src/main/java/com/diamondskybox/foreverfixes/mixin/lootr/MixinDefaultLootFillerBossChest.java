package com.diamondskybox.foreverfixes.mixin.lootr;

import com.diamondskybox.foreverfixes.BossChests;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import noobanidus.mods.lootr.common.api.data.DefaultLootFiller;
import noobanidus.mods.lootr.common.api.data.ILootrInfoProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A boss chest fills each player's Lootr inventory from the roll made for them when the boss died. */
@Mixin(value = DefaultLootFiller.class, remap = false)
public abstract class MixinDefaultLootFillerBossChest
{
	@Inject(method = "unpackLootTable", remap = false, require = 1, at = @At("HEAD"), cancellable = true)
	private void foreverfixes$bossRoll(ILootrInfoProvider provider, Player player, Container inventory, CallbackInfo ci)
	{
		if (BossChests.fill(provider.getInfoLevel(), provider.getInfoPos(), player, inventory))
		{
			ci.cancel();
		}
	}
}
