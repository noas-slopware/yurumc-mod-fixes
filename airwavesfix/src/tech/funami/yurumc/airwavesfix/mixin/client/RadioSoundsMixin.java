package tech.funami.yurumc.airwavesfix.mixin.client;

import com.airwaves.client.RadioSounds;
import com.airwaves.net.RadioSoundPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.funami.yurumc.airwavesfix.AirwavesFix;
import tech.funami.yurumc.airwavesfix.client.EtchedRadio;
import tech.funami.yurumc.airwavesfix.tape.TrackIds;

/** Radio music that is an Etched track ({@link TrackIds}) goes to {@link EtchedRadio} instead of the sound manager. */
@Mixin(value = RadioSounds.class, remap = false)
public abstract class RadioSoundsMixin {
	@Inject(method = "music", at = @At("HEAD"), cancellable = true)
	private static void yurumc$etched(Minecraft mc, RadioSoundPayload p, String key, Vec3 at, float q, CallbackInfo ci) {
		if (TrackIds.isEtched(p.sound())) {
			ci.cancel();
			if (AirwavesFix.ETCHED) {
				EtchedRadio.music(mc, p, key, at, q);
			}
		}
	}

	@Inject(method = "stop", at = @At("HEAD"))
	private static void yurumc$stop(String key, CallbackInfo ci) {
		if (AirwavesFix.ETCHED) {
			EtchedRadio.stop(key);
		}
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private static void yurumc$tick(Minecraft mc, CallbackInfo ci) {
		if (AirwavesFix.ETCHED) {
			EtchedRadio.tick(mc);
		}
	}

	@Inject(method = "stopAt", at = @At("HEAD"))
	private static void yurumc$stopAt(BlockPos pos, CallbackInfo ci) {
		if (AirwavesFix.ETCHED) {
			EtchedRadio.stopAt(pos);
		}
	}

	@Inject(method = "stopAll", at = @At("HEAD"))
	private static void yurumc$stopAll(CallbackInfo ci) {
		if (AirwavesFix.ETCHED) {
			EtchedRadio.stopAll();
		}
	}
}
