package com.diamondskybox.foreverfixes;

import net.minecraft.resources.ResourceLocation;
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

	public static final ModConfigSpec.DoubleValue CHROMEWARE_FIRST_PERSON_SPREAD = B
			.comment("Chromeware: first-person arms carrying implant hardware (Gorilla Arms, cyber arms, Mantis Blades, Projectile Launcher)",
					"are moved this far outward (blocks) per side. Chromeware draws them in vanilla's empty-hand pose, which angles both arms",
					"toward the screen centre; its bulky arms then meet in the middle. 0 = as Chromeware draws them.")
			.defineInRange("chromeware.firstPersonArmSpread", 0.18D, 0.0D, 1.0D);

	public static final ModConfigSpec.BooleanValue MOBENDS_MCA_PLAYER_BREASTS = B
			.comment("Mo' Bends + MCA: draw MCA's breast parts on players Mo' Bends animates.",
					"Mo' Bends adds them back only for its villager mutators, never for the player model MCA gives the player.")
			.define("mobends.mcaPlayerBreasts", true);

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

	public static final ModConfigSpec.BooleanValue RAGDOLL_MCA_BREASTS = B
			.comment("Sable: Ragdoll Corpse + MCA: female gender physics: give corpses of MCA players their breasts.",
					"The corpse is drawn on a stand-in doll, and the gender mod only looks up MCA bodies for players and villagers.")
			.define("ragdoll.mcaCorpseBreasts", true);

	public static final ModConfigSpec.BooleanValue PUNCHY_MCA_ARMS = B
			.comment("Punchy + MCA: draw MCA's arms in first person instead of Punchy's Minecraft-skin arms, for players MCA draws.")
			.define("mca.punchyMcaArms", true);

	public static final ModConfigSpec.BooleanValue MCA_WEREWOLF_FORM = B
			.comment("Werewolves + MCA: show the werewolf model of a player in a transformed werewolf form instead of nothing.",
					"MCA hides the vanilla model of the players it draws, which also hid the werewolf model. The MCA look is back in human form.")
			.define("mca.werewolfForm", true);

	public static final ModConfigSpec.BooleanValue BSC_GLASS_FLUSH = B
			.comment("Better Simple Clouds: draw entities, chests and beacon beams before it copies the depth for clouds seen through glass.",
					"They were still batched at that point, so clouds and storm fog were drawn over them wherever glass was behind them.")
			.define("bettersimpleclouds.flushBeforeGlass", true);

	public static final ModConfigSpec.BooleanValue KEEP_KEYBINDS = B
			.comment("Keep key bindings across restarts: options saves during mod loading (which wrote every mod key's default over the",
					"player's binding) are held back until the bindings are read, and the saved bindings of keys a mod adds later (PneumaticCraft's",
					"armor keys) stay in options.txt and are applied when the keys appear.")
			.define("keybinds.keepSaved", true);

	public static final ModConfigSpec.BooleanValue BSC_PARTICLES_NOT_GLASS = B
			.comment("Better Simple Clouds: particles (smoke) do not count as glass. Under Fast/Fancy graphics it took the finished depth as the",
					"glass, so every particle in front of the frame was 'glass', and the rain haze worked out what is behind it by dividing by",
					"the particle's estimated coverage, which turned faint smoke into inverted colours. The glass is the depth after translucent blocks.")
			.define("bettersimpleclouds.particlesNotGlass", true);

	public static final ModConfigSpec.BooleanValue SABLE_SHIP_RAIN = B
			.comment("Sable: rain, its splashes, Better Simple Clouds' downpour and its wet ground stop on ships instead of falling through",
					"them. They only knew the world's heightmap, which has no Sable ships in it. Each ship keeps the tops of its block columns,",
					"worked out again only when a block on it changes, and lays them over the world when it moves, only while it is under rain or snow.")
			.define("sable.shipsBlockRain", true);

	public static final ModConfigSpec.BooleanValue SABLE_SHIP_WET_GROUND = B
			.comment("Sable + Better Simple Clouds: wet decks keep their puddles and ripples in place on a moving ship, and the dry ground",
					"under a ship follows it. The wet ground's pattern and open-sky test were in world coordinates (and the sky test on a",
					"heightmap rebuilt once a second), so the water slid across a flying ship's deck. The nearest 8 ships' column tops and",
					"poses go to the shader. Needs sable.shipsBlockRain.")
			.define("sable.shipWetGroundInShipSpace", true);

	public static final ModConfigSpec.BooleanValue BSC_WET_ONLY_WHERE_IT_RAINS = B
			.comment("Better Simple Clouds: wet ground only where it rains. Its wet ground covered everything near a storm, also deserts,",
					"savannas and badlands, where no rain falls, and ground where it snows.")
			.define("bettersimpleclouds.wetGroundOnlyWhereItRains", true);

	public static final ModConfigSpec.ConfigValue<List<? extends String>> BSC_DRY_BIOMES = B
			.comment("Better Simple Clouds: biomes (ids, or #tags) whose ground never gets wet, in addition to those without rain.")
			.defineListAllowEmpty("bettersimpleclouds.dryBiomes", List.of(), () -> "",
					o -> o instanceof String s && ResourceLocation.tryParse(s.startsWith("#") ? s.substring(1) : s) != null);

	public static final ModConfigSpec.BooleanValue BSC_WET_GROUND_SEAMS = B
			.comment("Better Simple Clouds: wet ground without dry dashed lines along block edges. Its rain shader worked out which ground faces",
					"up from 2x2-pixel depth slopes, which tilt on the seam between two blocks; a copy of the shader rebuilds it from the",
					"neighbouring pixels on the same surface. Takes effect on a resource reload (F3+T).")
			.define("bettersimpleclouds.wetGroundSeamFix", true);

	public static final ModConfigSpec.BooleanValue BSC_BEACON_GLOW_DEPTH = B
			.comment("Better Simple Clouds: the storm fog and rain haze stop at a beacon beam's outer glow and at JourneyMap's waypoint beams.",
					"Neither writes depth, so the fog was worked out to whatever was behind the beam and laid over it (against the sky the",
					"waypoint beams vanished). Only the clouds' depth copies get the beams, not the game's.")
			.define("bettersimpleclouds.beaconGlowDepth", true);

	public static final ModConfigSpec.BooleanValue BSC_STORM_FOG_GLASS = B
			.comment("Better Simple Clouds: glass (and other translucent blocks) is drawn in front of the storm fog behind it. The storm fog",
					"looks through glass to what is behind it, then was laid over the glass too, so glass against a rain column vanished.",
					"Needs simpleclouds.depthAwareStormFog.")
			.define("bettersimpleclouds.glassInFrontOfStormFog", true);

	public static final ModConfigSpec.BooleanValue SC_STORM_BLUR_VANILLA = B
			.comment("Simple Clouds: blur the storm fog with a copy of vanilla's blur instead of minecraft:shaders/post/blur.json, which",
					"Exposure Expanded replaces with a blur that adds up alpha instead of averaging it: distant rain columns came out solid black.")
			.define("simpleclouds.vanillaStormBlur", true);

	public static final ModConfigSpec.BooleanValue SC_STORM_UPSAMPLE = B
			.comment("Simple Clouds: stretch the quarter-resolution storm fog to the screen by depth instead of linearly, so it stops at the",
					"edges of what is in front of it instead of bleeding over them in 4-pixel steps (leaves, window frames, beacon beams).")
			.define("simpleclouds.depthAwareStormFog", true);

	public static final ModConfigSpec.BooleanValue SC_STORM_FOG_EDGES = B
			.comment("Simple Clouds: smooth edges on distant rain columns. Its storm fog samples the rain farther apart the farther out it",
					"looks (about 57 blocks apart at 4000 blocks), each sample all in or all out, so a column's edge could only be 0, 40, 80 or",
					"100 % fogged and flickered in 4x4-pixel blocks as the clouds drifted, worst against the sky. Uses a copy of Better Simple",
					"Clouds' storm fog shader that offsets each pixel's samples and counts the part of a step that is in the rain. Takes",
					"effect on a resource reload (F3+T).")
			.define("simpleclouds.smoothStormFogEdges", true);

	public static final ModConfigSpec.BooleanValue MOBENDS_PARAGLIDER_POSE = B
			.comment("Mo' Bends + Paraglider 21.1: play Mo' Bends' paragliding pose (glider held above the head) while gliding.",
					"Mo' Bends looks for a Paraglider class that 21.1 no longer has, so the pose never played.")
			.define("paraglider.moBendsGlidePose", true);

	public static final ModConfigSpec.BooleanValue PUNCHY_PARAGLIDER_OVERHEAD = B
			.comment("Punchy + Paraglider: in first person, leave the open paraglider to vanilla rendering so it sits above the head",
					"as Paraglider places it, instead of in Punchy's hand position.")
			.define("paraglider.punchyGlideOverhead", true);

	public static final ModConfigSpec.BooleanValue PUNCHY_SKIP_MISSING_SLEEVES = B
			.comment("Punchy: no first-person armor sleeve for chest armor without a vanilla armor-layer texture.",
					"Armor that draws its own model (RPG series through Armor Model API) has none, and Punchy drew a black sleeve.")
			.define("punchy.skipMissingArmorSleeves", true);

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
			.comment("Mods whose inventory button is shown there, top to bottom: spell_atlas, fieldguide, solonion, doggytalents, chromeware.")
			.defineListAllowEmpty("betterinventory.modButtonOrder", List.of("spell_atlas", "fieldguide", "solonion", "doggytalents", "chromeware"), () -> "", o -> o instanceof String);

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

	public static final ModConfigSpec.BooleanValue HOARFROST_FOG_ONLY_UNDER_SNOW_CLOUDS = B
			.comment("Hoarfrost: its snow haze, sky greying and fog colour only come with snow from real weather (a snow cloud overhead or",
					"vanilla snowfall), scaled by the rain level, not with fair-weather flurries. Hoarfrost fogged for any snow at all, so a",
					"light flurry from a small cloud closed the view to a pale wall at about 240 blocks, on the LODs too. Flakes still fall.")
			.define("hoarfrost.fogOnlyUnderSnowClouds", true);

	public static final ModConfigSpec.BooleanValue SNOW_HAZE = B
			.comment("Petrichor + Hoarfrost: Petrichor's rain haze (its atmosphere pass: haze that thickens with distance and in low ground,",
					"drifting showers, the overcast deck) also covers snow, with Hoarfrost's snow visibility, and replaces Hoarfrost's flat",
					"distance fog while it does. Rain and snow drive the one haze, from the share of land around you where either falls, so",
					"walking from snow into rain no longer clears the view before the rain haze fades in. Needs Petrichor's atmosphere on.")
			.define("weather.snowHaze", true);

	public static final ModConfigSpec.DoubleValue WEATHER_FOG_BRIGHTNESS = B
			.comment("In rain and snow, the fog colour takes its brightness from the light open-sky ground gets (the game's own lightmap, with",
					"night, Simple Clouds' storm darkening and the brightness setting in it): this times that light, blended in by how hard it",
					"rains or snows. The fog colour came from the sky and was lightened for a snowy look, so a storm's haze, and the far chunks",
					"and LODs fully inside it, glowed against the dark terrain at night and darkened it by day. 0 leaves the fog colour alone.")
			.defineInRange("weather.fogBrightnessFromLight", 0.8D, 0.0D, 1.5D);

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
