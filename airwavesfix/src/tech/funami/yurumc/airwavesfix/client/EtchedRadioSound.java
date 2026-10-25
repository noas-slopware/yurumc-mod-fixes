package tech.funami.yurumc.airwavesfix.client;

import gg.moonflower.etched.api.sound.AbstractOnlineSoundInstance;
import gg.moonflower.etched.api.sound.source.AudioSource;
import gg.moonflower.etched.api.util.DownloadProgressListener;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * An Etched track coming out of an Airwaves radio. Like Airwaves' own SeekingSound for vanilla discs, it skips
 * ahead to where the station is in the track, so tuning in halfway through a song doesn't restart it.
 */
public class EtchedRadioSound extends AbstractOnlineSoundInstance {
	private static final DownloadProgressListener QUIET = new DownloadProgressListener() {
		@Override
		public void progressStartRequest(Component component) {
		}

		@Override
		public void progressStartDownload(float size) {
		}

		@Override
		public void progressStagePercentage(int percentage) {
		}

		@Override
		public void progressStartLoading() {
		}

		@Override
		public void onSuccess() {
		}

		@Override
		public void onFail() {
		}
	};

	private final int offsetMs;

	public EtchedRadioSound(String url, @Nullable Vec3 at, boolean handheld, float volume, float pitch, int offsetMs) {
		super(url, null, 16, SoundSource.RECORDS, QUIET, AudioSource.AudioFileType.FILE, handheld);
		this.volume = volume;
		this.pitch = pitch;
		this.relative = handheld;
		this.attenuation = handheld ? Attenuation.NONE : Attenuation.LINEAR;
		if (at != null) {
			this.x = at.x;
			this.y = at.y;
			this.z = at.z;
		}
		this.offsetMs = offsetMs;
	}

	@Override
	public CompletableFuture<AudioStream> getStream(SoundBufferLibrary lib, Sound sound, boolean looping) {
		CompletableFuture<AudioStream> stream = super.getStream(lib, sound, looping);
		return this.offsetMs < 250 ? stream : stream.thenApply(s -> {
			skip(s, this.offsetMs);
			return s;
		});
	}

	private static void skip(AudioStream s, int ms) {
		try {
			AudioFormat fmt = s.getFormat();
			long bytes = (long) (ms / 1000.0 * fmt.getSampleRate()) * fmt.getChannels() * Math.max(1, fmt.getSampleSizeInBits() / 8);
			while (bytes > 0L) {
				ByteBuffer b = s.read((int) Math.min(bytes, 32768L));
				if (b == null || b.remaining() == 0) {
					break;
				}
				bytes -= b.remaining();
			}
		} catch (IOException | RuntimeException ignored) {
		}
	}
}
