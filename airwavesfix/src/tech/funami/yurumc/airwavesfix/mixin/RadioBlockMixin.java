package tech.funami.yurumc.airwavesfix.mixin;

import com.airwaves.block.RadioBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.funami.yurumc.airwavesfix.Throttle;

/** Seek cooldown (sneak-click), see {@link Throttle#seekThrottled}. */
@Mixin(value = RadioBlock.class, remap = false)
public abstract class RadioBlockMixin {
	@Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
	private void yurumc$seekCooldown(BlockState s, Level lv, BlockPos pos, Player pl, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
		if (pl.isShiftKeyDown() && Throttle.handsEmpty(pl) && Throttle.seekThrottled(lv, pl)) {
			cir.setReturnValue(InteractionResult.CONSUME);
		}
	}
}
