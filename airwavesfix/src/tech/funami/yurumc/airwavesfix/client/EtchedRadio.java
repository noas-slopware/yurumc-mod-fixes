package tech.funami.yurumc.airwavesfix.client;

import com.airwaves.client.AudioPlayback;
import com.airwaves.client.VoiceConfig;
import com.airwaves.net.RadioSoundPayload;
import gg.moonflower.etched.api.record.TrackData;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import tech.funami.yurumc.airwavesfix.tape.TrackIds;

/**
 * Client side of Etched tracks on the radio: a copy of what Airwaves' RadioSounds does for music (same keys, ranges,
 * timeouts, volume and static), playing {@link EtchedRadioSound}s instead of sound events. Only loaded with Etched.
 */
public final class EtchedRadio {
	private static final RandomSource RNG = RandomSource.create();
	private static final long TIMEOUT_MS = 3500L;
	private static final double MUSIC_RANGE = 72.0;
	private static final double NOW_PLAYING_RANGE = 24.0;
	private static final Map<String, Playing> PLAYING = new HashMap<>();

	private EtchedRadio() {
	}

	public static void music(Minecraft mc, RadioSoundPayload p, String key, @Nullable Vec3 at, float q) {
		Playing t = PLAYING.get(key);
		boolean inRange = q >= 0.04F && (at == null || mc.player.getEyePosition().distanceTo(at) <= MUSIC_RANGE);
		if (!inRange) {
			stop(key);
			return;
		}
		if (t != null) {
			t.seen = System.currentTimeMillis();
			return;
		}
		String url = TrackIds.url(p.sound());
		if (url == null || !TrackData.isValidURL(url) || TrackData.isLocalSound(url)) {
			return;
		}
		float volume = Math.max(0.02F, p.volume() * VoiceConfig.radio * (0.35F + 0.65F * q));
		float pitch = p.pitch() * (p.fm() ? 1.0F : 1.0F + (RNG.nextFloat() - 0.5F) * 0.05F * (1.0F - q));
		EtchedRadioSound sound = new EtchedRadioSound(url, at, p.handheld(), volume, pitch, p.offsetMs());
		PLAYING.put(key, new Playing(sound, at));
		mc.getSoundManager().play(sound);
		if (q < 0.55F) {
			AudioPlayback.burst(0.12F * (1.0F - q));
		}
		String title = TrackIds.title(p.sound());
		if (title != null && !title.isBlank() && (at == null || mc.player.getEyePosition().distanceTo(at) <= NOW_PLAYING_RANGE)) {
			mc.gui.setNowPlaying(Component.literal(title));
		}
	}

	public static void stop(String key) {
		Playing old = PLAYING.remove(key);
		if (old != null) {
			Minecraft.getInstance().getSoundManager().stop(old.sound);
		}
	}

	public static void tick(Minecraft mc) {
		if (mc.level == null || mc.player == null) {
			stopAll();
			return;
		}
		long now = System.currentTimeMillis();
		Vec3 eye = mc.player.getEyePosition();
		Iterator<Playing> it = PLAYING.values().iterator();
		while (it.hasNext()) {
			Playing t = it.next();
			if (now - t.seen > TIMEOUT_MS || t.at != null && eye.distanceTo(t.at) > MUSIC_RANGE + 8.0) {
				mc.getSoundManager().stop(t.sound);
				it.remove();
			}
		}
	}

	public static void stopAt(BlockPos pos) {
		String prefix = pos.asLong() + ":";
		Minecraft mc = Minecraft.getInstance();
		Iterator<Map.Entry<String, Playing>> it = PLAYING.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Playing> e = it.next();
			if (e.getKey().startsWith(prefix)) {
				mc.getSoundManager().stop(e.getValue().sound);
				it.remove();
			}
		}
	}

	public static void stopAll() {
		Minecraft mc = Minecraft.getInstance();
		for (Playing t : PLAYING.values()) {
			mc.getSoundManager().stop(t.sound);
		}
		PLAYING.clear();
	}

	private static final class Playing {
		final EtchedRadioSound sound;
		final Vec3 at;
		long seen = System.currentTimeMillis();

		Playing(EtchedRadioSound sound, Vec3 at) {
			this.sound = sound;
			this.at = at;
		}
	}
}
