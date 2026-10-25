package tech.funami.yurumc.airwavesfix;

import com.airwaves.be.DimensionalRelayBE;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.world.chunk.TicketController;

/**
 * Chunk tickets for the Dimensional Relay, owned by the relay's position. Airwaves used the vanilla forced-chunk
 * flag, which is one boolean per chunk: two relays in a chunk unforced each other, and a relay removed without its
 * block's onRemove (or one in a chunk an admin /forceload-ed) left the chunk forced forever.
 */
public final class RelayTickets {
	public static final TicketController CONTROLLER = new TicketController(
		ResourceLocation.fromNamespaceAndPath(AirwavesFix.MODID, "dimensional_relay"),
		(level, tickets) -> List.copyOf(tickets.getBlockTickets().keySet()).forEach(pos -> {
			if (!(level.getBlockEntity(pos) instanceof DimensionalRelayBE)) {
				tickets.removeAllTickets(pos);
			}
		})
	);

	private RelayTickets() {
	}
}
