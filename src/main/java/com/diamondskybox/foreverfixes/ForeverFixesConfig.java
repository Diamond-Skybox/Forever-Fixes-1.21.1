package com.diamondskybox.foreverfixes;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

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

	public static final ModConfigSpec.BooleanValue MOBENDS_PARAGLIDER_POSE = B
			.comment("Mo' Bends + Paraglider 21.1: play Mo' Bends' paragliding pose (glider held above the head) while gliding.",
					"Mo' Bends looks for a Paraglider class that 21.1 no longer has, so the pose never played.")
			.define("paraglider.moBendsGlidePose", true);

	public static final ModConfigSpec.BooleanValue PUNCHY_PARAGLIDER_OVERHEAD = B
			.comment("Punchy + Paraglider: in first person, leave the open paraglider to vanilla rendering so it sits above the head",
					"as Paraglider places it, instead of in Punchy's hand position.")
			.define("paraglider.punchyGlideOverhead", true);

	public static final ModConfigSpec.BooleanValue RPG_BREAST_ARMOR = B
			.comment("MCA: female gender physics + Armor Model API: RPG-series chest armor on the breasts uses its own texture instead of the missing-texture purple.")
			.define("mca.rpgBreastArmor", true);

	public static final ModConfigSpec.BooleanValue NO_HIGH_ALTITUDE_SILENCE = B
			.comment("Create: Cosmonautics: keep game sounds between Y 1000 and 2000 (it mutes everything there except its high wind,",
					"which in the tall JJThunder overworld means the mountain tops).")
			.define("cosmonautics.noHighAltitudeSilence", true);

	public static final ModConfigSpec.BooleanValue RAISE_SPACE_EFFECTS = B
			.comment("Create: Cosmonautics: in dimensions taller than Y 1000, start its edge-of-space effects (planet drawn beneath, space",
					"fog and black sky, daylight stars, high wind, altitude render distance) at spaceEffectsStartY instead of Y 1000,",
					"which in the tall JJThunder overworld is mountain terrain. The fade-ins keep their length (planet: 500 blocks).")
			.define("cosmonautics.raiseSpaceEffects", true);

	public static final ModConfigSpec.DoubleValue SPACE_EFFECTS_START_Y = B
			.comment("Y where those effects start in dimensions taller than Y 1000; 0 = the dimension's build top (Y 2032 in JJThunder).")
			.defineInRange("cosmonautics.spaceEffectsStartY", 0D, 0D, 19000D);

	public static final ModConfigSpec.BooleanValue THROTTLE_STAR_TEXTURE = B
			.comment("Create: Cosmonautics: rebuild the sun's animated plasma texture every starTextureIntervalTicks instead of every tick.",
					"Each rebuild runs warped fractal noise for every pixel on the render thread, at any altitude in any dimension that",
					"shows the universe sky (the overworld too); every tick it took about a quarter of render-thread time.")
			.define("cosmonautics.throttleStarTexture", true);

	public static final ModConfigSpec.DoubleValue STAR_TEXTURE_INTERVAL = B
			.comment("Game ticks between rebuilds (20 = once per second); 0 = build it once and never animate.")
			.defineInRange("cosmonautics.starTextureIntervalTicks", 20D, 0D, 1200D);

	public static final ModConfigSpec.BooleanValue COASTERS_SKIP_IDLE_FLUSH = B
			.comment("Simulated Coasters: skip its end-of-pass buffer flush when there are no carts, chain lifts or balloon tethers to draw.",
					"It flushed the shared world buffer every frame after block entities; with Iris that sorted and drew everything buffered",
					"so far a second time per frame, even with no coasters anywhere.")
			.define("simulatedcoasters.skipIdleBatchFlush", true);

	public static final ModConfigSpec.BooleanValue COASTERS_SKIP_REPEATED_CLEAR = B
			.comment("Simulated Coasters: clear the track placement preview outlines once, not every tick.",
					"Without a coaster track in hand it removed 4099 outline keys every client tick, although nothing had been drawn since.")
			.define("simulatedcoasters.skipRepeatedOutlineClear", true);

	public static final ModConfigSpec.BooleanValue NTGL_TICK_DRAWN_ONLY = B
			.comment("NTGL: tick a weapon's animations only while it is being drawn.",
					"NTGL keeps one animator per entity and view (hand, back, inventory, recipe viewer...) for every weapon ever drawn and ticks",
					"all of them for the rest of the session; animators not drawn for animatorIdleSeconds are skipped until drawn again.")
			.define("ntgl.tickOnlyDrawnAnimators", true);

	public static final ModConfigSpec.DoubleValue NTGL_IDLE_SECONDS = B
			.comment("Seconds without being drawn after which a weapon animator stops ticking.")
			.defineInRange("ntgl.animatorIdleSeconds", 5D, 1D, 600D);

	public static final ModConfigSpec.BooleanValue HEXEREI_CACHE_PAGE_IDS = B
			.comment("Hexerei: parse book page ids once instead of every client tick.",
					"Its book paint tick walks every page of every book 20 times a second and parsed each page's id from text each time.")
			.define("hexerei.cachePageIds", true);

	public static final ModConfigSpec.BooleanValue BI_MOD_BUTTONS = B
			.comment("Better Inventory: other mods' vanilla-inventory buttons, stacked in the gutter between the character and the item grid.")
			.define("betterinventory.modButtons", true);

	public static final ModConfigSpec.ConfigValue<List<? extends String>> BI_MOD_BUTTON_ORDER = B
			.comment("Mods whose inventory button is shown there, top to bottom: spell_atlas, fieldguide, solonion, doggytalents.")
			.defineListAllowEmpty("betterinventory.modButtonOrder", List.of("spell_atlas", "fieldguide", "solonion", "doggytalents"), () -> "", o -> o instanceof String);

	public static final ModConfigSpec.BooleanValue BI_MOD_PANELS = B
			.comment("Better Inventory: Spell Atlas's inventory panel and NTGL's chassis tabs, which are drawn on the vanilla inventory screen, also on Better Inventory's.")
			.define("betterinventory.modPanels", true);

	public static final ModConfigSpec.BooleanValue ASTRAL_ENCHANTMENT_DESCRIPTIONS = B
			.comment("Astral Sorcery + Enchantment Descriptions: keep the enchantment descriptions in tooltips (Astral's copy of the",
					"enchantment list it adds its constellation enchantments to dropped them).")
			.define("astralsorcery.enchantmentDescriptions", true);

	public static final ModConfigSpec.BooleanValue STOCKMARKET_AUCTION_HOUSE_TAB = B
			.comment("Create: Stock Market + Create: Auction House: an Auction House tab on the Stock Market screen, after Top Sellers;",
					"closing the Auction House opened from it goes back to Stock Market.")
			.define("stockmarket.auctionHouseTab", true);

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
