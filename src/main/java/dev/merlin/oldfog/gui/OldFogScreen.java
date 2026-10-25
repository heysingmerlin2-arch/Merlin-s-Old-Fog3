package dev.merlin.oldfog.gui;

import dev.merlin.oldfog.OldFogConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * In-game settings (opened from Mod Menu or with /oldfog).
 * Built only from vanilla widgets (Button) so it needs no config library.
 * Number rows: [<<] [<] [value = click to reset] [>] [>>]
 */
public class OldFogScreen extends Screen {
	private static final String[] PAGES = {"Allgemein", "Nether", "Farbe", "Erweitert"};

	private final Screen parent;
	private final int page;
	private final List<int[]> labelY = new ArrayList<>();       // {y}
	private final List<String> labelText = new ArrayList<>();

	public OldFogScreen(Screen parent, int page) {
		super(Component.literal("Merlin's Old Fog"));
		this.parent = parent;
		this.page = page;
	}

	@Override
	protected void init() {
		labelY.clear();
		labelText.clear();
		OldFogConfig c = OldFogConfig.get();
		int cx = this.width / 2;

		// page tabs
		for (int i = 0; i < PAGES.length; i++) {
			final int p = i;
			Button tab = Button.builder(Component.literal(PAGES[i]),
					b -> this.minecraft.gui.setScreen(new OldFogScreen(parent, p)))
				.bounds(cx - 144 + i * 72, 26, 68, 20).build();
			tab.active = (i != page);
			this.addRenderableWidget(tab);
		}

		int y = 58;
		final int step = 24;
		switch (page) {
			case 0 -> {
				boolRow(y, "Old Fog aktiv", () -> c.enabled, v -> c.enabled = v); y += step;
				boolRow(y, "Feste Distanz (statt Sichtweite)", () -> c.fixedDistance, v -> c.fixedDistance = v); y += step;
				floatRow(y, "Nebel-Ende (Blöcke)", () -> c.fixedDistanceBlocks, v -> c.fixedDistanceBlocks = v, 64F, 4F, 16F, 16F, 1024F, 0); y += step;
				floatRow(y, "Start (Anteil)", () -> c.overworldStart, v -> c.overworldStart = v, 0.4F, 0.01F, 0.1F, 0F, 10F, 2); y += step;
				floatRow(y, "Ende (Anteil)", () -> c.overworldEnd, v -> c.overworldEnd = v, 1.0F, 0.01F, 0.1F, 0.01F, 10F, 2); y += step + 8;
				this.addRenderableWidget(Button.builder(Component.literal("Preset: Dicht"), b -> {
					c.applyPreset("dense");
					OldFogConfig.save();
					this.minecraft.gui.setScreen(new OldFogScreen(parent, 0));
				}).bounds(cx - 155, y, 150, 20).build());
				this.addRenderableWidget(Button.builder(Component.literal("Preset: Klassisch 1.8.9"), b -> {
					c.applyPreset("classic");
					OldFogConfig.save();
					this.minecraft.gui.setScreen(new OldFogScreen(parent, 0));
				}).bounds(cx + 5, y, 150, 20).build());
			}
			case 1 -> {
				boolRow(y, "Old Fog im Nether", () -> c.applyInNether, v -> c.applyInNether = v); y += step;
				floatRow(y, "Nether Start", () -> c.netherStart, v -> c.netherStart = v, 0.05F, 0.01F, 0.1F, 0F, 10F, 2); y += step;
				floatRow(y, "Nether Ende", () -> c.netherEnd, v -> c.netherEnd = v, 0.5F, 0.01F, 0.1F, 0.01F, 10F, 2); y += step;
				floatRow(y, "Nether Max-Distanz (Blöcke)", () -> c.netherMaxDistance, v -> c.netherMaxDistance = v, 192F, 8F, 32F, 16F, 4096F, 0);
			}
			case 3 -> {
				floatRow(y, "Deckkraft (0-1)", () -> c.fogOpacity, v -> c.fogOpacity = v, 1.0F, 0.01F, 0.1F, 0F, 1F, 2); y += step;
				boolRow(y, "Moderner Dunst entfernen", () -> c.disableEnvironmentalFog, v -> c.disableEnvironmentalFog = v); y += step;
				boolRow(y, "Himmelsnebel angleichen", () -> c.matchSkyFog, v -> c.matchSkyFog = v);
			}
			default -> {
				boolRow(y, "Eigene Nebelfarbe", () -> c.customFogColor, v -> c.customFogColor = v); y += step;
				boolRow(y, "Auch im Nether", () -> c.customColorInNether, v -> c.customColorInNether = v); y += step;
				floatRow(y, "Rot", () -> (float) ((c.fogColorRgb() >> 16) & 255),
					v -> c.setFogColorRgb((c.fogColorRgb() & 0x00FFFF) | (v.intValue() << 16)), 192F, 1F, 16F, 0F, 255F, 0); y += step;
				floatRow(y, "Grün", () -> (float) ((c.fogColorRgb() >> 8) & 255),
					v -> c.setFogColorRgb((c.fogColorRgb() & 0xFF00FF) | (v.intValue() << 8)), 216F, 1F, 16F, 0F, 255F, 0); y += step;
				floatRow(y, "Blau", () -> (float) (c.fogColorRgb() & 255),
					v -> c.setFogColorRgb((c.fogColorRgb() & 0xFFFF00) | v.intValue()), 255F, 1F, 16F, 0F, 255F, 0); y += step + 14;

				// presets
				String[] names = {"1.8.9", "Weiß", "Grau", "Lila", "Rosa", "Schwarz"};
				int[] cols = {0xC0D8FF, 0xFFFFFF, 0x9A9A9A, 0x9B30FF, 0xFFB6D9, 0x000000};
				for (int i = 0; i < names.length; i++) {
					final int rgb = cols[i];
					Button b = Button.builder(Component.literal(names[i]), btn -> {
						c.setFogColorRgb(rgb);
						c.customFogColor = true;
						OldFogConfig.save();
						this.minecraft.gui.setScreen(new OldFogScreen(parent, 2));
					}).bounds(cx - 156 + i * 53, y, 50, 20).build();
					this.addRenderableWidget(b);
				}
			}
		}

		// bottom buttons
		this.addRenderableWidget(Button.builder(Component.literal("Alles zurücksetzen"), b -> {
			OldFogConfig.reset();
			this.minecraft.gui.setScreen(new OldFogScreen(parent, page));
		}).bounds(cx - 155, this.height - 28, 150, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("Fertig"), b -> this.onClose())
			.bounds(cx + 5, this.height - 28, 150, 20).build());
	}

