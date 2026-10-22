package com.diamondskybox.foreverfixes;

import net.neoforged.neoforge.common.ModConfigSpec;

/** config/foreverfixes-client.toml */
public final class ForeverFixesConfig
{
	private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

	public static final ModConfigSpec.BooleanValue MOBENDS_ARMOR_MODEL_API = B
			.comment("Mo' Bends 5.6.2 + Armor Model API 1.1.0: repair Mo' Bends' Armor Model API compat.",
					"Mo' Bends looks up TrimLayer.resolveSprite(TextureAtlas, ArmorTrim), which the API no longer has; the failed lookup",
					"disables the whole compat and RPG-series armors (Paladins, Wizards, Archers, Rogues, Armory) lose their textures.")
			.define("mobends.armorModelApiFix", true);

	public static final ModConfigSpec.BooleanValue MOBENDS_MCA_PLAYER_BREASTS = B
			.comment("Mo' Bends + MCA: draw MCA's breast parts on players Mo' Bends animates.",
					"Mo' Bends adds them back only for its villager mutators, never for the player model MCA gives the player.")
			.define("mobends.mcaPlayerBreasts", true);

	public static final ModConfigSpec.BooleanValue MOBENDS_MCA_DIAGNOSTICS = B
			.comment("Mo' Bends + MCA: log (once per model) which render path Mo' Bends takes for each MCA model; for the missing-hair hunt.")
			.define("mobends.mcaDiagnostics", true);

	public static final ModConfigSpec.BooleanValue WAVEY_NO_CROUCH_TILT = B
			.comment("Wavey Capes + Mo' Bends: leave out Wavey Capes' own crouch tilt for players Mo' Bends animates.",
					"Mo' Bends' crouching torso already leans and the cape hangs from that torso, so the cape would tilt twice.")
			.define("waveycapes.noCrouchTiltWhenMoBendsAnimates", true);

	public static final ModConfigSpec.BooleanValue WAVEY_FLAT_IN_FLIGHT = B
			.comment("Wavey Capes + Mo' Bends: when Mo' Bends tips the whole body (creative flight), lay the cape along the back.",
					"Wavey Capes simulates its cape for an upright body; drawn from a body that lies flat, the trailing cape stands off",
					"the back. Between flatStartDegrees and flatFullDegrees of body tilt the cape blends towards a straight hang along",
					"the torso (Wavey Capes' wind sway still plays).")
			.define("waveycapes.flatCapeWhenMoBendsTipsBody", true);

	public static final ModConfigSpec.DoubleValue WAVEY_FLAT_START = B
			.comment("Body tilt in degrees where the cape starts to lie along the back.")
			.defineInRange("waveycapes.flatStartDegrees", 20D, 0D, 90D);

	public static final ModConfigSpec.DoubleValue WAVEY_FLAT_FULL = B
			.comment("Body tilt in degrees where the cape lies fully along the back.")
			.defineInRange("waveycapes.flatFullDegrees", 70D, 1D, 180D);

	public static final ModConfigSpec.BooleanValue FAR_PLANE_RAISE = B
			.comment("Raise the far clip plane of the world view to at least farPlaneBlocks.",
					"Vanilla clips at 4 x render distance in every direction (512 blocks at 8 chunks), which cuts the tops off",
					"tall mountains in 2000-block-tall worlds. Fog is horizontal only, so the far plane is all that hides them.")
			.define("render.farPlaneRaise", true);

	public static final ModConfigSpec.DoubleValue FAR_PLANE_BLOCKS = B
			.comment("Minimum far clip distance in blocks.")
			.defineInRange("render.farPlaneBlocks", 4096D, 512D, 16384D);

	public static final ModConfigSpec.BooleanValue NO_VERTICAL_SECTION_CULL = B
			.comment("Sodium: do not cull chunk sections for being more than one render distance above or below the camera.",
					"Sodium's occlusion culler limits the vertical distance like the horizontal one (vanilla does not); in a 2096-block-tall",
					"world with a small render distance that hides the ground when flying high and makes terrain flicker across section",
					"boundaries. Only the vertical test is lifted; the horizontal render distance still applies.")
			.define("render.noVerticalSectionCull", true);

	public static final ModConfigSpec.BooleanValue TITLES_ABOVE_BUILD_HEIGHT = B
			.comment("Traveler's Titles: keep checking for dimension and biome titles while the player is above the build limit.",
					"Traveler's Titles skips its checks whenever the player's block is 'not loaded', which vanilla says of every position",
					"above the build height; Create: Cosmonautics lands players in a planet dimension at Y 19000, so no title appeared on",
					"arrival. The checks now use the player's position clamped into the build height.")
			.define("travelerstitles.titlesAboveBuildHeight", true);

	public static final ModConfigSpec.BooleanValue RAGDOLL_HIDE_SOURCE = B
			.comment("Sable: Ragdolls: do not draw a ragdolled player's normal body.",
					"The ragdoll mod hides the player it turned into a ragdoll by making them invisible, but Mo' Bends (with MCA) draws",
					"players on its own path and showed the normal body next to the ragdoll. Players riding a ragdoll seat are skipped.")
			.define("ragdoll.hideRagdolledPlayer", true);

	public static final ModConfigSpec.BooleanValue RAGDOLL_MCA_SKIN = B
			.comment("Sable: Ragdolls + MCA: draw a ragdoll of a player MCA draws as a villager in the MCA look only.",
					"The ragdoll draws each part in the Minecraft skin and then MCA's layers on top, so the head showed twice.")
			.define("ragdoll.mcaLookOnly", true);

	public static final ModConfigSpec.BooleanValue RAGDOLL_WAVEY_CAPE = B
			.comment("Sable: Ragdolls + Wavey Capes: leave the ragdoll's cape to Wavey Capes instead of also drawing a stiff one.")
			.define("ragdoll.waveyCapeOnly", true);

	public static final ModConfigSpec.BooleanValue PUNCHY_MCA_ARMS = B
			.comment("Punchy + MCA: draw MCA's arms in first person instead of Punchy's Minecraft-skin arms, for players MCA draws.")
			.define("mca.punchyMcaArms", true);

	public static final ModConfigSpec.BooleanValue RPG_BREAST_ARMOR = B
			.comment("MCA: female gender physics + Armor Model API: RPG-series chest armor on the breasts uses its own texture instead of the missing-texture purple.")
			.define("mca.rpgBreastArmor", true);

	public static final ModConfigSpec SPEC = B.build();

	private ForeverFixesConfig()
	{
	}

	/** Config values are read from render code, which can run before the config file is loaded; default to enabled then. */
	public static boolean enabled(ModConfigSpec.BooleanValue value)
	{
		return !SPEC.isLoaded() || value.get();
	}

	/** Same for numbers: the given default until the config file is loaded. */
	public static double value(ModConfigSpec.DoubleValue value, double fallback)
	{
		return SPEC.isLoaded() ? value.get() : fallback;
	}
}
