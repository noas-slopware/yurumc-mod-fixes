package tech.funami.yurumc.airwavesfix.mixin;

import com.airwaves.be.MicrophoneBE;
import com.airwaves.logic.SoundCapture;
import java.util.Collection;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Microphones and recording tape players used to listen for jukeboxes within 6 blocks (a 13x13x13 block entity scan
 * per live microphone, every second). Tape players take the disc directly now, so nothing listens.
 */
@Mixin(value = SoundCapture.class, remap = false)
public abstract class SoundCaptureMixin {
	@Inject(method = "pollJukeboxes", at = @At("HEAD"), cancellable = true)
	private static void yurumc$noJukeboxes(ServerLevel sl, Collection<MicrophoneBE> mics, CallbackInfo ci) {
		ci.cancel();
	}
}
