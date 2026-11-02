package com.diamondskybox.foreverfixes;

import com.diamondskybox.foreverfixes.mixin.bscglass.AccessorEffectInstance;
import com.diamondskybox.foreverfixes.mixin.bscglass.AccessorSimpleCloudsRenderer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.bettersimpleclouds.core.IrisBridge;
import dev.bettersimpleclouds.dh.CloudDepthCopy;
import dev.bettersimpleclouds.immersion.CloudDepthFormat;
import dev.nonamecrackers2.simpleclouds.client.framebuffer.CloudRenderTarget;
import dev.nonamecrackers2.simpleclouds.client.renderer.SimpleCloudsRenderer;
import dev.nonamecrackers2.simpleclouds.mixin.MixinPostChain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * Simple Clouds + Better Simple Clouds storm fog fixes. Client only, and only loaded when both mods are installed.
 * <ul>
 * <li>Particles are not glass: Better Simple Clouds tells glass from what is behind it by comparing its depth snapshot from
 * before the translucent pass with the depth "with the translucent blocks in it", which under Fast/Fancy graphics is the
 * finished depth, particles included. A second snapshot after the translucent blocks and before the translucent particles
 * takes its place, and the rain haze reads it too.</li>
 * <li>Beacon glow depth: the outer glow of a beacon beam writes no depth, so the storm fog and rain haze were worked out to
 * whatever was behind the beam. The glow boxes of the frame's beams are drawn into both snapshots, depth only.</li>
 * <li>Storm blur: Simple Clouds blurs the storm fog with minecraft:shaders/post/blur.json, which other mods replace (Exposure
 * Expanded's adds up alpha, which turned distant rain columns black); a copy of vanilla's is used instead.</li>
 * <li>Depth-aware storm fog: the quarter-resolution storm fog is stretched to the screen by depth, not linearly.</li>
 * </ul>
 */
public final class StormFogFixes
{
	private static final ResourceLocation VANILLA_BLUR = ResourceLocation.withDefaultNamespace("shaders/post/blur.json");
	private static final ResourceLocation STORM_BLUR = ResourceLocation.fromNamespaceAndPath(ForeverFixes.MOD_ID, "shaders/post/storm_blur.json");
	private static final String UPSAMPLE_PROGRAM = ForeverFixes.MOD_ID + ":storm_upsample";
	private static final int MAX_BEAMS = 512;
	private static final int CHECKED_BLITS = 8;

	private static final List<Beam> beams = new ArrayList<>();
	/** Better Simple Clouds took its depth snapshot this frame */
	private static boolean captured;
	/** the depth after the translucent blocks, before the translucent particles */
	private static RenderTarget glass;
	private static boolean glassReady;
	private static boolean blitRefused;
	private static int checkedBlits;
	/** the rain haze pass is running: it reads the glass snapshot as the main depth */
	private static boolean inRainHaze;
	private static PostPass upsample;
	private static RenderTarget upsampleIn;
	private static RenderTarget upsampleOut;
	private static boolean upsampleFailed;

	private record Beam(Matrix4f pose, float bottom, float top, float radius)
	{
	}

	private StormFogFixes()
	{
	}

	public static void onStage(RenderLevelStageEvent event)
	{
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY)
		{
			beams.clear();
			captured = false;
			glassReady = false;
		}
		else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS && captured
				&& ForeverFixesConfig.enabled(ForeverFixesConfig.BSC_PARTICLES_NOT_GLASS) && !IrisBridge.shadersRunning()
				&& Minecraft.getInstance().levelRenderer.getTranslucentTarget() == null)
		{
			snapshotGlass();
		}
	}

	/** a beacon beam segment as vanilla draws it: the glow is a box of half-width glowRadius around the block's centre column */
	public static void recordBeam(PoseStack poseStack, int yOffset, int height, float glowRadius)
	{
		if (beams.size() < MAX_BEAMS && ForeverFixesConfig.enabled(ForeverFixesConfig.BSC_BEACON_GLOW_DEPTH))
		{
			beams.add(new Beam(new Matrix4f(poseStack.last().pose()), Math.min(yOffset, yOffset + height), Math.max(yOffset, yOffset + height), glowRadius));
		}
	}

	/** Better Simple Clouds copied the main depth into its snapshot */
	public static void afterCapture(RenderTarget opaque)
	{
		captured = true;
		drawGlowDepth(opaque);
	}

	/** the glass depth for Better Simple Clouds, or -1 to leave it its own */
	public static int glassDepthTexture()
	{
		return glassReady ? glass.getDepthTextureId() : -1;
	}

	public static void setInRainHaze(boolean running)
	{
		inRainHaze = running;
	}

	/** the depth texture the rain haze reads in place of the main one, or -1 */
	public static int rainHazeDepth(RenderTarget target)
	{
		return inRainHaze && glassReady && target == Minecraft.getInstance().getMainRenderTarget() ? glass.getDepthTextureId() : -1;
	}

	public static ResourceLocation stormBlurChain(ResourceLocation chain)
	{
		return VANILLA_BLUR.equals(chain) && ForeverFixesConfig.enabled(ForeverFixesConfig.SC_STORM_BLUR_VANILLA) ? STORM_BLUR : chain;
	}

	public static void onResourceReload()
	{
		closeUpsample();
		upsampleFailed = false;
	}

	private static void snapshotGlass()
	{
		final RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
		if (main == null || main.width <= 0 || main.height <= 0)
		{
			return;
		}
		final int read = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
		final int draw = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
		if (glass == null)
		{
			glass = new CloudRenderTarget(main.width, main.height, Minecraft.ON_OSX, false);
			glass.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
		}
		else if (glass.width != main.width || glass.height != main.height)
		{
			glass.resize(main.width, main.height, Minecraft.ON_OSX);
		}
		else
		{
			CloudDepthFormat.keepUp(glass);
		}
		// as Better Simple Clouds copies the main depth: a blit, or a conversion where the driver refuses the format
		final boolean check = !blitRefused && checkedBlits < CHECKED_BLITS;
		if (check)
		{
			while (GL11.glGetError() != GL11.GL_NO_ERROR)
			{
			}
		}
		GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
		GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, glass.frameBufferId);
		if (!blitRefused)
		{
			GL30.glBlitFramebuffer(0, 0, main.width, main.height, 0, 0, glass.width, glass.height, GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
		}
		if (check)
		{
			checkedBlits++;
			if (GL11.glGetError() != GL11.GL_NO_ERROR)
			{
				blitRefused = true;
				ForeverFixes.LOGGER.info("Better Simple Clouds glass depth: the main depth cannot be blitted, copying it with a conversion instead");
			}
		}
		if (blitRefused)
		{
			CloudDepthCopy.convert(glass, main);
		}
		GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, read);
		GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, draw);
		RenderSystem.viewport(0, 0, main.width, main.height);
		drawGlowDepth(glass);
		glassReady = true;
	}

	/** draws the outer glow of this frame's beacon beams into the depth of the target, nothing into its colour */
	private static void drawGlowDepth(RenderTarget target)
	{
		if (beams.isEmpty() || target == null || IrisBridge.shadersRunning())
		{
			return;
		}
		final int read = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
		final int draw = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
		GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, target.frameBufferId);
		RenderSystem.viewport(0, 0, target.width, target.height);
		RenderSystem.colorMask(false, false, false, false);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		RenderSystem.depthMask(true);
		RenderSystem.disableCull();
		RenderSystem.disableBlend();
		RenderSystem.setShader(GameRenderer::getPositionShader);
		// the poses are the block entity poses (block position minus camera); the model-view matrix still holds the camera
		final BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
		for (Beam beam : beams)
		{
			final Matrix4f m = beam.pose();
			final float lo = 0.5F - beam.radius();
			final float hi = 0.5F + beam.radius();
			final float y0 = beam.bottom();
			final float y1 = beam.top();
			buffer.addVertex(m, lo, y0, lo).addVertex(m, hi, y0, lo).addVertex(m, hi, y1, lo).addVertex(m, lo, y1, lo);
			buffer.addVertex(m, lo, y0, hi).addVertex(m, lo, y1, hi).addVertex(m, hi, y1, hi).addVertex(m, hi, y0, hi);
			buffer.addVertex(m, lo, y0, lo).addVertex(m, lo, y1, lo).addVertex(m, lo, y1, hi).addVertex(m, lo, y0, hi);
			buffer.addVertex(m, hi, y0, lo).addVertex(m, hi, y0, hi).addVertex(m, hi, y1, hi).addVertex(m, hi, y1, lo);
		}
		BufferUploader.drawWithShader(buffer.buildOrThrow());
		RenderSystem.enableCull();
		RenderSystem.colorMask(true, true, true, true);
		GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, read);
		GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, draw);
		final RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
		RenderSystem.viewport(0, 0, main.width, main.height);
	}

	/**
	 * Stretches the storm fog into the blur target by depth. The depth, its convention and inverse projection are the ones the
	 * storm fog pass was given this frame, so each low-resolution texel is read at the distance it marched to.
	 *
	 * @return false to leave the stretch to Simple Clouds' linear blit
	 */
	public static boolean upsampleStormFog(RenderTarget source)
	{
		if (upsampleFailed || source == null || !ForeverFixesConfig.enabled(ForeverFixesConfig.SC_STORM_UPSAMPLE))
		{
			return false;
		}
		final SimpleCloudsRenderer renderer = SimpleCloudsRenderer.getOptionalInstance().orElse(null);
		if (renderer == null || source != renderer.getStormFogTarget() || renderer.getBlurTarget() == null)
		{
			return false;
		}
		final PostChain storm = ((AccessorSimpleCloudsRenderer) (Object) renderer).foreverfixes$stormPostProcessing();
		if (storm == null)
		{
			return false;
		}
		final List<PostPass> passes = ((MixinPostChain) storm).simpleclouds$getPostPasses();
		if (passes.isEmpty())
		{
			return false;
		}
		final EffectInstance fog = passes.get(0).getEffect();
		final IntSupplier depth = ((AccessorEffectInstance) fog).foreverfixes$samplers().get("DepthSampler");
		final Uniform inverse = fog.getUniform("InverseWorldProjMat");
		if (depth == null || inverse == null || !ensureUpsample(source, renderer.getBlurTarget()))
		{
			return false;
		}
		final int depthTexture = depth.getAsInt();
		final EffectInstance effect = upsample.getEffect();
		effect.setSampler("DepthSampler", () -> depthTexture);
		effect.safeGetUniform("InverseProjMat").set(new Matrix4f().set(0, inverse.getFloatBuffer()));
		effect.safeGetUniform("DepthReverse").set(floatUniform(fog, "MicDepthReverse"));
		effect.safeGetUniform("DepthRange").set(floatUniform(fog, "MicDepthRange"));
		upsample.setOrthoMatrix(new Matrix4f().setOrtho(0.0F, upsampleOut.width, 0.0F, upsampleOut.height, 0.1F, 1000.0F));
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		upsample.process(0.0F);
		RenderSystem.depthMask(true);
		return true;
	}

	/** a uniform the storm fog shader declares with a default of 0 that the storm fog pass may not list */
	private static float floatUniform(EffectInstance effect, String name)
	{
		final Uniform uniform = effect.getUniform(name);
		return uniform != null && uniform.getFloatBuffer() != null ? uniform.getFloatBuffer().get(0) : 0.0F;
	}

	private static boolean ensureUpsample(RenderTarget in, RenderTarget out)
	{
		if (upsample != null && upsampleIn == in && upsampleOut == out)
		{
			return true;
		}
		closeUpsample();
		try
		{
			upsample = new PostPass(Minecraft.getInstance().getResourceManager(), UPSAMPLE_PROGRAM, in, out, false);
			upsampleIn = in;
			upsampleOut = out;
			return true;
		}
		catch (IOException | RuntimeException e)
		{
			upsampleFailed = true;
			ForeverFixes.LOGGER.error("Simple Clouds depth-aware storm fog: the shader could not be loaded, the storm fog is stretched linearly", e);
			return false;
		}
	}

	private static void closeUpsample()
	{
		if (upsample != null)
		{
			upsample.close();
		}
		upsample = null;
		upsampleIn = null;
		upsampleOut = null;
	}
}
