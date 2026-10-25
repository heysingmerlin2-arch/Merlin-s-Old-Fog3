package dev.merlin.oldfog;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.merlin.oldfog.gui.OldFogScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Client-side commands: /oldfog ... */
public final class OldFogCommands {
	private OldFogCommands() {}

	public static void register(CommandDispatcher<FabricClientCommandSource> d) {
		d.register(ClientCommands.literal("oldfog")
			.executes(ctx -> openGui())
			.then(ClientCommands.literal("gui").executes(ctx -> openGui()))
			.then(ClientCommands.literal("on").executes(ctx -> enable(ctx, true)))
			.then(ClientCommands.literal("off").executes(ctx -> enable(ctx, false)))
			.then(ClientCommands.literal("toggle").executes(ctx -> enable(ctx, !OldFogConfig.get().enabled)))
			.then(ClientCommands.literal("reload").executes(ctx -> {
				OldFogConfig.load();
				msg(ctx, "Config neu geladen.");
				return 1;
			}))
			.then(ClientCommands.literal("reset").executes(ctx -> {
				OldFogConfig.reset();
				msg(ctx, "Alle Einstellungen auf 1.8.9-Standard zurückgesetzt.");
				return 1;
			}))
			.then(ClientCommands.literal("preset")
				.then(ClientCommands.literal("dense").executes(ctx -> preset(ctx, "dense")))
				.then(ClientCommands.literal("classic").executes(ctx -> preset(ctx, "classic"))))
			.then(ClientCommands.literal("distance")
				.then(ClientCommands.literal("off").executes(ctx -> {
					OldFogConfig.get().fixedDistance = false;
					OldFogConfig.save();
					msg(ctx, "Feste Nebel-Distanz: AUS (Sichtweite wird benutzt)");
					return 1;
				}))
				.then(ClientCommands.argument("blocks", FloatArgumentType.floatArg(16F, 1024F)).executes(ctx -> {
					OldFogConfig c = OldFogConfig.get();
					c.fixedDistance = true;
					c.fixedDistanceBlocks = FloatArgumentType.getFloat(ctx, "blocks");
					OldFogConfig.save();
					msg(ctx, String.format(Locale.ROOT, "Feste Nebel-Distanz: %.0f Blöcke", c.fixedDistanceBlocks));
					return 1;
				})))
			.then(ClientCommands.literal("opacity")
				.then(ClientCommands.argument("value", FloatArgumentType.floatArg(0F, 1F)).executes(ctx -> {
					OldFogConfig c = OldFogConfig.get();
					c.fogOpacity = FloatArgumentType.getFloat(ctx, "value");
					OldFogConfig.save();
					msg(ctx, String.format(Locale.ROOT, "Deckkraft: %.2f", c.fogOpacity));
					return 1;
				})))
			.then(ClientCommands.literal("status").executes(OldFogCommands::status))
			.then(ClientCommands.literal("overworld")
				.then(ClientCommands.argument("start", FloatArgumentType.floatArg(0F, 10F))
					.then(ClientCommands.argument("end", FloatArgumentType.floatArg(0.01F, 10F))
						.executes(ctx -> {
							OldFogConfig c = OldFogConfig.get();
							c.overworldStart = FloatArgumentType.getFloat(ctx, "start");
							c.overworldEnd = FloatArgumentType.getFloat(ctx, "end");
							c.sanitize();
							OldFogConfig.save();
							msg(ctx, String.format(Locale.ROOT, "Overworld: Start %.2f, Ende %.2f", c.overworldStart, c.overworldEnd));
							return 1;
						}))))
			.then(ClientCommands.literal("nether")
				.then(ClientCommands.argument("start", FloatArgumentType.floatArg(0F, 10F))
					.then(ClientCommands.argument("end", FloatArgumentType.floatArg(0.01F, 10F))
						.executes(ctx -> {
							OldFogConfig c = OldFogConfig.get();
							c.netherStart = FloatArgumentType.getFloat(ctx, "start");
							c.netherEnd = FloatArgumentType.getFloat(ctx, "end");
							c.sanitize();
							OldFogConfig.save();
							msg(ctx, String.format(Locale.ROOT, "Nether: Start %.2f, Ende %.2f", c.netherStart, c.netherEnd));
							return 1;
						}))))
			.then(ClientCommands.literal("color")
				.then(ClientCommands.literal("on").executes(ctx -> color(ctx, true)))
				.then(ClientCommands.literal("off").executes(ctx -> color(ctx, false)))
				.then(ClientCommands.argument("hex", StringArgumentType.word()).executes(ctx -> {
					String hex = StringArgumentType.getString(ctx, "hex").replace("#", "");
					if (!hex.matches("[0-9a-fA-F]{6}")) {
						msg(ctx, "Ungültige Farbe. Beispiel: /oldfog color C0D8FF");
						return 0;
					}
					OldFogConfig c = OldFogConfig.get();
					c.setFogColorRgb(Integer.parseInt(hex, 16));
					c.customFogColor = true;
					OldFogConfig.save();
					msg(ctx, "Nebelfarbe: " + c.fogColor + " (aktiv)");
					return 1;
				})))
			.then(ClientCommands.literal("nethercolor")
				.then(ClientCommands.literal("on").executes(ctx -> netherColor(ctx, true)))
				.then(ClientCommands.literal("off").executes(ctx -> netherColor(ctx, false))))
			.then(ClientCommands.literal("nether-fog")
				.then(ClientCommands.literal("on").executes(ctx -> netherFog(ctx, true)))
				.then(ClientCommands.literal("off").executes(ctx -> netherFog(ctx, false))))
			.then(ClientCommands.literal("haze")
				.then(ClientCommands.literal("on").executes(ctx -> haze(ctx, false)))
				.then(ClientCommands.literal("off").executes(ctx -> haze(ctx, true))))
		);
	}

