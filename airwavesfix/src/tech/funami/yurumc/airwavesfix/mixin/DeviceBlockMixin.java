package tech.funami.yurumc.airwavesfix.mixin;

import com.airwaves.block.DeviceBlock;
import com.airwaves.block.MicrophoneBlock;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The Microphone stays registered (removing it would break worlds, recipes and Airwaves' own setup code that looks
 * it up), but it can't be crafted, isn't listed anywhere, does nothing, and can't be placed any more.
 */
@Mixin(value = DeviceBlock.class, remap = false)
public abstract class DeviceBlockMixin {
	@Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
	private void yurumc$noMicrophones(BlockPlaceContext c, CallbackInfoReturnable<BlockState> cir) {
		if ((Object) this instanceof MicrophoneBlock) {
			cir.setReturnValue(null);
		}
	}
}
