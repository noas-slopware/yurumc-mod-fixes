package tech.funami.yurumc.airwavesfix;

import com.airwaves.ModRegistry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import tech.funami.yurumc.airwavesfix.mixin.RadioNetworkAccessor;

/**
 * Fixes for Create: Airwaves 0.1.0 (build 9.9). Most of the work is done by the mixins; this class only hooks the
 * events they need: hiding the removed Microphone from creative tabs, the chunk ticket controller for the
 * Dimensional Relay and per-player cleanup on logout.
 */
@Mod(AirwavesFix.MODID)
public final class AirwavesFix {
	public static final String MODID = "yurumc_airwavesfix";
	public static final boolean ETCHED = ModList.get().isLoaded("etched");

	public AirwavesFix(IEventBus modBus) {
		modBus.addListener(AirwavesFix::creativeTabs);
		modBus.addListener((RegisterTicketControllersEvent e) -> e.register(RelayTickets.CONTROLLER));
		NeoForge.EVENT_BUS.addListener(AirwavesFix::loggedOut);
	}

	// RadioNetwork only drops a handheld radio when its owner's level ticks without one, so players who log out with
	// a radio switched on stayed in the map (and were looked up on every broadcast) forever.
	private static void loggedOut(PlayerEvent.PlayerLoggedOutEvent e) {
		Throttle.forget(e.getEntity().getUUID());
		RadioNetworkAccessor.yurumc$handheld().remove(e.getEntity().getUUID());
	}

	private static void creativeTabs(BuildCreativeModeTabContentsEvent e) {
		ItemStack mic = new ItemStack(ModRegistry.MICROPHONE_ITEM.get());
		if (e.getParentEntries().contains(mic) || e.getSearchEntries().contains(mic)) {
			e.remove(mic, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		}
	}
}
