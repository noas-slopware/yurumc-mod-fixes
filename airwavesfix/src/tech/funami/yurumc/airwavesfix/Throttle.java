package tech.funami.yurumc.airwavesfix;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Per-player cooldowns for server-side actions a modified client can trigger as fast as it can send packets. Only
 * touched from the server thread (Airwaves registers its payload handlers without executesOn, so they run there).
 */
public final class Throttle {
	private static final Map<UUID, Map<String, Integer>> LAST = new HashMap<>();

	private Throttle() {
	}

	/** True at most once every {@code ticks} server ticks per player and action. */
	public static boolean allow(ServerPlayer p, String action, int ticks) {
		int now = p.getServer().getTickCount();
		Map<String, Integer> mine = LAST.computeIfAbsent(p.getUUID(), k -> new HashMap<>());
		Integer last = mine.get(action);
		if (last != null && now >= last && now - last < ticks) {
			return false;
		}
		mine.put(action, now);
		return true;
	}

	/**
	 * Seeking (sneak-use on a radio, either click on a radio console) checks signal and terrain against every station
	 * on the band, and a modified client can send use packets every tick or faster. Vanilla repeats a held right-click
	 * every 4 ticks, so a 4 tick cooldown per player is invisible in normal play.
	 */
	public static boolean seekThrottled(Level lv, Player pl) {
		return !lv.isClientSide && pl instanceof ServerPlayer sp && !allow(sp, "radio_seek", 4);
	}

	public static boolean handsEmpty(Player pl) {
		return pl.getMainHandItem().isEmpty() && pl.getOffhandItem().isEmpty();
	}

	public static void forget(UUID player) {
		LAST.remove(player);
	}
}
