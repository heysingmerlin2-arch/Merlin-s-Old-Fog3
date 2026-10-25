package dev.merlin.oldfog;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.merlin.oldfog.gui.OldFogScreen;

/** Adds the "Config" button in Mod Menu. Only loaded when Mod Menu is installed. */
public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> new OldFogScreen(parent, 0);
	}
}