	private static int preset(CommandContext<FabricClientCommandSource> ctx, String name) {
		OldFogConfig c = OldFogConfig.get();
		c.applyPreset(name);
		OldFogConfig.save();
		msg(ctx, "Preset: " + (name.equals("classic") ? "Klassisch 1.8.9" : "Dicht (Screenshot-Look)"));
		return 1;
	}

	private static int openGui() {
		Minecraft mc = Minecraft.getInstance();
		mc.execute(() -> mc.gui.setScreen(new OldFogScreen(null, 0)));
		return 1;
	}

	private static int enable(CommandContext<FabricClientCommandSource> ctx, boolean on) {
		OldFogConfig.get().enabled = on;
		OldFogConfig.save();
		msg(ctx, on ? "Old Fog: AN" : "Old Fog: AUS (Vanilla-Nebel)");
		return 1;
	}

	private static int color(CommandContext<FabricClientCommandSource> ctx, boolean on) {
		OldFogConfig.get().customFogColor = on;
		OldFogConfig.save();
		msg(ctx, "Eigene Nebelfarbe: " + (on ? "AN" : "AUS"));
		return 1;
	}

	private static int netherColor(CommandContext<FabricClientCommandSource> ctx, boolean on) {
		OldFogConfig.get().customColorInNether = on;
		OldFogConfig.save();
		msg(ctx, "Eigene Farbe im Nether: " + (on ? "AN" : "AUS"));
		return 1;
	}

	private static int netherFog(CommandContext<FabricClientCommandSource> ctx, boolean on) {
		OldFogConfig.get().applyInNether = on;
		OldFogConfig.save();
		msg(ctx, "Old Fog im Nether: " + (on ? "AN" : "AUS"));
		return 1;
	}

	private static int haze(CommandContext<FabricClientCommandSource> ctx, boolean disable) {
		OldFogConfig.get().disableEnvironmentalFog = disable;
		OldFogConfig.save();
		msg(ctx, "Moderner Dunst: " + (disable ? "entfernt" : "aktiv"));
		return 1;
	}

	private static int status(CommandContext<FabricClientCommandSource> ctx) {
		OldFogConfig c = OldFogConfig.get();
		msg(ctx, "Merlin's Old Fog: " + (c.enabled ? "AN" : "AUS"));
		msg(ctx, String.format(Locale.ROOT, "Overworld %.2f - %.2f | Nether %s %.2f - %.2f (max %.0f)",
			c.overworldStart, c.overworldEnd, c.applyInNether ? "AN" : "AUS", c.netherStart, c.netherEnd, c.netherMaxDistance));
		msg(ctx, "Farbe: " + (c.customFogColor ? c.fogColor : "Vanilla") + " | Nether-Farbe: " + (c.customColorInNether ? "AN" : "AUS")
			+ " | Dunst entfernt: " + c.disableEnvironmentalFog + " | Himmel angeglichen: " + c.matchSkyFog + " | Deckkraft: " + c.fogOpacity);
		return 1;
	}

	private static void msg(CommandContext<FabricClientCommandSource> ctx, String text) {
		ctx.getSource().sendFeedback(Component.literal("[Old Fog] " + text));
	}
}
