package dev.merlin.oldfog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * JSON config: .minecraft/config/merlins_old_fog.json
 * Hot-reloaded: edit the file, the in-game menu or use /oldfog - changes apply immediately.
 */
public class OldFogConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("merlins_old_fog.json");

	public static final String DEFAULT_COLOR = "#C0D8FF"; // 1.8.9 daytime fog colour

	// ---- options (defaults = Minecraft 1.8.9) ----
	public boolean enabled = true;

	/** Use a fixed fog distance in blocks instead of the render distance (same look at any render distance). */
	public boolean fixedDistance = true;
	public float fixedDistanceBlocks = 64.0F;

	public float overworldStart = 0.4F;
	public float overworldEnd = 1.0F;

	public boolean applyInNether = true;
	public float netherStart = 0.05F;
	public float netherEnd = 0.5F;
	public float netherMaxDistance = 192.0F;

	/** Fog opacity 0..1 (1 = normal, lower = see-through fog). Only for normal air fog. */
	public float fogOpacity = 1.0F;

	public boolean disableEnvironmentalFog = true;
	public boolean matchSkyFog = true;

	/** Override the fog colour with fogColor ("#RRGGBB"). */
	public boolean customFogColor = false;
	public String fogColor = DEFAULT_COLOR;
	/** Also apply the custom colour in the Nether. */
	public boolean customColorInNether = false;

	/** "dense" = thick pale fog (default look), "classic" = exact 1.8.9 values. */
	public void applyPreset(String name) {
		OldFogConfig d = new OldFogConfig();
		enabled = true;
		disableEnvironmentalFog = true;
		matchSkyFog = true;
		if (name.equals("classic")) {
			fixedDistance = false;
			overworldStart = 0.75F;
			overworldEnd = 1.0F;
		} else { // dense
			fixedDistance = true;
			fixedDistanceBlocks = d.fixedDistanceBlocks;
			overworldStart = d.overworldStart;
			overworldEnd = d.overworldEnd;
		}
	}

	public int fogColorRgb() {
		try {
			return Integer.parseInt(fogColor.replace("#", "").trim(), 16) & 0xFFFFFF;
		} catch (Exception e) {
			return 0xC0D8FF;
		}
	}

	public void setFogColorRgb(int rgb) {
		fogColor = String.format(Locale.ROOT, "#%06X", rgb & 0xFFFFFF);
	}

	// ---- loading / saving ----
	private static OldFogConfig instance = new OldFogConfig();
	private static long lastModified = -1;
	private static long lastCheck = 0;

	public static OldFogConfig get() {
		long now = System.currentTimeMillis();
		if (now - lastCheck > 1000) {
			lastCheck = now;
			try {
				long mod = Files.exists(PATH) ? Files.getLastModifiedTime(PATH).toMillis() : -2;
				if (mod != lastModified) {
					load();
				}
			} catch (IOException e) {
				MerlinsOldFog.LOGGER.warn("Could not check config file", e);
			}
		}
		return instance;
	}

	public static synchronized void load() {
		OldFogConfig cfg = new OldFogConfig();
		try {
			if (Files.exists(PATH)) {
				try (Reader r = Files.newBufferedReader(PATH)) {
					OldFogConfig read = GSON.fromJson(r, OldFogConfig.class);
					if (read != null) cfg = read;
				}
			}
			cfg.sanitize();
		} catch (Exception e) {
			MerlinsOldFog.LOGGER.error("Invalid config, using defaults", e);
			cfg = new OldFogConfig();
		}
		instance = cfg;
		save(); // rewrite: adds new options, fixes values
	}

	/** Call after changing values (menu / commands). */
	public static synchronized void save() {
		try (Writer w = Files.newBufferedWriter(PATH)) {
			GSON.toJson(instance, w);
		} catch (IOException e) {
			MerlinsOldFog.LOGGER.error("Could not save config", e);
			return;
		}
		try {
			lastModified = Files.getLastModifiedTime(PATH).toMillis();
		} catch (IOException ignored) {
		}
	}

	public static synchronized void reset() {
		instance = new OldFogConfig();
		save();
	}

	public void sanitize() {
		fogOpacity = clamp(fogOpacity, 0F, 1F);
		fixedDistanceBlocks = clamp(fixedDistanceBlocks, 16F, 1024F);
		overworldStart = clamp(overworldStart, 0F, 10F);
		overworldEnd = Math.max(clamp(overworldEnd, 0.01F, 10F), overworldStart + 0.01F);
		netherStart = clamp(netherStart, 0F, 10F);
		netherEnd = Math.max(clamp(netherEnd, 0.01F, 10F), netherStart + 0.01F);
		netherMaxDistance = clamp(netherMaxDistance, 16F, 4096F);
		setFogColorRgb(fogColor == null ? 0xC0D8FF : fogColorRgb());
	}

	private static float clamp(float v, float min, float max) {
		return Float.isNaN(v) ? min : Math.max(min, Math.min(max, v));
	}
}
