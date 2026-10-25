package tech.funami.yurumc.airwavesfix.mixin.client;

import com.airwaves.client.ClientInit;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.funami.yurumc.airwavesfix.client.RadioHud;
import tech.funami.yurumc.airwavesfix.client.RadioSettingsScreen;

/**
 * No push-to-talk key, the V key opens {@link RadioSettingsScreen} instead of the voice menu, and the HUD loses its
 * always-on voice status pill ({@link RadioHud}).
 */
@Mixin(value = ClientInit.class, remap = false)
public abstract class ClientInitMixin {
	@Redirect(method = "keys", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/event/RegisterKeyMappingsEvent;register(Lnet/minecraft/client/KeyMapping;)V"))
	private static void yurumc$noPushToTalk(RegisterKeyMappingsEvent e, KeyMapping key) {
		if (key != ClientInit.PTT_KEY) {
			e.register(key);
		}
	}

	@ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
	private static Screen yurumc$radioSettings(Screen voiceMenu) {
		return new RadioSettingsScreen();
	}

	@Inject(method = "hud", at = @At("HEAD"), cancellable = true)
	private static void yurumc$hud(GuiGraphics g, CallbackInfo ci) {
		RadioHud.render(g);
		ci.cancel();
	}
}
