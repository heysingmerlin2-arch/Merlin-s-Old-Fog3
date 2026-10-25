package dev.merlin.oldfog.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.merlin.oldfog.OldFogConfig;
import dev.merlin.oldfog.OldFogState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Optional custom fog colour. require = 0: if the method changes in a future
 * Minecraft version only the colour option stops working, the game still starts.
 */
@Mixin(AtmosphericFogEnvironment.class)
public abstract class AtmosphericFogColorMixin {

	/** Marks this frame as "normal air fog" so the opacity option does not touch water/lava fog. */
	@Inject(method = "setupFog", at = @At("HEAD"), remap = false, require = 0)
	private void merlinsOldFog$markAtmospheric(CallbackInfo ci) {
		OldFogState.atmospheric = true;
	}

	@ModifyReturnValue(method = "getBaseColor", at = @At("RETURN"), remap = false, require = 0)
	private int merlinsOldFog$customColor(int original, @Local(argsOnly = true) ClientLevel level) {
		OldFogConfig cfg = OldFogConfig.get();
		if (!cfg.enabled || !cfg.customFogColor) return original;
		if (level.dimensionType().hasCeiling() && !cfg.customColorInNether) return original;
		return 0xFF000000 | cfg.fogColorRgb();
	}
}
