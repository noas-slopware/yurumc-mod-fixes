package tech.funami.yurumc.airwavesfix.tape;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import tech.funami.yurumc.airwavesfix.AirwavesFix;

/**
 * Airwaves moves music around as a sound event id (tape cues, the 1 Hz radio heartbeat, relays, handhelds, moving
 * radios), and the client plays whatever id it gets. Etched tracks are URLs, so a dubbed Etched track is stored as
 * {@code yurumc_airwavesfix:etched/<hex url>/<hex title>}: everything on the way just passes it along, and only the
 * client end (RadioSoundsMixin) needs to know what it means. Hex keeps it inside ResourceLocation's [a-z0-9/._-].
 */
public final class TrackIds {
	/** Every listener gets the id once a second, so keep it reasonably small. */
	public static final int MAX_URL = 512;
	public static final int MAX_TITLE = 80;
	private static final String PREFIX = "etched/";
	private static final HexFormat HEX = HexFormat.of();

	private TrackIds() {
	}

	@Nullable
	public static ResourceLocation etched(String url, String title) {
		if (url.length() > MAX_URL) {
			return null;
		}
		if (title.length() > MAX_TITLE) {
			title = title.substring(0, MAX_TITLE);
		}
		return ResourceLocation.fromNamespaceAndPath(AirwavesFix.MODID, PREFIX + hex(url) + "/" + hex(title));
	}

	public static boolean isEtched(ResourceLocation id) {
		return AirwavesFix.MODID.equals(id.getNamespace()) && id.getPath().startsWith(PREFIX);
	}

	@Nullable
	public static String url(ResourceLocation id) {
		return part(id, 0);
	}

	@Nullable
	public static String title(ResourceLocation id) {
		return part(id, 1);
	}

	@Nullable
	private static String part(ResourceLocation id, int index) {
		if (!isEtched(id)) {
			return null;
		}
		String[] parts = id.getPath().substring(PREFIX.length()).split("/", -1);
		if (parts.length != 2) {
			return null;
		}
		try {
			return new String(HEX.parseHex(parts[index]), StandardCharsets.UTF_8);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private static String hex(String s) {
		return HEX.formatHex(s.getBytes(StandardCharsets.UTF_8));
	}
}
