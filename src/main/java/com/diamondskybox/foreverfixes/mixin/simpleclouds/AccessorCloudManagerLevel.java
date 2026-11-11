package com.diamondskybox.foreverfixes.mixin.simpleclouds;

import dev.nonamecrackers2.simpleclouds.common.world.CloudManager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Simple Clouds: the level a cloud manager belongs to. */
@Mixin(value = CloudManager.class, remap = false)
public interface AccessorCloudManagerLevel
{
	@Accessor(value = "level", remap = false)
	Level foreverfixes$level();
}
