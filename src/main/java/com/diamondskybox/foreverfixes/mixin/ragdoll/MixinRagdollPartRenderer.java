package com.diamondskybox.foreverfixes.mixin.ragdoll;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.UUID;

/**
 * Sable: Ragdolls draws each ragdoll part in the player's Minecraft skin and then runs the player renderer's layers on it. MCA
 * draws its look as such layers (skin, face, hair, clothing, limited to the visible part), so a player MCA draws got the MCA part
 * plus the Minecraft-skin part underneath; the head showed twice. For players MCA draws as villagers the Minecraft-skin pass is
 * skipped; everyone else keeps it.
 * <p>
 * Corpses (Sable: Ragdoll Corpse) are drawn on a stand-in doll entity instead of the player, and MCA looks its data up by the
 * drawn entity's UUID, so a corpse had no MCA look and showed only the Minecraft skin. While a part of an MCA player's corpse is
 * drawn, the doll carries the owner's UUID (the doll is the renderer's own, never added to the level).
 */
@Mixin(targets = "dev.leo.sableplayerragdoll.neoforge.client.RagdollPartBlockEntityRenderer", remap = false)
public abstract class MixinRagdollPartRenderer
{
	@Unique
	private static MethodHandle foreverfixes$useVillagerRenderer;
	@Unique
	private static boolean foreverfixes$lookedUp;

	/** the doll's own UUID, restored for corpses MCA does not draw */
	@Unique
	private UUID foreverfixes$dollUuid;
	/** the part being drawn belongs to a player MCA draws as a villager */
	@Unique
	private boolean foreverfixes$mcaOwner;

	@Inject(method = "renderLayers", remap = false, require = 1, at = @At("HEAD"))
	private void foreverfixes$lendOwnerUuid(RagdollPartBlockEntity blockEntity, RagdollPartBlockEntity.BodyPart bodyPart, LivingEntity entity,
			PoseStack pose, MultiBufferSource buffer, int light, float partialTick, CallbackInfo ci)
	{
		final UUID owner = blockEntity.skinProfile().getId();
		foreverfixes$mcaOwner = owner != null && ForeverFixesConfig.enabled(ForeverFixesConfig.RAGDOLL_MCA_SKIN) && foreverfixes$drawnByMca(owner);
		if (!(entity instanceof Player))
		{
			if (foreverfixes$dollUuid == null)
			{
				foreverfixes$dollUuid = entity.getUUID();
			}
			entity.setUUID(foreverfixes$mcaOwner ? owner : foreverfixes$dollUuid);
		}
	}

	@WrapOperation(method = "renderLayers", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/client/model/PlayerModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
	private void foreverfixes$skipSkinUnderMca(PlayerModel<?> model, PoseStack pose, VertexConsumer buffer, int light, int overlay,
			Operation<Void> original)
	{
		if (foreverfixes$mcaOwner)
		{
			return;
		}
		original.call(model, pose, buffer, light, overlay);
	}

	@Unique
	private static boolean foreverfixes$drawnByMca(UUID player)
	{
		if (!foreverfixes$lookedUp)
		{
			foreverfixes$lookedUp = true;
			try
			{
				foreverfixes$useVillagerRenderer = MethodHandles.publicLookup().findStatic(Class.forName("net.conczin.mca.MCAClient"),
						"useVillagerRenderer", MethodType.methodType(boolean.class, UUID.class));
			}
			catch (ReflectiveOperationException e)
			{
				foreverfixes$useVillagerRenderer = null;
			}
		}
		try
		{
			return foreverfixes$useVillagerRenderer != null && (boolean) foreverfixes$useVillagerRenderer.invokeExact(player);
		}
		catch (Throwable t)
		{
			return false;
		}
	}
}
