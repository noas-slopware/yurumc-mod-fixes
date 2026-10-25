package tech.funami.yurumc.airwavesfix.mixin;

import com.airwaves.item.PortableRadioItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.funami.yurumc.airwavesfix.Throttle;

/** Seek cooldown, see {@link Throttle#seekThrottled}. */
@Mixin(value = PortableRadioItem.class, remap = false)
public abstract class PortableRadioItemMixin {
	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void yurumc$seekCooldown(Level lv, Player pl, InteractionHand h, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
		if (pl.isShiftKeyDown() && Throttle.seekThrottled(lv, pl)) {
			cir.setReturnValue(InteractionResultHolder.consume(pl.getItemInHand(h)));
		}
	}
}
