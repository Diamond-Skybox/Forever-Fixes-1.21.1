package com.diamondskybox.foreverfixes;

import goblinbob.mobends.compat.McaCompat;
import goblinbob.mobends.standard.client.model.adaptive.AdaptiveHumanoidGeometry;
import net.minecraft.client.model.HumanoidModel;

import java.util.HashSet;
import java.util.Set;

/**
 * One log line per MCA model instance and situation about the path Mo' Bends takes for it (its own render, overlay geometry,
 * or MCA's own renderToBuffer), plus part visibility and which overlay meshes exist. For finding why MCA's hair layer draws
 * nothing on Mo' Bends-animated players; switched off with mobends.mcaDiagnostics. Loaded only through the mobends mixins.
 */
public final class McaHairDiagnostics
{
	private static final Set<String> SEEN = new HashSet<>();
	private static boolean pendingMainRender;

	private McaHairDiagnostics()
	{
	}

	private static boolean on()
	{
		return ForeverFixesConfig.enabled(ForeverFixesConfig.MOBENDS_MCA_DIAGNOSTICS);
	}

	private static boolean first(String key)
	{
		return SEEN.size() < 256 && SEEN.add(key);
	}

	private static String id(Object model)
	{
		return model.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(model));
	}

	/** McaCompat.renderMutatedInstead, before it runs: remember whether this is the main model render (it ends it when it draws). */
	public static void intercepting(boolean mainRender)
	{
		pendingMainRender = mainRender;
	}

	/** McaCompat.renderMutatedInstead, after it ran. */
	public static void intercepted(Object model, boolean handled)
	{
		if (!on() || !(model instanceof HumanoidModel<?> humanoid) || !McaCompat.isMcaModel(humanoid))
		{
			return;
		}
		if (first("i|" + System.identityHashCode(model) + "|" + pendingMainRender + "|" + handled))
		{
			ForeverFixes.LOGGER.info("MCA diag: {} in {} -> {}", id(model), pendingMainRender ? "the main model render" : "a layer render",
					handled ? "drawn by Mo' Bends" : "left to MCA's own renderToBuffer");
		}
	}

	/** BipedMutator.renderOverlayGeometry, before it draws. */
	public static void overlay(HumanoidModel<?> model, AdaptiveHumanoidGeometry geometry, boolean isCurrentVanillaModel)
	{
		if (!on() || model == null || !McaCompat.isMcaModel(model))
		{
			return;
		}
		if (first("o|" + System.identityHashCode(model) + "|" + (geometry != null)))
		{
			ForeverFixes.LOGGER.info("MCA diag: overlay {} currentVanillaModel={} geometry={} visible(head={} hat={} body={}) meshes(head={} hat={} body={})",
					id(model), isCurrentVanillaModel, geometry == null ? "not built yet" : "built",
					model.head.visible, model.hat.visible, model.body.visible,
					geometry != null && geometry.headMesh != null, geometry != null && geometry.hatMesh != null,
					geometry != null && geometry.bodyMesh != null);
		}
	}
}
