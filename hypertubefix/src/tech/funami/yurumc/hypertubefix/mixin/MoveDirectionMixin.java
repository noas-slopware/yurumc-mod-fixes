package tech.funami.yurumc.hypertubefix.mixin;

import com.pedrorok.hypertube.core.data.MoveDirection;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Safety net for any other path that hands a vertical direction to {@link MoveDirection#map}: LEFT/RIGHT use
 * {@code Direction#getCounterClockWise}/{@code getClockWise}, which throw for UP and DOWN. There is no left or right
 * of straight up, so keep going straight instead of crashing.
 */
@Mixin(value = MoveDirection.class, remap = false)
public abstract class MoveDirectionMixin {

	@Inject(method = "map", at = @At("HEAD"), cancellable = true, remap = false)
	private void yurumc$noLeftOfUp(Direction direction, CallbackInfoReturnable<Direction> cir) {
		if (direction != null && direction.getAxis() == Direction.Axis.Y) {
			cir.setReturnValue(direction);
		}
	}
}
