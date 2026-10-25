package tech.funami.yurumc.airwavesfix.mixin.client;

import com.airwaves.ModRegistry;
import com.airwaves.client.ponder.AirwavesPonderPlugin;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.createmod.ponder.api.registration.MultiSceneBuilder;
import net.createmod.ponder.api.registration.MultiTagBuilder;
import net.createmod.ponder.api.scene.PonderStoryBoard;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Drops the "Microphones and Wires" Ponder scene (shown on transmitters, wires and the mic) and the mic's index entry. */
@Mixin(value = AirwavesPonderPlugin.class, remap = false)
public abstract class AirwavesPonderPluginMixin {
	@WrapOperation(method = "registerScenes", at = @At(value = "INVOKE", target = "Lnet/createmod/ponder/api/registration/MultiSceneBuilder;addStoryBoard(Ljava/lang/String;Lnet/createmod/ponder/api/scene/PonderStoryBoard;[Lnet/minecraft/resources/ResourceLocation;)Lnet/createmod/ponder/api/registration/MultiSceneBuilder;"))
	private MultiSceneBuilder yurumc$noMicScene(MultiSceneBuilder builder, String schematic, PonderStoryBoard scene, ResourceLocation[] tags, Operation<MultiSceneBuilder> original) {
		return "transmitter/microphone".equals(schematic) ? builder : original.call(builder, schematic, scene, tags);
	}

	@WrapOperation(method = "registerTags", at = @At(value = "INVOKE", target = "Lnet/createmod/ponder/api/registration/MultiTagBuilder$Tag;add(Ljava/lang/Object;)Lnet/createmod/ponder/api/registration/MultiTagBuilder$Tag;"))
	private MultiTagBuilder.Tag<?> yurumc$noMicTag(MultiTagBuilder.Tag<?> tag, Object component, Operation<MultiTagBuilder.Tag<?>> original) {
		return ModRegistry.MICROPHONE_ITEM.getId().equals(component) ? tag : original.call(tag, component);
	}
}
