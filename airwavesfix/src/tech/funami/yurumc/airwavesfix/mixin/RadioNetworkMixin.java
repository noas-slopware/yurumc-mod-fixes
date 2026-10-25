package tech.funami.yurumc.airwavesfix.mixin;

import com.airwaves.be.MicrophoneBE;
import com.airwaves.be.ReceiverBE;
import com.airwaves.be.TapePlayerBE;
import com.airwaves.logic.Cables;
import com.airwaves.logic.RadioNetwork;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Nothing picks up sound from the world any more: no player voice (from Airwaves' own voice chat or Simple Voice
 * Chat, near a transmitter, microphone, radio console or walkie-talkie), and no world sounds or jukeboxes near a
 * microphone, moving radio or recording tape player. Microphones already placed in a world stay as inert blocks.
 */
@Mixin(value = RadioNetwork.class, remap = false)
public abstract class RadioNetworkMixin {
	@Inject(method = "onVoice", at = @At("HEAD"), cancellable = true)
	private static void yurumc$noVoice(ServerPlayer p, byte[] data, CallbackInfo ci) {
		ci.cancel();
	}

	// the voice fan-out itself: also reachable by other mods through AirwavesApi.broadcast
	@Inject(method = "broadcast(Lnet/minecraft/server/level/ServerLevel;JLjava/util/UUID;[BF)Z", at = @At("HEAD"), cancellable = true)
	private static void yurumc$noVoiceBroadcast(ServerLevel sl, long root, UUID talker, byte[] data, float gain, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}

	@Inject(method = "onWorldSound", at = @At("HEAD"), cancellable = true)
	private static void yurumc$noWorldSounds(ServerLevel sl, Vec3 at, ResourceLocation id, float vol, float pitch, CallbackInfo ci) {
		ci.cancel();
	}

	@Inject(method = "register", at = @At("HEAD"), cancellable = true)
	private static void yurumc$noMicrophones(BlockEntity be, CallbackInfo ci) {
		if (be instanceof MicrophoneBE && !(be instanceof TapePlayerBE)) {
			ci.cancel();
		}
	}

	// Collecting a receiver's cable segments de-duplicated with List.contains, which is quadratic in the size of the
	// cable network (up to 512 nodes) and ran for every receiver once a second. Collect everything, de-duplicate once.
	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/List;contains(Ljava/lang/Object;)Z"))
	private static boolean yurumc$collectAll(List<?> segs, Object seg) {
		return false;
	}

	@Redirect(method = "tick", at = @At(value = "FIELD", target = "Lcom/airwaves/be/ReceiverBE;paths:Ljava/util/List;", opcode = Opcodes.PUTFIELD))
	private static void yurumc$dedupe(ReceiverBE rx, List<Cables.Seg> segs) {
		rx.paths = new ArrayList<>(new LinkedHashSet<>(segs));
	}
}
