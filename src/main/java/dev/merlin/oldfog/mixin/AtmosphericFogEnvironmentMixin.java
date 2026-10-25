package dev.merlin.oldfog.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.merlin.oldfog.OldFogConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Restores the Minecraft 1.8.9 terrain fog (values are configurable).
 * Only the atmospheric fog is changed; water/lava/blindness/etc. stay vanilla.
 * Vanilla's FogData is edited before upload to the fog buffer, which Sodium also reads.
 */
@Mixin(AtmosphericFogEnvironment.class)
public abstract class AtmosphericFogEnvironmentMixin {

	@Inject(method = "setupFog", at = @At("TAIL"), remap = false)
	private void merlinsOldFog$classicFog(CallbackInfo ci,
										  @Local(argsOnly = true) FogData data,
										  @Local(argsOnly = true) ClientLevel level) {
		OldFogConfig cfg = OldFogConfig.get();
		if (!cfg.enabled) return;

		float far = Minecraft.getInstance().options.getEffectiveRenderDistance() * 16.0F;

		float start;
		float end;
		if (level.dimensionType().hasCeiling()) {
			if (!cfg.applyInNether) return;
			start = far * cfg.netherStart;
			end = Math.min(far, cfg.netherMaxDistance) * cfg.netherEnd;
		} else {
			float ref = cfg.fixedDistance ? Math.min(far, cfg.fixedDistanceBlocks) : far;
			start = ref * cfg.overworldStart;
			end = ref * cfg.overworldEnd;
		}

		data.renderDistanceStart = start;
		data.renderDistanceEnd = end;

		if (cfg.disableEnvironmentalFog) {
			data.environmentalStart = far * 2.0F;
			data.environmentalEnd = far * 2.0F + 1.0F;
		}
		if (cfg.matchSkyFog) {
			data.skyEnd = end;
		}
	}
}
