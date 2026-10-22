package com.diamondskybox.foreverfixes.mixin.genderarmor;

import com.diamondskybox.foreverfixes.ForeverFixesConfig;
import com.diamondskybox.foreverfixes.RpgBreastArmor;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mcabp.wildfire.api.IGenderArmor;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Armor Model API armor on the breasts of MCA: female gender physics: its own texture at UVs from its geo model instead of the
 * missing vanilla {@code _layer_1} texture (see {@link RpgBreastArmor}). The vanilla trim overlay is left off for these items, as
 * it is drawn at the breast UVs on the vanilla trim layout and would not line up; the API draws their trims on the armor itself.
 */
@Mixin(targets = "com.mcabp.wildfire.client.render.GenderLayer", remap = false)
public abstract class MixinGenderLayer
{
	@WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
			remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lcom/mcabp/wildfire/main/WildfireHelper;getArmorConfig(Lnet/minecraft/world/item/ItemStack;)Lcom/mcabp/wildfire/api/IGenderArmor;"))
	private IGenderArmor foreverfixes$rpgUvs(ItemStack stack, Operation<IGenderArmor> original)
	{
		final IGenderArmor config = original.call(stack);
		return ForeverFixesConfig.enabled(ForeverFixesConfig.RPG_BREAST_ARMOR) ? RpgBreastArmor.config(stack, config) : config;
	}

	@WrapOperation(method = "renderBreast", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/neoforged/neoforge/client/ClientHooks;getArmorTexture(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ArmorMaterial$Layer;ZLnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/resources/ResourceLocation;"))
	private ResourceLocation foreverfixes$rpgTexture(Entity entity, ItemStack stack, ArmorMaterial.Layer layer, boolean inner, EquipmentSlot slot,
			Operation<ResourceLocation> original)
	{
		final ResourceLocation texture = ForeverFixesConfig.enabled(ForeverFixesConfig.RPG_BREAST_ARMOR) ? RpgBreastArmor.texture(stack) : null;
		return texture != null ? texture : original.call(entity, stack, layer, inner, slot);
	}

	@WrapOperation(method = "renderBreast", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
	private Object foreverfixes$noRpgTrim(ItemStack stack, DataComponentType<?> type, Operation<Object> original)
	{
		if (type == DataComponents.TRIM && ForeverFixesConfig.enabled(ForeverFixesConfig.RPG_BREAST_ARMOR) && RpgBreastArmor.texture(stack) != null)
		{
			return null;
		}
		return original.call(stack, type);
	}
}
