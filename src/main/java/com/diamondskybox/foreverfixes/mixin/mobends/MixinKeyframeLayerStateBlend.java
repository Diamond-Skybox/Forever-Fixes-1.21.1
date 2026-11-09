package com.diamondskybox.foreverfixes.mixin.mobends;

import com.diamondskybox.foreverfixes.PackBones;
import goblinbob.mobends.core.kumo.state.ConnectionState;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.INodeState;
import goblinbob.mobends.core.kumo.state.keyframe.KeyframeLayerState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.keyframe.ConnectionTemplate;
import goblinbob.mobends.lib.animation.keyframe.Bone;
import goblinbob.mobends.lib.animation.keyframe.Keyframe;
import goblinbob.mobends.lib.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.lib.client.model.IAnimatedPart;
import goblinbob.mobends.lib.data.IEntityAnimationData;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.lib.math.vector.IVec3f;
import goblinbob.mobends.lib.math.vector.SmoothVector3f;
import goblinbob.mobends.lib.util.Tween;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Mo' Bends blends a Bends-pack keyframe layer's transitions by adding the two poses' quaternions weighted (1-t) and t onto
 * a zeroed rotation. The sum isn't unit length, and the renderer turns that into a scale, so the model shrinks mid-transition
 * (to nothing when one side is a node that doesn't animate the bone), then pops back. Bones only one side animates also snap
 * instead of blending. For non-additive layers the pose is now built per bone as a normalised interpolation between the two
 * sides, where a side that doesn't animate the bone contributes the live stock pose, so a clip fades in from and back out to
 * whatever Mo' Bends was doing. Additive layers keep the original code.
 */
@Mixin(value = KeyframeLayerState.class, remap = false)
public abstract class MixinKeyframeLayerStateBlend
{
	@Shadow(remap = false)
	@Final
	private boolean additive;
	@Shadow(remap = false)
	private INodeState previousNode;
	@Shadow(remap = false)
	private INodeState currentNode;
	@Shadow(remap = false)
	private float transitionProgress;
	@Shadow(remap = false)
	private float transitionDuration;
	@Shadow(remap = false)
	private ConnectionTemplate.Easing transitionEasing;

	@Shadow(remap = false)
	private boolean shouldPartBeAffected(String partName)
	{
		throw new AssertionError();
	}

	@Inject(method = "update", remap = false, require = 1, at = @At("HEAD"), cancellable = true)
	private void foreverfixes$blendedUpdate(IKumoContext context, float deltaTime, CallbackInfo ci) throws MalformedKumoTemplateException
	{
		if (this.additive)
		{
			// Mo' Bends' own multiplicative blend is fine for deltas (they start and end at identity); only note which bones the
			// layer is adding onto, so Chromeware's implant arm pose doesn't get copied over a pack swing (packOwnsArms).
			final Set<String> names = new LinkedHashSet<>();
			for (INodeState node : new INodeState[]{this.currentNode, this.previousNode})
			{
				if (node != null && node.getAnimation() != null)
				{
					names.addAll(node.getAnimation().bones.keySet());
				}
			}
			if (!names.isEmpty())
			{
				PackBones.record(context.getEntityData(), names);
			}
			return;
		}
		ci.cancel();
		final IEntityAnimationData data = context.getEntityData();
		if (this.currentNode != null)
		{
			final KeyframeAnimation current = this.currentNode.getAnimation();
			if (this.previousNode != null)
			{
				final float t = this.transitionDuration > 0.0F ? ease(this.transitionProgress / this.transitionDuration) : 1.0F;
				foreverfixes$apply(data, this.previousNode.getAnimation(), this.previousNode.getProgress(), current, this.currentNode.getProgress(), t);
				this.transitionProgress += deltaTime;
				if (this.transitionProgress >= this.transitionDuration)
				{
					this.previousNode = null;
				}
			}
			else if (current != null)
			{
				foreverfixes$apply(data, null, 0.0F, current, this.currentNode.getProgress(), 1.0F);
			}
		}
		context.setCurrentNode(this.currentNode);
		if (this.currentNode != null)
		{
			this.currentNode.update(context, deltaTime);
		}
		if (this.previousNode != null && this.previousNode != this.currentNode)
		{
			this.previousNode.update(context, deltaTime);
		}
		if (this.currentNode == null)
		{
			return;
		}
		for (ConnectionState connection : this.currentNode.getConnections())
		{
			if (connection.triggerCondition.isConditionMet(context))
			{
				this.transitionDuration = connection.transitionDuration;
				this.transitionEasing = connection.transitionEasing;
				if (this.transitionDuration == 0.0F)
				{
					this.previousNode = null;
				}
				else
				{
					this.previousNode = this.currentNode;
					this.transitionProgress = 0.0F;
				}
				this.currentNode = connection.targetNode;
				this.currentNode.start(context);
				break;
			}
		}
	}

	private float ease(float t)
	{
		t = Math.max(0.0F, Math.min(1.0F, t));
		if (this.transitionEasing == null)
		{
			return t;
		}
		return switch (this.transitionEasing)
		{
			case EASE_IN -> (float) Tween.easeIn(t, 2.0D);
			case EASE_OUT -> (float) Tween.easeOut(t, 2.0D);
			case EASE_IN_OUT -> (float) Tween.easeInOut(t, 2.0D);
			default -> t;
		};
	}

	/** Pose = lerp(side A, side B, t) per bone; a side without the bone is the live stock pose. */
	private void foreverfixes$apply(IEntityAnimationData data, KeyframeAnimation a, float progressA, KeyframeAnimation b, float progressB, float t)
	{
		final Set<String> names = new LinkedHashSet<>();
		if (a != null)
		{
			names.addAll(a.bones.keySet());
		}
		if (b != null)
		{
			names.addAll(b.bones.keySet());
		}
		PackBones.record(data, names);
		for (String name : names)
		{
			if (!this.shouldPartBeAffected(name))
			{
				continue;
			}
			final float[][] sa = sample(a, name, progressA);
			final float[][] sb = sample(b, name, progressB);
			if ("root".equals(name))
			{
				final SmoothVector3f global = data.getGlobalOffset();
				final float[] stock = {global.getX(), global.getY(), global.getZ()};
				final float[] p = lerp(sa == null ? stock : sa[1], sb == null ? stock : sb[1], t);
				global.set(p[0], p[1], p[2]);
				global.finish();
				continue;
			}
			if ("centerRotation".equals(name))
			{
				blend(data.getCenterRotation(), sa == null ? null : sa[0], sb == null ? null : sb[0], t);
				final float[] zero = {0.0F, 0.0F, 0.0F};
				final float[] p = lerp(sa == null ? zero : sa[1], sb == null ? zero : sb[1], t);
				if (p[0] != 0.0F || p[1] != 0.0F || p[2] != 0.0F)
				{
					final SmoothVector3f global = data.getGlobalOffset();
					global.add(p[0], p[1], p[2]);
					global.finish();
				}
				continue;
			}
			if (data.getPartForName(name) instanceof IAnimatedPart part)
			{
				blend(part.getRotation(), sa == null ? null : sa[0], sb == null ? null : sb[0], t);
				final IVec3f offset = part.getOffset();
				final float[] stock = {offset.getX(), offset.getY(), offset.getZ()};
				// a keyframe position P is applied as a part offset of -P
				final float[] oa = sa == null ? stock : new float[] {-sa[1][0], -sa[1][1], -sa[1][2]};
				final float[] ob = sb == null ? stock : new float[] {-sb[1][0], -sb[1][1], -sb[1][2]};
				final float[] p = lerp(oa, ob, t);
				offset.set(p[0], p[1], p[2]);
			}
		}
	}

	/** {rotation xyzw, position xyz} of a bone at a fractional frame, or null when the clip doesn't animate it. */
	private static float[][] sample(KeyframeAnimation animation, String name, float progress)
	{
		if (animation == null)
		{
			return null;
		}
		final Bone bone = animation.bones.get(name);
		if (bone == null || bone.keyframes == null || bone.keyframes.isEmpty())
		{
			return null;
		}
		final int frameA = Math.max(0, (int) progress);
		final float tween = progress - (int) progress;
		final Keyframe ka = at(bone.keyframes, frameA);
		final Keyframe kb = at(bone.keyframes, frameA + 1);
		return new float[][] {nlerp(ka.rotation, kb.rotation, tween), lerp(ka.position, kb.position, tween)};
	}

	private static Keyframe at(List<Keyframe> keyframes, int index)
	{
		return keyframes.get(Math.min(index, keyframes.size() - 1));
	}

	private static void blend(SmoothOrientation orientation, float[] qa, float[] qb, float t)
	{
		if (qa == null && qb == null)
		{
			return;
		}
		final Quaternion s = orientation.getSmooth();
		final float[] stock = {s.x, s.y, s.z, s.w};
		final float[] q = nlerp(qa == null ? stock : qa, qb == null ? stock : qb, t);
		orientation.set(q[0], q[1], q[2], q[3]);
	}

	private static float[] lerp(float[] a, float[] b, float t)
	{
		return new float[] {a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t};
	}

	/** Normalised linear interpolation along the shorter arc; identity when both inputs are degenerate. */
	private static float[] nlerp(float[] a, float[] b, float t)
	{
		float dot = a[0] * b[0] + a[1] * b[1] + a[2] * b[2] + a[3] * b[3];
		final float sign = dot < 0.0F ? -1.0F : 1.0F;
		final float[] q = {
				a[0] + (sign * b[0] - a[0]) * t,
				a[1] + (sign * b[1] - a[1]) * t,
				a[2] + (sign * b[2] - a[2]) * t,
				a[3] + (sign * b[3] - a[3]) * t};
		final float length = (float) Math.sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3]);
		if (length < 1.0E-6F)
		{
			return new float[] {0.0F, 0.0F, 0.0F, 1.0F};
		}
		return new float[] {q[0] / length, q[1] / length, q[2] / length, q[3] / length};
	}
}
