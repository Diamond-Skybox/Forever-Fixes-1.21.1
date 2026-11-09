package com.diamondskybox.foreverfixes;

import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.PlayerData;

/**
 * Copies one Mo' Bends biped pose onto another. Chromeware's Sandevistan draws the player a second time through a proxy
 * entity (the live copy for its post effect and the afterimage captures); Mo' Bends treated that proxy as its own player and
 * animated it from scratch, so under Mo' Bends the copy walked while the real player attacked. The proxy now shows the
 * real player's pose of this frame.
 */
public final class ProxyPose
{
	private ProxyPose()
	{
	}

	public static void copy(BipedEntityData<?> from, BipedEntityData<?> to)
	{
		part(from.head, to.head);
		part(from.body, to.body);
		part(from.rightArm, to.rightArm);
		part(from.leftArm, to.leftArm);
		part(from.rightForeArm, to.rightForeArm);
		part(from.leftForeArm, to.leftForeArm);
		part(from.rightLeg, to.rightLeg);
		part(from.leftLeg, to.leftLeg);
		part(from.rightForeLeg, to.rightForeLeg);
		part(from.leftForeLeg, to.leftForeLeg);
		if (from instanceof PlayerData a && to instanceof PlayerData b)
		{
			part(a.cape, b.cape);
		}
		to.globalOffset.set(from.globalOffset);
		to.localOffset.set(from.localOffset);
		to.renderRotation.set(from.renderRotation);
		to.centerRotation.set(from.centerRotation);
		to.renderRightItemRotation.set(from.renderRightItemRotation);
		to.renderLeftItemRotation.set(from.renderLeftItemRotation);
		to.headYaw.set(from.headYaw.get());
		to.headPitch.set(from.headPitch.get());
		to.limbSwing.set(from.limbSwing.get());
		to.limbSwingAmount.set(from.limbSwingAmount.get());
		to.swingProgress.set(from.swingProgress.get());
		to.externalPoseAdopted = from.externalPoseAdopted;
	}

	private static void part(ModelPartTransform from, ModelPartTransform to)
	{
		if (from != null && to != null)
		{
			to.syncUp(from);
		}
	}
}
