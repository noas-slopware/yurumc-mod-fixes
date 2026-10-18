package tech.funami.yurumc.liftfix.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.mtr.block.BlockEntityExtension;
import org.spongepowered.asm.mixin.Mixin;

/**
 * MTR 4.0.x block entities called markDirty2(), which in Minecraft-Mappings also did
 * world.updateListeners(pos, state, state, NOTIFY_LISTENERS) so the change reached clients. 4.1.0-beta.2 replaced
 * those calls with plain setChanged(), which only marks the chunk for saving: e.g. links made with the lift buttons
 * link connector exist on the server but never show up client-side until the chunk is reloaded. Restore the old
 * behaviour for every MTR block entity.
 */
@Mixin(value = BlockEntityExtension.class, remap = false)
public abstract class BlockEntityExtensionMixin extends BlockEntity {

	private BlockEntityExtensionMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void setChanged() {
		super.setChanged();
		final BlockState blockState = getBlockState();
		if (level != null && !level.isClientSide()) {
			level.sendBlockUpdated(worldPosition, blockState, blockState, Block.UPDATE_CLIENTS);
		}
	}
}
