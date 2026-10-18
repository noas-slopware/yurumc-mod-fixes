package tech.funami.yurumc.meadowfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Meadow 1.4.9 renames its old IDs (meadow:chair -> meadow:pine_chair, meadow:cheese_rack ->
 * meadow:pine_cheese_rack, meadow:cooking_cauldron -> meadow:cookpot, ...) through a mixin on the
 * ResourceLocation constructor. That mixin is not applied on some clients, so server and client end
 * up with different registry keys and the client is kicked during registry sync.
 * Keeping Meadow's rename table empty makes every side register the original IDs.
 */
@Pseudo
@Mixin(targets = "net.satisfy.meadow.core.util.datafixer.DataFixers", remap = false)
public abstract class MeadowDataFixersMixin {
    @Inject(method = "register(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void yurumc$skipRename(String namespace, String from, String to, CallbackInfo ci) {
        ci.cancel();
    }
}
