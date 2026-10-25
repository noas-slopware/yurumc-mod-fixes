package tech.funami.yurumc.airwavesfix.tape;

import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.api.record.TrackData;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Etched side of {@link Records}; only loaded when Etched is installed. */
final class EtchedRecords {
	private EtchedRecords() {
	}

	static boolean accepts(ItemStack stack) {
		return PlayableRecord.isPlayableRecord(stack);
	}

	/** The first playable track; album covers and multi-track discs only get their first one dubbed. */
	@Nullable
	static Records.Track of(HolderLookup.Provider registries, ItemStack stack) {
		for (TrackData track : PlayableRecord.getTracks(registries, stack)) {
			if (!track.isValid()) {
				continue;
			}
			ResourceLocation id = TrackData.isLocalSound(track.url())
				? ResourceLocation.tryParse(track.url())
				: TrackIds.etched(track.url(), track.artist() + " - " + track.title().getString());
			if (id != null) {
				return new Records.Track(id, -1);
			}
		}
		return null;
	}
}
