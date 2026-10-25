package tech.funami.yurumc.airwavesfix.mixin;

import com.airwaves.be.TapePlayerBE;
import com.airwaves.item.TapeItem;
import com.airwaves.menu.TapePlayerMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.funami.yurumc.airwavesfix.tape.DiscSlot;
import tech.funami.yurumc.airwavesfix.tape.Records;

/**
 * Adds the disc slot as the last menu slot (index 37, after the tape and the player inventory, so Airwaves' own slot
 * indices stay valid). The client builds this menu around a 1-slot dummy container, so it gets its own 2-slot one.
 */
@Mixin(value = TapePlayerMenu.class, remap = false)
public abstract class TapePlayerMenuMixin extends AbstractContainerMenu {
	private TapePlayerMenuMixin(MenuType<?> type, int id) {
		super(type, id);
	}

	@Inject(
		method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;Lnet/minecraft/world/inventory/ContainerLevelAccess;Lcom/airwaves/be/TapePlayerBE;)V",
		at = @At("TAIL")
	)
	private void yurumc$addDiscSlot(int id, Inventory inv, Container tape, ContainerData data, ContainerLevelAccess access, TapePlayerBE be, CallbackInfo ci) {
		this.addSlot(new DiscSlot(tape.getContainerSize() > 1 ? tape : new SimpleContainer(2)));
	}

	// Airwaves' version sent everything from the inventory to the tape slot and everything else to slots 1..36
	@Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
	private void yurumc$quickMove(Player p, int idx, CallbackInfoReturnable<ItemStack> cir) {
		int disc = this.slots.size() - 1;
		Slot slot = this.slots.get(idx);
		if (!slot.hasItem()) {
			cir.setReturnValue(ItemStack.EMPTY);
			return;
		}
		ItemStack stack = slot.getItem();
		ItemStack ret = stack.copy();
		boolean moved;
		if (idx == 0 || idx == disc) {
			moved = this.moveItemStackTo(stack, 1, disc, true);
		} else if (stack.getItem() instanceof TapeItem) {
			moved = this.moveItemStackTo(stack, 0, 1, false);
		} else if (Records.accepts(stack)) {
			moved = this.moveItemStackTo(stack, disc, disc + 1, false);
		} else {
			moved = false;
		}
		if (!moved) {
			cir.setReturnValue(ItemStack.EMPTY);
			return;
		}
		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		cir.setReturnValue(ret);
	}
}
