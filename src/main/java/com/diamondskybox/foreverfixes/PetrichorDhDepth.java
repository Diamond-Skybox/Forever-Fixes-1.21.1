package com.diamondskybox.foreverfixes;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.enums.config.EDhApiDepthDirection;
import com.seibel.distanthorizons.api.enums.config.EDhApiDepthRange;
import com.seibel.distanthorizons.api.enums.rendering.EDhApiRenderPass;
import com.seibel.distanthorizons.api.interfaces.render.IDhApiRenderProxy;
import com.seibel.distanthorizons.api.methods.events.DhApiEventRegister;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeApplyShaderRenderEvent;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiCancelableEventParam;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import com.seibel.distanthorizons.api.objects.DhApiResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.io.IOException;

/**
 * Petrichor + Distant Horizons: the LOD terrain counts as terrain for Petrichor's rain haze. Petrichor draws its haze over the
 * finished frame from a copy of Minecraft's depth buffer, treating empty depth as sky (a thick haze and its overcast cloud deck);
 * Distant Horizons draws its LODs with a depth buffer of its own, so every LOD pixel read as sky and was painted over in rain. Right
 * after Petrichor's copy, the empty pixels where Distant Horizons drew terrain get the depth Minecraft's projection gives the same
 * point (shaders/program/petrichor_dh_depth.fsh), so the LODs are hazed by their distance like the chunks in front. Client only.
 */
public final class PetrichorDhDepth
{
	private static final String PROGRAM = ForeverFixes.MOD_ID + ":petrichor_dh_depth";
	/** Distant Horizons counts as drawing while it rendered within this long */
	private static final long RECENT_NANOS = 250_000_000L;
	private static final Matrix4f DH_PROJECTION = new Matrix4f();
	private static final Matrix4f DH_INVERSE = new Matrix4f();
	private static volatile long lastDhRender;
	private static EffectInstance effect;
	private static boolean failed;

	private PetrichorDhDepth()
	{
	}

	public static void register()
	{
		final DhApiResult<Void> result = DhApiEventRegister.on(DhApiBeforeApplyShaderRenderEvent.class, new Capture());
		if (!result.success)
		{
			ForeverFixes.LOGGER.warn("Petrichor + Distant Horizons: could not hook Distant Horizons' rendering ({}); the rain haze keeps painting over the LODs",
					result.message);
		}
	}

	/** Distant Horizons' projection of the frame (opaque pass), as Better Simple Clouds captures it */
	private static final class Capture extends DhApiBeforeApplyShaderRenderEvent
	{
		@Override
		public void beforeRender(DhApiCancelableEventParam<DhApiRenderParam> event)
		{
			final DhApiRenderParam param = event.value;
			if (param != null && param.renderPass != EDhApiRenderPass.TRANSPARENT)
			{
				DH_PROJECTION.setTransposed(param.dhProjectionMatrix.getValuesAsArray());
				lastDhRender = System.nanoTime();
			}
		}
	}

	/** Petrichor just copied Minecraft's depth into {@code petrichorDepth}: the LOD terrain is written into its empty pixels. */
	public static void merge(RenderTarget petrichorDepth)
	{
		if (failed || petrichorDepth == null || System.nanoTime() - lastDhRender > RECENT_NANOS)
		{
			return;
		}
		final IDhApiRenderProxy proxy = DhApi.Delayed.renderProxy;
		if (proxy == null)
		{
			return;
		}
		final int dhDepth;
		final boolean reverseZ;
		final boolean zeroToOne;
		try
		{
			final DhApiResult<Integer> texture = proxy.getDhDepthTextureGlId();
			if (!texture.success || texture.payload == null || texture.payload <= 0)
			{
				return;
			}
			dhDepth = texture.payload;
			reverseZ = proxy.getDepthDirection() == EDhApiDepthDirection.REVERSE_Z;
			zeroToOne = proxy.getDepthRange() == EDhApiDepthRange.ZERO_TO_POS_ONE;
		}
		catch (IllegalStateException e)
		{
			return;
		}
		final Minecraft mc = Minecraft.getInstance();
		final RenderTarget main = mc.getMainRenderTarget();
		if (main == null || main.width <= 0 || main.height <= 0)
		{
			return;
		}
		if (effect == null)
		{
			try
			{
				effect = new EffectInstance(mc.getResourceManager(), PROGRAM);
			}
			catch (IOException | RuntimeException e)
			{
				failed = true;
				ForeverFixes.LOGGER.error("Petrichor + Distant Horizons: the LOD depth shader could not be loaded, the rain haze keeps painting over the LODs", e);
				return;
			}
		}
		final int read = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
		final int draw = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
		final int width = petrichorDepth.width;
		final int height = petrichorDepth.height;
		final int mainDepth = main.getDepthTextureId();
		effect.setSampler("MainDepth", () -> mainDepth);
		effect.setSampler("DhDepth", () -> dhDepth);
		effect.safeGetUniform("ProjMat").set(new Matrix4f().setOrtho(0.0F, width, 0.0F, height, 0.1F, 1000.0F));
		effect.safeGetUniform("OutSize").set((float) width, (float) height);
		effect.safeGetUniform("DhInvProj").set(DH_INVERSE.set(DH_PROJECTION).invert());
		effect.safeGetUniform("McProj").set(new Matrix4f(RenderSystem.getProjectionMatrix()));
		effect.safeGetUniform("DhReverseZ").set(reverseZ ? 1.0F : 0.0F);
		effect.safeGetUniform("DhZeroToOne").set(zeroToOne ? 1.0F : 0.0F);
		GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, petrichorDepth.frameBufferId);
		RenderSystem.viewport(0, 0, width, height);
		RenderSystem.colorMask(false, false, false, false);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL11.GL_ALWAYS);
		RenderSystem.depthMask(true);
		effect.apply();
		final BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
		buffer.addVertex(0.0F, 0.0F, 500.0F);
		buffer.addVertex(width, 0.0F, 500.0F);
		buffer.addVertex(width, height, 500.0F);
		buffer.addVertex(0.0F, height, 500.0F);
		BufferUploader.draw(buffer.buildOrThrow());
		effect.clear();
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		RenderSystem.colorMask(true, true, true, true);
		GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, read);
		GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, draw);
		RenderSystem.viewport(0, 0, main.width, main.height);
	}
}
