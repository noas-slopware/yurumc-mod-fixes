package tech.funami.yurumc.airwavesfix.mixin.client;

import com.airwaves.client.VoiceClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Airwaves opened the microphone on every login, voice-activated and unmuted. Never open it. */
@Mixin(value = VoiceClient.class, remap = false)
public abstract class VoiceClientMixin {
	@Inject(method = "start", at = @At("HEAD"), cancellable = true)
	private static void yurumc$noMicrophone(CallbackInfo ci) {
		ci.cancel();
	}
}
