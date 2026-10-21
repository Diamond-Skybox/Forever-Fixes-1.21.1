package com.diamondskybox.foreverfixes.mixin.cosmonautics;

import dev.egg.registries.BlockEntityRegistry;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/**
 * When Dimensional Sable (bundled in Create: Cosmonautics) moves ships to another dimension, block entities that link two
 * sub-levels (rope connectors and winches, docking connectors, swivel bearings) get the stored id of the other sub-level
 * rewritten to its new copy: {@code moveInfo.oldToNewSubLevelMap().get(otherId).first()}. If the other sub-level was not part
 * of the move (a rope end outside the gathered area, a swivel plate that was not picked up), get returns null and the server
 * crashes in the physics tick ({@code "newSubLevel" is null}). Such a connection is now left as it was, with a warning; that
 * one link may point at nothing on the moved ship, but the transfer completes.
 */
@Mixin(targets = "dev.egg.registries.ConnectionBlockEntityAccessor", remap = false)
public abstract class MixinConnectionBlockEntityAccessor
{
	@Unique
	private static final Logger foreverfixes$LOGGER = LoggerFactory.getLogger("ForeverFixes");

	@Shadow(remap = false)
	@Final
	private String subLevelTag;

	@Inject(method = "modifyNBT", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private void foreverfixes$skipUnmovedPartner(BlockEntityRegistry.BlockEntityInfo info, CallbackInfoReturnable<CompoundTag> cir)
	{
		final CompoundTag tag = info.tag();
		if (tag == null || !tag.hasUUID(this.subLevelTag))
		{
			return;
		}
		final UUID partner = tag.getUUID(this.subLevelTag);
		if (!info.moveInfo().oldToNewSubLevelMap().containsKey(partner))
		{
			foreverfixes$LOGGER.warn("Ship transfer: a connection block ({}) links to sub-level {}, which was not moved with it; "
					+ "leaving that link unchanged instead of crashing", tag.getString("id"), partner);
			cir.setReturnValue(tag);
		}
	}
}
