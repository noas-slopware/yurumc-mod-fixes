package tech.funami.yurumc.airwavesfix.client;

import com.airwaves.client.VoiceConfig;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Replaces Airwaves' "Voice &amp; Radio" menu, which was mostly microphone settings. What's left: how loud radios are
 * and a switch to silence them, saved in the same config/airwaves-voice.properties.
 */
public class RadioSettingsScreen extends Screen {
	private static final int W = 200;

	public RadioSettingsScreen() {
		super(Component.translatable("screen.yurumc_airwavesfix.radio_settings"));
	}

	@Override
	protected void init() {
		int x = (this.width - W) / 2;
		int y = this.height / 2 - 40;
		this.addRenderableWidget(new Volume(x, y, W));
		this.addRenderableWidget(Button.builder(muteLabel(), b -> {
			VoiceConfig.deafened = !VoiceConfig.deafened;
			b.setMessage(muteLabel());
		}).bounds(x, y + 24, W, 20).build());
		this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> this.onClose()).bounds(x, y + 56, W, 20).build());
	}

	private static Component muteLabel() {
		return Component.translatable(VoiceConfig.deafened ? "screen.yurumc_airwavesfix.radio_settings.unmute" : "screen.yurumc_airwavesfix.radio_settings.mute");
	}

	@Override
	public void render(GuiGraphics g, int mx, int my, float pt) {
		super.render(g, mx, my, pt);
		g.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 64, 0xFFFFFF);
	}

	@Override
	public void onClose() {
		VoiceConfig.save();
		super.onClose();
	}

	private static final class Volume extends AbstractSliderButton {
		Volume(int x, int y, int w) {
			super(x, y, w, 20, Component.empty(), Mth.clamp(VoiceConfig.radio / 2.0, 0.0, 1.0));
			this.updateMessage();
		}

		@Override
		protected void updateMessage() {
			this.setMessage(Component.translatable("screen.yurumc_airwavesfix.radio_settings.volume",
				String.format(Locale.ROOT, "%d%%", Math.round(this.value * 200.0))));
		}

		@Override
		protected void applyValue() {
			VoiceConfig.radio = (float) (this.value * 2.0);
		}
	}
}
