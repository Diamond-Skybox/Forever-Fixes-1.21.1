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
import dev.bettersimpleclouds.immersion.CloudGlass;
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
 * takes its place. The storm fog and the rain haze, which marched through the particles to what is behind them and covered
 * them, get depths with the particles at their own distance (the storm fog's still without glass, so rain shows behind
 * windows).</li>
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
	private static final String DEPTH_PROGRAM = ForeverFixes.MOD_ID + ":storm_depth";
	private static final int MAX_BEAMS = 512;
	private static final int CHECKED_BLITS = 8;

	/** vanilla beacon beams, drawn before Better Simple Clouds' snapshot */
	private static final List<Beam> beams = new ArrayList<>();
	/** JourneyMap waypoint beams, drawn after the translucent blocks and wholly translucent: no depth at all */
	private static final List<Beam> waypointBeams = new ArrayList<>();
	/** Better Simple Clouds took its depth snapshot this frame */
	private static boolean captured;
	/** the depth after the translucent blocks, before the translucent particles */
	private static RenderTarget glass;
	private static boolean glassReady;
	private static boolean blitRefused;
	private static int checkedBlits;
	/** Better Simple Clouds' snapshot from before the translucent blocks (the beacon glow drawn into it) */
	private static RenderTarget opaque;
	/** the storm fog's depth: the snapshot before the translucent blocks with the translucent particles in front of it */
	private static RenderTarget fogDepth;
	/** the rain haze's depth: the finished depth with the beacon glow */
	private static RenderTarget hazeDepth;
	private static boolean depthsReady;
	private static EffectInstance depthEffect;
	private static boolean depthEffectFailed;
	/** the storm fog pass or the rain haze is being set up or drawn: Better Simple Clouds' depth is the fog depth */
	private static boolean inStormFog;
	private static boolean inRainHazeSetup;
	/** the rain haze pass is running: it reads the haze depth as the main depth */
	private static boolean inRainHaze;
	private static boolean loggedBeams;
	private static boolean loggedWaypointBeams;
	private static PostPass upsample;
	private static RenderTarget upsampleIn;
	private static RenderTarget upsampleOut;
	private static boolean upsampleFailed;

	/** an upright prism: four corners (x, z pairs, in order around it) from bottom to top, in the pose it was drawn with */
	private record Beam(Matrix4f pose, float bottom, float top, float[] corners)
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
			waypointBeams.clear();
			captured = false;
			glassReady = false;
			depthsReady = false;
		}
		else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS && captured
				&& ForeverFixesConfig.enabled(ForeverFixesConfig.BSC_PARTICLES_NOT_GLASS) && !IrisBridge.shadersRunning()
				&& Minecraft.getInstance().levelRenderer.getTranslucentTarget() == null)
		{
			snapshotGlass();
		}
		else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES && glassReady && opaque != null)
		{
			makeDepths();
		}
	}

	/** a beacon beam segment as vanilla draws it: the glow is a box of half-width glowRadius around the block's centre column */
	public static void recordBeam(PoseStack poseStack, int yOffset, int height, float glowRadius)
	{
		if (beams.size() < MAX_BEAMS && ForeverFixesConfig.enabled(ForeverFixesConfig.BSC_BEACON_GLOW_DEPTH))
		{
			final float lo = 0.5F - glowRadius;
			final float hi = 0.5F + glowRadius;
			beams.add(new Beam(new Matrix4f(poseStack.last().pose()), Math.min(yOffset, yOffset + height), Math.max(yOffset, yOffset + height),
					new float[] { lo, lo, hi, lo, hi, hi, lo, hi }));
		}
	}

	/**
	 * a JourneyMap waypoint beam segment as it draws it: an inner diamond and an outer square around the pose's origin, each
	 * turning with the game time unless it is set to stand still
	 */
	public static void recordWaypointBeam(PoseStack poseStack, float partialTick, long gameTime, int yOffset, int height, float beamRadius,
			float glowRadius, boolean staticInner, boolean rotatingOuter)
	{
		if (waypointBeams.size() + 2 > MAX_BEAMS || !ForeverFixesConfig.enabled(ForeverFixesConfig.BSC_BEACON_GLOW_DEPTH))
		{
			return;
		}
		final Matrix4f pose = poseStack.last().pose();
		final float turn = (float) Math.toRadians(((float) Math.floorMod(gameTime, 40L) + partialTick) * 2.25F - 45.0F);
		final float bottom = Math.min(yOffset, yOffset + height);
		final float top = Math.max(yOffset, yOffset + height);
		final float r = beamRadius;
		final float g = glowRadius;
		waypointBeams.add(new Beam(staticInner ? new Matrix4f(pose) : new Matrix4f(pose).rotateY(turn), bottom, top,
				new float[] { 0.0F, r, r, 0.0F, 0.0F, -r, -r, 0.0F }));
		waypointBeams.add(new Beam(rotatingOuter ? new Matrix4f(pose).rotateY(turn) : new Matrix4f(pose), bottom, top,
				new float[] { -g, -g, g, -g, g, g, -g, g }));
	}

	/** Better Simple Clouds copied the main depth into its snapshot */
	public static void afterCapture(RenderTarget snapshot)
	{
		captured = true;
		opaque = snapshot;
		drawBeamDepth(snapshot, beams);
		if (!loggedBeams && !beams.isEmpty())
		{
			loggedBeams = true;
			ForeverFixes.LOGGER.info("Better Simple Clouds: beacon glow drawn into the storm fog's depth ({} beam segments this frame)", beams.size());
		}
	}

	/** the glass depth for Better Simple Clouds, or -1 to leave it its own */
	public static int glassDepthTexture()
	{
		return glassReady ? glass.getDepthTextureId() : -1;
	}

	public static void setInStormFog(boolean running)
	{
		inStormFog = running;
	}

	public static void setInRainHazeSetup(boolean running)
	{
		inRainHazeSetup = running;
	}

	public static void setInRainHaze(boolean running)
	{
		inRainHaze = running;
	}

	/** Better Simple Clouds' depth for the storm fog and the rain haze, or -1 to leave it its own */
	public static int fogDepthTexture()
	{
		return depthsReady && (inStormFog || inRainHazeSetup) ? fogDepth.getDepthTextureId() : -1;
	}

	/** the depth texture the rain haze reads in place of the main one, or -1 */
	public static int rainHazeDepth(RenderTarget target)
	{
		return inRainHaze && depthsReady && target == Minecraft.getInstance().getMainRenderTarget() ? hazeDepth.getDepthTextureId() : -1;
	}

	public static ResourceLocation stormBlurChain(ResourceLocation chain)
	{
		return VANILLA_BLUR.equals(chain) && ForeverFixesConfig.enabled(ForeverFixesConfig.SC_STORM_BLUR_VANILLA) ? STORM_BLUR : chain;
	}

	public static void onResourceReload()
	{
		closeUpsample();
		upsampleFailed = false;
		if (depthEffect != null)
		{
			depthEffect.close();
		}
		depthEffect = null;
		depthEffectFailed = false;
	}

	/** the storm fog's and the rain haze's depths, from the finished depth once the translucent particles are in it */
	private static void makeDepths()
	{
		final Minecraft mc = Minecraft.getInstance();
		final RenderTarget main = mc.getMainRenderTarget();
		if (depthEffectFailed || main == null || main.width <= 0 || main.height <= 0)
		{
			return;
		}
		if (depthEffect == null)
		{
			try
			{
				depthEffect = new EffectInstance(mc.getResourceManager(), DEPTH_PROGRAM);
			}
			catch (IOException | RuntimeException e)
			{
				depthEffectFailed = true;
				ForeverFixes.LOGGER.error("Better Simple Clouds storm fog depth: the shader could not be loaded, particles stay behind the storm fog", e);
				return;
			}
		}
		fogDepth = sized(fogDepth, main);
		hazeDepth = sized(hazeDepth, main);
		final int read = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
		final int draw = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
		final int live = main.getDepthTextureId();
		final int glassDepth = glass.getDepthTextureId();
		final int opaqueDepth = opaque.getDepthTextureId();
		depthEffect.setSampler("LiveDepth", () -> live);
		depthEffect.setSampler("GlassDepth", () -> glassDepth);
		depthEffect.setSampler("OpaqueDepth", () -> opaqueDepth);
		depthEffect.safeGetUniform("ProjMat").set(new Matrix4f().setOrtho(0.0F, main.width, 0.0F, main.height, 0.1F, 1000.0F));
		depthEffect.safeGetUniform("OutSize").set((float) main.width, (float) main.height);
		RenderSystem.colorMask(false, false, false, false);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL11.GL_ALWAYS);
		RenderSystem.depthMask(true);
		writeDepth(fogDepth, 0.0F, main);
		writeDepth(hazeDepth, 1.0F, main);
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		RenderSystem.colorMask(true, true, true, true);
		GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, read);
		GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, draw);
		RenderSystem.viewport(0, 0, main.width, main.height);
		drawBeamDepth(fogDepth, waypointBeams);
		drawBeamDepth(hazeDepth, waypointBeams);
		depthsReady = true;
		if (!loggedWaypointBeams && !waypointBeams.isEmpty())
		{
			loggedWaypointBeams = true;
			ForeverFixes.LOGGER.info("Better Simple Clouds: JourneyMap waypoint beams drawn into the storm fog's depth ({} this frame)", waypointBeams.size() / 2);
		}
	}

	private static void writeDepth(RenderTarget target, float mode, RenderTarget main)
	{
		GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, target.frameBufferId);
		RenderSystem.viewport(0, 0, target.width, target.height);
		depthEffect.safeGetUniform("Mode").set(mode);
		depthEffect.apply();
		final BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
		buffer.addVertex(0.0F, 0.0F, 500.0F);
		buffer.addVertex(main.width, 0.0F, 500.0F);
		buffer.addVertex(main.width, main.height, 500.0F);
		buffer.addVertex(0.0F, main.height, 500.0F);
		BufferUploader.draw(buffer.buildOrThrow());
		depthEffect.clear();
	}

	private static RenderTarget sized(RenderTarget target, RenderTarget main)
	{
		if (target == null)
		{
			final RenderTarget made = new CloudRenderTarget(main.width, main.height, Minecraft.ON_OSX, false);
			made.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
			return made;
		}
		if (target.width != main.width || target.height != main.height)
		{
			target.resize(main.width, main.height, Minecraft.ON_OSX);
		}
		return target;
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
		drawBeamDepth(glass, beams);
		glassReady = true;
	}

	/** draws the beams into the depth of the target, nothing into its colour */
	private static void drawBeamDepth(RenderTarget target, List<Beam> list)
	{
		if (list.isEmpty() || target == null || IrisBridge.shadersRunning())
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
		for (Beam beam : list)
		{
			final Matrix4f m = beam.pose();
			final float[] c = beam.corners();
			for (int i = 0; i < 4; i++)
			{
				final int j = (i + 1) % 4;
				buffer.addVertex(m, c[2 * i], beam.bottom(), c[2 * i + 1]).addVertex(m, c[2 * j], beam.bottom(), c[2 * j + 1])
						.addVertex(m, c[2 * j], beam.top(), c[2 * j + 1]).addVertex(m, c[2 * i], beam.top(), c[2 * i + 1]);
			}
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
		// the storm fog's own depth: the sampler asks Better Simple Clouds, which answers with the fog depth during the storm fog
		inStormFog = true;
		final int depthTexture = depth.getAsInt();
		inStormFog = false;
		final EffectInstance effect = upsample.getEffect();
		effect.setSampler("DepthSampler", () -> depthTexture);
		effect.safeGetUniform("InverseProjMat").set(new Matrix4f().set(0, inverse.getFloatBuffer()));
		effect.safeGetUniform("DepthReverse").set(floatUniform(fog, "MicDepthReverse"));
		effect.safeGetUniform("DepthRange").set(floatUniform(fog, "MicDepthRange"));
		// glass in front of the storm fog: Better Simple Clouds' filter, worked out once a frame against the finished frame
		final boolean glassOn = ForeverFixesConfig.enabled(ForeverFixesConfig.BSC_STORM_FOG_GLASS) && CloudGlass.prepareFilter();
		final int filter = glassOn ? CloudGlass.filterTexture() : 0;
		effect.setSampler("GlassFilter", () -> filter);
		effect.safeGetUniform("GlassOn").set(glassOn ? 1.0F : 0.0F);
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
