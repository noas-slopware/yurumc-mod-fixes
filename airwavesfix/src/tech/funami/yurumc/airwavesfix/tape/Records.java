package tech.funami.yurumc.airwavesfix.tape;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import tech.funami.yurumc.airwavesfix.AirwavesFix;

/** What the Tape Player can dub: vanilla music discs (anything jukebox-playable) and, with Etched, its discs. */
public final class Records {
	private Records() {
	}

	/**
	 * @param sound       sound event id, or an {@link TrackIds#etched} id
	 * @param lengthTicks length of the track, or -1 when the server can't know it (Etched only streams on clients)
	 */
	public record Track(ResourceLocation sound, int lengthTicks) {
	}

	public static boolean accepts(ItemStack stack) {
		return stack.has(DataComponents.JUKEBOX_PLAYABLE) || AirwavesFix.ETCHED && EtchedRecords.accepts(stack);
	}

	@Nullable
	public static Track of(HolderLookup.Provider registries, ItemStack stack) {
		if (stack.isEmpty()) {
			return null;
		}
		Optional<Holder<JukeboxSong>> song = JukeboxSong.fromStack(registries, stack);
		if (song.isPresent()) {
			JukeboxSong s = song.get().value();
			return new Track(s.soundEvent().value().getLocation(), Mth.ceil(s.lengthInSeconds() * 20.0F));
		}
		return AirwavesFix.ETCHED ? EtchedRecords.of(registries, stack) : null;
	}
}
