package tech.funami.yurumc.hypertubefix.mixin;

import com.pedrorok.hypertube.blocks.HyperJunctionBlock;
import com.pedrorok.hypertube.core.travel.client.ClientTravelPathMover;
import com.pedrorok.hypertube.core.travel.client.ClientTravelPathRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 0.6.1's splitter HUD labels the chosen exit by comparing it with the arrival direction and its
 * {@code getCounterClockWise()}. A vertical arrival makes that throw an IllegalStateException on the render thread every
 * frame. {@link JunctionDirectionUtilsMixin} already routes a vertical arrival relative to the junction's own facing, so
 * label it relative to that same facing.
 */
@Mixin(value = ClientTravelPathRender.class, remap = false)
public abstract class ClientTravelPathRenderMixin {

	@ModifyVariable(method = "renderOverlay", at = @At("STORE"), ordinal = 0, remap = false)
	private static Direction yurumc$horizontalApproach(Direction approach) {
		if (approach == null || approach.getAxis() != Direction.Axis.Y) {
			return approach;
		}
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return approach;
		}
		ClientTravelPathMover.PathData path = ClientTravelPathMover.getData(player.getId());
		if (path == null) {
			return approach;
		}
		BlockState state = player.level().getBlockState(path.getLastBlockPos());
		if (state.getBlock() instanceof HyperJunctionBlock && state.hasProperty(HyperJunctionBlock.FACING)) {
			Direction facing = state.getValue(HyperJunctionBlock.FACING);
			if (facing.getAxis() != Direction.Axis.Y) {
				return facing;
			}
		}
		return approach;
	}
}
