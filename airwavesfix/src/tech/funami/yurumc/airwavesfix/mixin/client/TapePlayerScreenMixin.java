package tech.funami.yurumc.airwavesfix.mixin.client;

import com.airwaves.client.TapePlayerScreen;
import com.airwaves.menu.TapePlayerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.funami.yurumc.airwavesfix.tape.DiscSlot;

/** Draws the disc slot (over the speaker grille in the side panel) and updates the hints for disc recording. */
@Mixin(value = TapePlayerScreen.class, remap = false)
public abstract class TapePlayerScreenMixin extends AbstractContainerScreen<TapePlayerMenu> {
	private TapePlayerScreenMixin(TapePlayerMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
	}

	@Inject(method = "renderBg", at = @At("TAIL"))
	private void yurumc$discSlot(GuiGraphics g, float pt, int mx, int my, CallbackInfo ci) {
		// same frame as the tape slot
		int sx = this.leftPos + DiscSlot.X - 5;
		int sy = this.topPos + DiscSlot.Y - 5;
		g.fill(sx + 1, sy, sx + 25, sy + 26, -15067628);
		g.fill(sx, sy + 1, sx + 26, sy + 25, -15067628);
		g.fill(sx + 2, sy + 2, sx + 24, sy + 24, -13752284);
		g.fill(sx + 4, sy + 23, sx + 22, sy + 24, -11911110);
		if (!this.yurumc$disc().hasItem()) {
			// a faint record where the disc goes
			int cx = this.leftPos + DiscSlot.X + 8;
			int cy = this.topPos + DiscSlot.Y + 8;
			for (int dy = -7; dy <= 7; dy++) {
				int dx = (int) Math.sqrt(49 - dy * dy);
				g.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, 0x30FFFFFF);
			}
			g.fill(cx - 1, cy - 1, cx + 2, cy + 2, -13752284);
		}
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void yurumc$discTooltip(GuiGraphics g, int mx, int my, float pt, CallbackInfo ci) {
		Slot disc = this.yurumc$disc();
		if (!disc.hasItem() && this.isHovering(disc.x, disc.y, 16, 16, mx, my)) {
			g.renderTooltip(this.font, Component.translatable("gui.yurumc_airwavesfix.tape_player.disc"), mx, my);
		}
	}

	@ModifyConstant(method = "drawScreen", constant = @Constant(stringValue = "talk or make noise nearby"))
	private String yurumc$hint(String original) {
		return this.yurumc$disc().hasItem() ? "dubbing the disc" : "put a disc in";
	}

	@ModifyConstant(method = "render", constant = @Constant(stringValue = "Record"))
	private String yurumc$recordTip(String original) {
		return "Record disc";
	}

	@Unique
	private Slot yurumc$disc() {
		return this.menu.slots.get(this.menu.slots.size() - 1);
	}
}
