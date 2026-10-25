package tech.funami.yurumc.hypertubefix.mixin;

import com.pedrorok.hypertube.blocks.HyperJunctionBlock;
import com.pedrorok.hypertube.core.data.MoveDirection;
import com.pedrorok.hypertube.utils.JunctionDirectionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * The direction a traveller arrives at a junction from is the direction of the last tube segment, which is UP or DOWN
 * for an entrance placed while looking up/down or a tube that reaches the junction vertically. Junction routing then
 * asks for the left/right of that direction ({@code Direction#getClockWise}), which only exists for horizontal
 * directions: an IllegalStateException in the junction's block entity tick takes the whole server down (and the same
 * call runs on the client to show the exit arrows). Junctions always face horizontally and their exits are defined
 * relative to that facing, so route a vertical arrival relative to the junction's own facing.
 */
@Mixin(value = JunctionDirectionUtils.class, remap = false)
public abstract class JunctionDirectionUtilsMixin {

	@ModifyVariable(method = "resolveValidDirectionTuple", at = @At("HEAD"), argsOnly = true, remap = false)
	private static Direction yurumc$horizontalJunctionDirection(Direction junctionDirection, MoveDirection direction, BlockPos blockPos, LevelAccessor level) {
		if (junctionDirection == null || junctionDirection.getAxis() != Direction.Axis.Y) {
			return junctionDirection;
		}
		BlockState state = level.getBlockState(blockPos);
		if (state.getBlock() instanceof HyperJunctionBlock && state.hasProperty(HyperJunctionBlock.FACING)) {
			Direction facing = state.getValue(HyperJunctionBlock.FACING);
			if (facing.getAxis() != Direction.Axis.Y) {
				return facing;
			}
		}
		return junctionDirection;
	}
}
