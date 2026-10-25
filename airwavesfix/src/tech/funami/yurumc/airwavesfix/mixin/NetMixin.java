package tech.funami.yurumc.airwavesfix.mixin;

import com.airwaves.logic.RadioNetwork;
import com.airwaves.menu.TuningTableMenu;
import com.airwaves.net.Net;
import com.airwaves.net.SpectrumPayload;
import com.airwaves.net.SpectrumRequestPayload;
import com.airwaves.net.StationInfo;
import com.airwaves.net.VoiceFramePayload;
import com.airwaves.net.VoiceModePayload;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.funami.yurumc.airwavesfix.Throttle;

/**
 * Serverbound packets. Voice frames and the voice-mode switch are dropped outright (the payloads stay registered so
 * the channel set still matches Airwaves'): even a modified client can't send audio any more. The spectrum request,
 * which runs signal and terrain math for every station in the dimension, now needs the Tuning Table open (the only
 * screen that shows it) and is limited to its normal once a second.
 */
@Mixin(value = Net.class, remap = false)
public abstract class NetMixin {
	@Unique
	private static final int yurumc$MAX_NAME = 64;

	@Inject(method = "onVoice", at = @At("HEAD"), cancellable = true)
	private static void yurumc$noVoice(VoiceFramePayload p, IPayloadContext ctx, CallbackInfo ci) {
		ci.cancel();
	}

	@Inject(method = "onVoiceMode", at = @At("HEAD"), cancellable = true)
	private static void yurumc$noVoiceMode(VoiceModePayload p, IPayloadContext ctx, CallbackInfo ci) {
		ci.cancel();
	}

	@Inject(method = "onSpectrumRequest", at = @At("HEAD"), cancellable = true)
	private static void yurumc$spectrum(SpectrumRequestPayload p, IPayloadContext ctx, CallbackInfo ci) {
		ci.cancel();
		if (ctx.player() instanceof ServerPlayer sp && sp.containerMenu instanceof TuningTableMenu && Throttle.allow(sp, "spectrum", 10)) {
			// StationInfo encodes names as stringUtf8(64): one longer name (e.g. from /give) threw while encoding
			// and kicked everyone who opened a Tuning Table in range of that station.
			List<StationInfo> stations = RadioNetwork.spectrum(sp).stream()
				.map(s -> s.name() != null && s.name().length() > yurumc$MAX_NAME
					? new StationInfo(s.fm(), s.freq(), s.quality(), yurumc$truncate(s.name()))
					: s)
				.toList();
			PacketDistributor.sendToPlayer(sp, new SpectrumPayload(stations));
		}
	}

	@Unique
	private static String yurumc$truncate(String name) {
		int end = yurumc$MAX_NAME;
		if (Character.isHighSurrogate(name.charAt(end - 1))) {
			end--;
		}
		return name.substring(0, end);
	}
}