	private void boolRow(int y, String label, Supplier<Boolean> get, Consumer<Boolean> set) {
		addLabel(y, label);
		Button b = Button.builder(boolText(get.get()), btn -> {
			set.accept(!get.get());
			OldFogConfig.get().sanitize();
			OldFogConfig.save();
			btn.setMessage(boolText(get.get()));
		}).bounds(this.width / 2 + 10, y, 148, 20).build();
		this.addRenderableWidget(b);
	}

	private void floatRow(int y, String label, Supplier<Float> get, Consumer<Float> set,
						  float def, float small, float big, float min, float max, int decimals) {
		addLabel(y, label);
		int x = this.width / 2 + 10;
		Button value = Button.builder(Component.literal(fmt(get.get(), decimals)), btn -> {
			set.accept(def);              // click value = reset this option
			afterChange(btn, get, decimals);
		}).bounds(x + 44, y, 60, 20).build();

		this.addRenderableWidget(arrow("<<", x, y, () -> change(get, set, -big, min, max), value, get, decimals));
		this.addRenderableWidget(arrow("<", x + 22, y, () -> change(get, set, -small, min, max), value, get, decimals));
		this.addRenderableWidget(value);
		this.addRenderableWidget(arrow(">", x + 106, y, () -> change(get, set, small, min, max), value, get, decimals));
		this.addRenderableWidget(arrow(">>", x + 128, y, () -> change(get, set, big, min, max), value, get, decimals));
	}

	private Button arrow(String text, int x, int y, Runnable action, Button value, Supplier<Float> get, int decimals) {
		return Button.builder(Component.literal(text), btn -> {
			action.run();
			afterChange(value, get, decimals);
		}).bounds(x, y, 20, 20).build();
	}

	private static void change(Supplier<Float> get, Consumer<Float> set, float delta, float min, float max) {
		float v = get.get() + delta;
		set.accept(Math.max(min, Math.min(max, v)));
	}

	private static void afterChange(Button valueButton, Supplier<Float> get, int decimals) {
		OldFogConfig.get().sanitize();
		OldFogConfig.save();
		valueButton.setMessage(Component.literal(fmt(get.get(), decimals)));
	}

	private void addLabel(int y, String text) {
		labelY.add(new int[]{y});
		labelText.add(text);
	}

	private static String fmt(float v, int decimals) {
		return String.format(Locale.ROOT, "%." + decimals + "f", v);
	}

	private static Component boolText(boolean on) {
		return Component.literal(on ? "AN" : "AUS");
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		int cx = this.width / 2;

		String title = "Merlin's Old Fog";
		graphics.text(this.font, title, cx - this.font.width(title) / 2, 10, 0xFFFFFFFF, true);

		for (int i = 0; i < labelText.size(); i++) {
			graphics.text(this.font, labelText.get(i), cx - 160, labelY.get(i)[0] + 6, 0xFFFFFFFF, true);
		}

		if (page == 2) {
			OldFogConfig c = OldFogConfig.get();
			String preview = "Vorschau: " + c.fogColor + (c.customFogColor ? "" : "  (aus)");
			graphics.text(this.font, preview, cx - 160, 180, 0xFF000000 | c.fogColorRgb(), true);
		}
	}

	@Override
	public void onClose() {
		OldFogConfig.save();
		this.minecraft.gui.setScreen(this.parent);
	}
}
