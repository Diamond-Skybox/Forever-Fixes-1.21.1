package com.diamondskybox.foreverfixes.mixin.skeletonsrising;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skeletons Rising's Soul Terror counts up "IA" while it has a target and, on reaching 60, dashes at the nearest player in a
 * 64-block box, reading that player's position without checking there is one. With a non-player target (a guard, villager or
 * pet) or the player out of range, that crashed the server ("Ticking entity", NullPointerException at
 * SoulTerrorOnEntityTickUpdateProcedure line 40). When no player is in the box at that moment the counter now skips past 60, so
 * the dash is left out for that cycle; the rest of the cycle (the reset at 120) is unchanged.
 */
@Mixin(targets = "net.mcreator.skeletonsrising.procedures.SoulTerrorOnEntityTickUpdateProcedure", remap = false)
public abstract class MixinSoulTerrorOnEntityTickUpdateProcedure
{
	@Inject(method = "execute", remap = false, require = 1, at = @At("HEAD"))
	private static void foreverfixes$skipDashWithoutPlayer(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci)
	{
		if (entity == null)
		{
			return;
		}
		final CompoundTag data = entity.getPersistentData();
		final double ia = data.getDouble("IA");
		final double next = entity instanceof Mob mob && mob.getTarget() != null ? ia + 1.0 : ia;
		if (next == 60.0 && world.getEntitiesOfClass(Player.class, AABB.ofSize(new Vec3(x, y, z), 64.0, 64.0, 64.0), p -> true).isEmpty())
		{
			// the procedure adds 1 for a targeting mob before testing == 60
			data.putDouble("IA", next == ia ? 61.0 : 60.0);
		}
	}
}
