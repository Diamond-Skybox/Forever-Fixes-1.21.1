package com.diamondskybox.foreverfixes.mixin.punchychromeware;

import com.chromeware.client.body.CyberLimbRenderer;
import com.chromeware.client.body.LimbRenderContext;
import com.chromeware.client.cyberware.GorillaArmRenderer;
import com.chromeware.client.cyberware.GorillaFirstPerson;
import com.chromeware.client.extendo.ExtendoRenderer;
import com.chromeware.client.launcher.LauncherRenderer;
import com.chromeware.client.mantis.MantisRenderer;
import com.chromeware.client.monowire.MonowireWristRenderer;
import com.diamondskybox.foreverfixes.ForeverFixes;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Punchy draws first-person arms itself (its own PlayerModel), bypassing the vanilla renderHand that Chromeware hooks, so no implant
 * showed. Chromeware's limb scope (its flesh rules: Gorilla hides the arm, blades cut the forearm, Monowire cuts the wrist) is opened
 * around Punchy's arm, and the implant renderers run on Punchy's arm right after it is drawn, as Chromeware does after renderHand.
 */
@Mixin(targets = "punchy.client.render.PunchyArmRenderer", remap = false)
public abstract class MixinPunchyChromewareImplants
{
	@Unique
	private static final ThreadLocal<Deque<LimbRenderContext[]>> foreverfixes$scopes = ThreadLocal.withInitial(ArrayDeque::new);

	@Inject(method = "renderArm", remap = false, require = 1, at = @At("HEAD"))
	private static void foreverfixes$openScope(PlayerModel<?> model, AbstractClientPlayer player, HumanoidArm side, PoseStack pose, MultiBufferSource buffers,
			int light, ResourceLocation texture, boolean slim, float partialTicks, CallbackInfo ci)
	{
		LimbRenderContext scope = null;
		try
		{
			scope = LimbRenderContext.open(player, model, false);
		}
		catch (RuntimeException e)
		{
			ForeverFixes.LOGGER.warn("Chromeware limb scope for Punchy failed: {}", e.toString());
		}
		foreverfixes$scopes.get().push(new LimbRenderContext[]{scope});
	}

	@Inject(method = "renderArm", remap = false, require = 1, at = @At("RETURN"))
	private static void foreverfixes$closeScope(PlayerModel<?> model, AbstractClientPlayer player, HumanoidArm side, PoseStack pose, MultiBufferSource buffers,
			int light, ResourceLocation texture, boolean slim, float partialTicks, CallbackInfo ci)
	{
		final Deque<LimbRenderContext[]> scopes = foreverfixes$scopes.get();
		if (!scopes.isEmpty())
		{
			final LimbRenderContext scope = scopes.pop()[0];
			if (scope != null)
			{
				scope.close();
			}
		}
	}

	@Inject(method = "renderArm", remap = false, require = 1,
			at = @At(value = "INVOKE", ordinal = 0, shift = At.Shift.AFTER, remap = false,
					target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
	private static void foreverfixes$implants(PlayerModel<?> model, AbstractClientPlayer player, HumanoidArm side, PoseStack pose, MultiBufferSource buffers,
			int light, ResourceLocation texture, boolean slim, float partialTicks, CallbackInfo ci)
	{
		final ModelPart arm = side == HumanoidArm.LEFT ? model.leftArm : model.rightArm;
		final ModelPart sleeve = side == HumanoidArm.LEFT ? model.leftSleeve : model.rightSleeve;
		try
		{
			if (GorillaFirstPerson.showsArm(player, side))
			{
				GorillaArmRenderer.render(player, side, arm, pose, buffers, light);
			}
			CyberLimbRenderer.renderArm(player, side, arm, pose, buffers, light);
			MonowireWristRenderer.render(player, side, arm, pose, buffers, light, true);
			LauncherRenderer.render(player, side, arm, pose, buffers, light);
			MantisRenderer.render(player, side, arm, pose, buffers, light);
			ExtendoRenderer.render(player, side, arm, sleeve, pose, buffers, light);
		}
		catch (RuntimeException e)
		{
			ForeverFixes.LOGGER.warn("Chromeware implants on Punchy's arm failed: {}", e.toString());
		}
	}
}
