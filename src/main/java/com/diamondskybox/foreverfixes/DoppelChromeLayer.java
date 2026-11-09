package com.diamondskybox.foreverfixes;

import com.chromeware.body.BodyPart;
import com.chromeware.body.CyberLimbs;
import com.chromeware.client.body.CyberLimbRenderer;
import com.chromeware.client.cyberware.ImplantTint;
import com.chromeware.cyberware.ImplantColors;
import com.chromeware.cyberware.PlayerData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.bandit.darkdoppelganger.entity.DarkDoppelgangerEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws the cyber limbs a Dark Doppelganger copied from its summoner (see DoppelChrome) with Chromeware's own limb meshes and the
 * summoner's Shell colours, and hides the boss's flesh on those limbs, like Chromeware does on the player. Synthskin limbs stay flesh.
 * <p>
 * The boss is an Iron's casting mob (GeckoLib), laid out like the vanilla player model: the torso and limb bones pivot where the
 * player's do. Chromeware draws a limb mesh relative to the vanilla part's pivot, so each mesh is drawn at its bone's pivot with the
 * bone's animated rotation, flipped into vanilla model space. Proxy vanilla parts at the origin give Chromeware the cube size it
 * reads (wide arms, like the boss's own).
 */
public final class DoppelChromeLayer extends GeoRenderLayer<AbstractSpellCastingMob>
{
	private static final String[] BONES = {"torso", "left_arm", "right_arm", "left_leg", "right_leg"};
	private static final BodyPart[] PARTS = {BodyPart.BODY, BodyPart.LEFT_ARM, BodyPart.RIGHT_ARM, BodyPart.LEFT_LEG, BodyPart.RIGHT_LEG};
	private static final String[] VANILLA = {"body", "left_arm", "right_arm", "left_leg", "right_leg"};
	private static ModelPart[] proxies;

	/** Bones hidden for the entity being drawn, with their previous hidden / children-hidden flags (the model is shared). */
	private final List<GeoBone> hidden = new ArrayList<>();
	private final List<boolean[]> previous = new ArrayList<>();
	private final boolean[] drawing = new boolean[PARTS.length];
	private ImplantColors colors = ImplantColors.EMPTY;

	public DoppelChromeLayer(GeoRenderer<AbstractSpellCastingMob> renderer)
	{
		super(renderer);
	}

	private static ModelPart[] proxies()
	{
		if (proxies == null)
		{
			final ModelPart root = LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64).bakeRoot();
			final ModelPart[] parts = new ModelPart[VANILLA.length];
			for (int i = 0; i < VANILLA.length; i++)
			{
				parts[i] = root.getChild(VANILLA[i]);
				parts[i].setPos(0.0F, 0.0F, 0.0F);
				parts[i].setRotation(0.0F, 0.0F, 0.0F);
			}
			proxies = parts;
		}
		return proxies;
	}

	@Override
	public void preRender(PoseStack poseStack, AbstractSpellCastingMob animatable, BakedGeoModel bakedModel, RenderType renderType,
			MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay)
	{
		restore();
		java.util.Arrays.fill(this.drawing, false);
		if (!(animatable instanceof DarkDoppelgangerEntity) || animatable.isInvisible() || !animatable.hasData(CyberLimbs.FRAMES)
				|| !ForeverFixesCommonConfig.SPEC.isLoaded() || !ForeverFixesCommonConfig.DOPPEL_CHROME.get())
		{
			return;
		}
		final CyberLimbs frames = animatable.getData(CyberLimbs.FRAMES);
		this.colors = animatable.hasData(PlayerData.IMPLANT_COLORS) ? animatable.getData(PlayerData.IMPLANT_COLORS) : ImplantColors.EMPTY;
		for (int i = 0; i < PARTS.length; i++)
		{
			if (!frames.has(PARTS[i]) || frames.skinned(PARTS[i]))
			{
				continue;
			}
			final GeoBone bone = bakedModel.getBone(BONES[i]).orElse(null);
			if (bone == null)
			{
				continue;
			}
			this.drawing[i] = true;
			this.hidden.add(bone);
			this.previous.add(new boolean[] {bone.isHidden(), bone.isHidingChildren()});
			// cubes only: the armour, item and hand bones under it still render
			bone.setHidden(true);
			bone.setChildrenHidden(this.previous.get(this.previous.size() - 1)[1]);
		}
	}

	@Override
	public void renderForBone(PoseStack poseStack, AbstractSpellCastingMob animatable, GeoBone bone, RenderType renderType,
			MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay)
	{
		final String name = bone.getName();
		for (int i = 0; i < BONES.length; i++)
		{
			if (this.drawing[i] && BONES[i].equals(name))
			{
				poseStack.pushPose();
				poseStack.translate(bone.getPivotX() / 16.0F, bone.getPivotY() / 16.0F, bone.getPivotZ() / 16.0F);
				poseStack.scale(-1.0F, -1.0F, 1.0F);
				final VertexConsumer chrome = bufferSource.getBuffer(RenderType.entityCutoutNoCull(ImplantTint.texture(this.colors, CyberLimbRenderer.IMPLANT)));
				CyberLimbRenderer.render(PARTS[i], proxies()[i], poseStack, chrome, packedLight, packedOverlay);
				poseStack.popPose();
				return;
			}
		}
	}

	@Override
	public void render(PoseStack poseStack, AbstractSpellCastingMob animatable, BakedGeoModel bakedModel, RenderType renderType,
			MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay)
	{
		restore();
	}

	private void restore()
	{
		for (int i = 0; i < this.hidden.size(); i++)
		{
			this.hidden.get(i).setHidden(this.previous.get(i)[0]);
			this.hidden.get(i).setChildrenHidden(this.previous.get(i)[1]);
		}
		this.hidden.clear();
		this.previous.clear();
	}
}
