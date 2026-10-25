package tech.funami.yurumc.airwavesfix.tape;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** The Tape Player's record slot (container index 1, next to the tape in index 0). */
public class DiscSlot extends Slot {
	public static final int X = 168;
	public static final int Y = 75;

	public DiscSlot(Container container) {
		super(container, 1, X, Y);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return Records.accepts(stack);
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}
}
