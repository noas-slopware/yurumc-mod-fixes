package tech.funami.yurumc.airwavesfix.client;

import com.airwaves.cable.NodeRef;
import com.airwaves.client.CableRenderer;
import com.airwaves.client.ClientData;
import com.airwaves.item.CableItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Airwaves' HUD layer minus the voice-chat pill it drew in the top-left corner at all times ("Voice", "Live",
 * "No mic"). The portable radio pill moves up into its place; the wire length meter is unchanged.
 */
public final class RadioHud {
	private RadioHud() {
	}

	private static void pill(GuiGraphics g, int x, int y, int w, int h, int accent) {
		g.fill(x, y, x + w, y + h, -1072688618);
		g.fill(x, y, x + 2, y + h, accent);
		g.fill(x, y, x + w, y + 1, 1090519039);
	}

	public static void render(GuiGraphics g) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui || mc.screen != null) {
			return;
		}
		Font font = mc.font;
		if (ClientData.hhOn) {
			String s = ClientData.hhLabel;
			int bars = ClientData.hhCarrier ? 1 + Math.round(ClientData.hhQuality * 4.0F) : 0;
			int w = font.width(s) + 40;
			int y = 4;
			pill(g, 4, y, w, 14, ClientData.hhCarrier ? -11220768 : -9800832);
			g.drawString(font, s, 10, y + 3, -1512208, false);
			for (int i = 0; i < 5; i++) {
				g.fill(w - 26 + i * 4, y + 10 - i * 2, w - 23 + i * 4, y + 11, i < bars ? -11214736 : -12959670);
			}
		}

		ItemStack st = mc.player.getMainHandItem();
		NodeRef start = st.getItem() instanceof CableItem ? CableItem.start(st) : null;
		if (start != null) {
			int max = ((CableItem) st.getItem()).type.maxLen;
			double len = CableRenderer.anchor(mc.level, start.pos(), start.slot()).distanceTo(cursor(mc));
			boolean far = len > max;
			int cx = mc.getWindow().getGuiScaledWidth() / 2;
			int cy = mc.getWindow().getGuiScaledHeight() / 2 + 12;
			pill(g, cx - 34, cy, 68, 8, far ? -2078648 : -2588598);
			int fillW = (int) (60.0 * Math.min(1.0, len / max));
			g.fill(cx - 30, cy + 3, cx - 30 + fillW, cy + 5, far ? -40864 : -1533344);
			g.fill(cx - 30 + fillW, cy + 3, cx + 30, cy + 5, -14013388);
		}
	}

	private static Vec3 cursor(Minecraft mc) {
		HitResult h = mc.hitResult;
		if (h != null && h.getType() == HitResult.Type.BLOCK) {
			NodeRef r = CableItem.refAt(mc.level, ((BlockHitResult) h).getBlockPos(), h.getLocation());
			return r != null ? CableRenderer.anchor(mc.level, r.pos(), r.slot()) : h.getLocation();
		}
		return mc.player.getEyePosition().add(mc.player.getLookAngle().scale(2.5));
	}
}
