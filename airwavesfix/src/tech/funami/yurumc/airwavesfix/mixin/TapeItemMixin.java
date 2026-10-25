package tech.funami.yurumc.airwavesfix.mixin;

import com.airwaves.item.TapeItem;
import com.airwaves.logic.TapeData;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.funami.yurumc.airwavesfix.tape.TrackIds;

/** Lists the dubbed tracks on a tape's tooltip. */
@Mixin(value = TapeItem.class, remap = false)
public abstract class TapeItemMixin {
	@Unique
	private static final int yurumc$MAX_LINES = 6;

	@Inject(method = "appendHoverText", at = @At("TAIL"))
	private void yurumc$tracks(ItemStack s, TooltipContext ctx, List<Component> tip, TooltipFlag flag, CallbackInfo ci) {
		HolderLookup.Provider registries = ctx.registries();
		int shown = 0;
		for (TapeData.Cue c : TapeItem.data(s).cues()) {
			if (c.kind() != 1) {
				continue;
			}
			if (shown++ == yurumc$MAX_LINES) {
				tip.add(Component.literal("  ...").withStyle(ChatFormatting.DARK_GRAY));
				break;
			}
			tip.add(Component.literal("  ").append(yurumc$name(registries, c.sound())).withStyle(ChatFormatting.GRAY));
		}
	}

	@Unique
	private static Component yurumc$name(HolderLookup.Provider registries, ResourceLocation sound) {
		String title = TrackIds.title(sound);
		if (title != null) {
			return Component.literal(title);
		}
		if (registries != null) {
			var songs = registries.lookup(Registries.JUKEBOX_SONG);
			if (songs.isPresent()) {
				for (var song : songs.get().listElements().toList()) {
					JukeboxSong j = song.value();
					if (j.soundEvent().value().getLocation().equals(sound)) {
						return j.description();
					}
				}
			}
		}
		return Component.literal(sound.toString());
	}
}
