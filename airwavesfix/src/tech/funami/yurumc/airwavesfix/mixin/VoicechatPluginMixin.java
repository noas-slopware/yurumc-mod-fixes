package tech.funami.yurumc.airwavesfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * With Simple Voice Chat installed, Airwaves listened to every SVC microphone packet (whispers and private groups
 * included) and fed it to its radios. Don't register that listener at all. Only applies when SVC loads the plugin.
 */
@Pseudo
@Mixin(targets = "com.airwaves.compat.svc.AirwavesVoicechatPlugin", remap = false)
public abstract class VoicechatPluginMixin {
	@Inject(method = "registerEvents", at = @At("HEAD"), cancellable = true)
	private void yurumc$noMicListener(CallbackInfo ci) {
		ci.cancel();
	}
}
