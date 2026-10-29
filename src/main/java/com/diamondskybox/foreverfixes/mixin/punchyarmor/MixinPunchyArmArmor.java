package com.diamondskybox.foreverfixes.mixin.punchyarmor;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Punchy draws chest armor on the first-person arms with the vanilla armor model, textured from the armor material's layers
 * (textures/models/armor/<name>_layer_1.png). Armor drawn by its own model (RPG series through Armor Model API, e.g. Paladins'
 * Crusader armor) has no such texture, so the sleeve came out black with only the enchantment glint. The sleeve is skipped when a
 * layer texture is missing; vanilla-style armor keeps its sleeves.
 */
@Mixin(targets = "punchy.client.render.PunchyArmRenderer", remap = false)
public abstract class MixinPunchyArmArmor
{
	@Inject(method = "renderArmArmor", remap = false, require = 1, at = @At("HEAD"), cancellable = true)
	private static void foreverfixes$skipMissingSleeve(PlayerModel<?> model, AbstractClientPlayer player, HumanoidArm arm, PoseStack poseStack,
			MultiBufferSource buffers, int light, boolean slim, float partialTick, CallbackInfo ci)
	{
		if (player == null || !ForeverFixesConfig.enabled(ForeverFixesConfig.PUNCHY_SKIP_MISSING_SLEEVES)
				|| !(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorItem armor))
		{
			return;
		}
		for (ArmorMaterial.Layer layer : armor.getMaterial().value().layers())
		{
			if (Minecraft.getInstance().getResourceManager().getResource(layer.texture(false)).isEmpty())
			{
				ci.cancel();
				return;
			}
		}
	}
}
