package com.diamondskybox.foreverfixes.mixin.chromewaremobends;

import com.chromeware.body.BodyPart;
import com.diamondskybox.foreverfixes.CyberLimbBends;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mo' Bends does not draw flesh (or worn clothing layers, e.g. MCA clothes) on body parts Chromeware has replaced with cyber
 * limbs or that are missing. Limbs are hidden upper and lower segment both (the overlay path checks each segment on its own);
 * the torso is only "concealed", which skips its own geometry but keeps head and arms, and also skips the MCA breasts drawn from
 * it (McaPlayerBreasts needs a showing torso). Nested renders (overlay geometry -> current pose) act once, at the outermost.
 */
@Mixin(value = BipedMutator.class, remap = false)
public abstract class MixinBipedMutatorCyberLimbs
{
	@Shadow(remap = false)
	protected BendsModelPart leftArm;
	@Shadow(remap = false)
	protected BendsModelPart rightArm;
	@Shadow(remap = false)
	protected BendsModelPart leftForeArm;
	@Shadow(remap = false)
	protected BendsModelPart rightForeArm;
	@Shadow(remap = false)
	protected BendsModelPart leftLeg;
	@Shadow(remap = false)
	protected BendsModelPart rightLeg;
	@Shadow(remap = false)
	protected BendsModelPart leftForeLeg;
	@Shadow(remap = false)
	protected BendsModelPart rightForeLeg;
	@Shadow(remap = false)
	protected BendsModelPart body;
	@Shadow(remap = false)
	protected BendsModelPart outerBody;
	@Shadow(remap = false)
	protected BendsModelPart outerLeftArm;
	@Shadow(remap = false)
	protected BendsModelPart outerRightArm;
	@Shadow(remap = false)
	protected BendsModelPart outerLeftForeArm;
	@Shadow(remap = false)
	protected BendsModelPart outerRightForeArm;
	@Shadow(remap = false)
	protected BendsModelPart outerLeftLeg;
	@Shadow(remap = false)
	protected BendsModelPart outerRightLeg;
	@Shadow(remap = false)
	protected BendsModelPart outerLeftForeLeg;
	@Shadow(remap = false)
	protected BendsModelPart outerRightForeLeg;

	@Unique
	private int foreverfixes$depth;
	@Unique
	private BendsModelPart[] foreverfixes$hidden = new BendsModelPart[0];
	@Unique
	private BendsModelPart[] foreverfixes$concealed = new BendsModelPart[0];

	@Inject(method = {"renderCurrentPose", "renderOuter", "renderOverlayGeometry"}, remap = false, require = 3, at = @At("HEAD"))
	private void foreverfixes$hideReplaced(CallbackInfo ci)
	{
		if (this.foreverfixes$depth++ != 0 || !(MoBendsRenderContext.getCurrentEntity() instanceof Player player))
		{
			return;
		}
		final java.util.List<BendsModelPart> hide = new java.util.ArrayList<>();
		foreverfixes$limb(hide, player, BodyPart.LEFT_ARM, this.leftArm, this.leftForeArm, this.outerLeftArm, this.outerLeftForeArm);
		foreverfixes$limb(hide, player, BodyPart.RIGHT_ARM, this.rightArm, this.rightForeArm, this.outerRightArm, this.outerRightForeArm);
		foreverfixes$limb(hide, player, BodyPart.LEFT_LEG, this.leftLeg, this.leftForeLeg, this.outerLeftLeg, this.outerLeftForeLeg);
		foreverfixes$limb(hide, player, BodyPart.RIGHT_LEG, this.rightLeg, this.rightForeLeg, this.outerRightLeg, this.outerRightForeLeg);
		this.foreverfixes$hidden = hide.toArray(new BendsModelPart[0]);
		final java.util.List<BendsModelPart> conceal = new java.util.ArrayList<>();
		if (CyberLimbBends.fleshHidden(player, BodyPart.BODY))
		{
			for (BendsModelPart part : new BendsModelPart[]{this.body, this.outerBody})
			{
				if (part != null && !part.concealed)
				{
					part.concealed = true;
					conceal.add(part);
				}
			}
		}
		this.foreverfixes$concealed = conceal.toArray(new BendsModelPart[0]);
	}

	@Inject(method = {"renderCurrentPose", "renderOuter", "renderOverlayGeometry"}, remap = false, require = 3, at = @At("RETURN"))
	private void foreverfixes$restore(CallbackInfo ci)
	{
		if (--this.foreverfixes$depth != 0)
		{
			return;
		}
		for (BendsModelPart part : this.foreverfixes$hidden)
		{
			part.hidden = false;
		}
		for (BendsModelPart part : this.foreverfixes$concealed)
		{
			part.concealed = false;
		}
		this.foreverfixes$hidden = new BendsModelPart[0];
		this.foreverfixes$concealed = new BendsModelPart[0];
	}

	@Unique
	private static void foreverfixes$limb(java.util.List<BendsModelPart> out, Player player, BodyPart limb, BendsModelPart upper, BendsModelPart lower,
			BendsModelPart outerUpper, BendsModelPart outerLower)
	{
		final boolean whole = CyberLimbBends.fleshHidden(player, limb);
		final boolean forearm = !whole && (limb == BodyPart.LEFT_ARM || limb == BodyPart.RIGHT_ARM) && CyberLimbBends.forearmReplaced(player, limb);
		if (!whole && !forearm)
		{
			return;
		}
		// the outer (sleeve / clothing) layer goes with the flesh, or it would still cover the implant
		for (BendsModelPart part : whole ? new BendsModelPart[]{upper, lower, outerUpper, outerLower} : new BendsModelPart[]{lower, outerLower})
		{
			if (part != null && !part.hidden)
			{
				part.hidden = true;
				out.add(part);
			}
		}
	}
}
