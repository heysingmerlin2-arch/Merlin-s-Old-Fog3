package dev.merlin.oldfog.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.merlin.oldfog.OldFogConfig;
import dev.merlin.oldfog.OldFogState;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fog opacity. The fog shader multiplies the fog amount by the fog colour's alpha,
 * so we write the configured opacity into that alpha right before vanilla uploads
 * the fog data. require = 0: if names change, only this option stops working.
 */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

	@Inject(method = "setupFog", at = @At("HEAD"), remap = false, require = 0)
	private void merlinsOldFog$resetFlag(CallbackInfoReturnable<Vector4f> cir) {
		OldFogState.atmospheric = false;
	}

	@Inject(method = "updateBuffer", at = @At("HEAD"), remap = false, require = 0)
	private void merlinsOldFog$opacity(CallbackInfo ci, @Local(argsOnly = true) Vector4f color) {
		OldFogConfig cfg = OldFogConfig.get();
		if (cfg.enabled && OldFogState.atmospheric) {
			color.w = cfg.fogOpacity;
			OldFogState.alphaModified = true;
		} else if (OldFogState.alphaModified) {
			color.w = 1.0F;
		}
	}
}
