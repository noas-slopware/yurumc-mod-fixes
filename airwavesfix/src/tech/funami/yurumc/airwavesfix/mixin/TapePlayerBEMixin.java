package tech.funami.yurumc.airwavesfix.mixin;

import com.airwaves.be.MicrophoneBE;
import com.airwaves.be.TapePlayerBE;
import com.airwaves.logic.RadioNetwork;
import com.airwaves.logic.TapeAudio;
import com.airwaves.logic.TapeData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.funami.yurumc.airwavesfix.tape.Records;

/**
 * The Tape Player records a music disc instead of listening to the room. Its inventory gets a second slot for the
 * disc; pressing Record dubs whatever disc is in that slot onto the tape in real time, while it plays out of the
 * player so you hear what is being recorded. Swapping discs mid-recording makes a mixtape. A vanilla disc stops the
 * recording when it ends; Etched discs only stream on clients, so the server can't know their length and those
 * recordings run until Record is pressed again or the tape is full (5 minutes). Leading silence is cut off, and a
 * recording with no disc in it leaves the tape untouched.
 *
 * Voice recording and playback are gone: old tapes with recorded voices play only their sounds.
 */
@Mixin(value = TapePlayerBE.class, remap = false)
public abstract class TapePlayerBEMixin extends MicrophoneBE {
	@Unique
	private static final int yurumc$DISC = 1;
	@Unique
	private static final float yurumc$MONITOR_VOLUME = 2.5F;

	@Shadow
	@Final
	public SimpleContainer inv;
	@Shadow
	public int mode;
	@Shadow
	public int pos;
	@Shadow
	private List<TapeData.Cue> recording;

	/** The disc stack being dubbed (by identity, so taking it out and putting it back restarts it). */
	@Unique
	private ItemStack yurumc$dubStack = ItemStack.EMPTY;
	@Unique
	private ResourceLocation yurumc$dub;
	@Unique
	private int yurumc$dubStart;
	@Unique
	private int yurumc$dubLength;
	@Unique
	private ItemStack yurumc$tape = ItemStack.EMPTY;

	private TapePlayerBEMixin(BlockPos p, BlockState s) {
		super(p, s);
	}

	@Shadow
	public abstract ItemStack stack();

	@Shadow
	private void finishRecording() {
	}

	@Shadow
	public abstract void stop();

	// inv = new SimpleContainer(1) { ... }: slot 0 tape, slot 1 disc. ContainerHelper saves both and the block drops both.
	@ModifyConstant(method = "<init>", constant = @Constant(intValue = 1))
	private int yurumc$discSlot(int one) {
		return 2;
	}

	@Inject(method = "record", at = @At("TAIL"))
	private void yurumc$startDub(CallbackInfo ci) {
		if (this.mode == 2 && this.level instanceof ServerLevel sl) {
			this.yurumc$dub = null;
			this.yurumc$dubStack = ItemStack.EMPTY;
			this.yurumc$pollDisc(sl);
		}
	}

	@Inject(method = "serverTick", at = @At("HEAD"))
	private void yurumc$dubTick(CallbackInfo ci) {
		if (this.mode != 2 || !(this.level instanceof ServerLevel sl)) {
			return;
		}
		this.yurumc$pollDisc(sl);
		if (this.yurumc$dub == null) {
			return;
		}
		int played = this.pos - this.yurumc$dubStart;
		if (this.yurumc$dubLength > 0 && played >= this.yurumc$dubLength) {
			// the disc is over: end the tape exactly here (mode goes back to 0, so the rest of serverTick is a no-op)
			this.finishRecording();
		} else if (played > 0 && played % 20 == 0) {
			// keep the monitor alive: clients drop radio music they haven't heard about for 3.5 s
			this.yurumc$monitor(sl, played * 50);
		}
	}

	@Unique
	private void yurumc$pollDisc(ServerLevel sl) {
		ItemStack disc = this.inv.getItem(yurumc$DISC);
		if (disc == this.yurumc$dubStack) {
			return;
		}
		this.yurumc$dubStack = disc;
		if (this.yurumc$dub != null) {
			this.yurumc$cue(this.yurumc$dub, (byte) 2);
			this.yurumc$stopMonitor(sl);
		}
		Records.Track track = Records.of(sl.registryAccess(), disc);
		if (track != null && this.yurumc$cue(track.sound(), (byte) 1)) {
			this.yurumc$dub = track.sound();
			this.yurumc$dubStart = this.pos;
			this.yurumc$dubLength = track.lengthTicks();
			this.yurumc$monitor(sl, 0);
		}
	}

	@Unique
	private boolean yurumc$cue(ResourceLocation sound, byte kind) {
		if (this.recording.size() >= TapeData.MAX_CUES) {
			return false;
		}
		this.recording.add(new TapeData.Cue(this.pos, sound, 1.0F, 1.0F, kind));
		return true;
	}

	@Unique
	private void yurumc$monitor(ServerLevel sl, int offsetMs) {
		RadioNetwork.sendSound(sl, this.worldPosition, this.yurumc$dub, yurumc$MONITOR_VOLUME, 1.0F, 1.0F, true, (byte) 1, this.worldPosition.asLong(), offsetMs);
	}

	@Unique
	private void yurumc$stopMonitor(ServerLevel sl) {
		RadioNetwork.sendSound(sl, this.worldPosition, this.yurumc$dub, 1.0F, 1.0F, 1.0F, true, (byte) 2, this.worldPosition.asLong(), 0);
		this.yurumc$dub = null;
	}

	@Inject(method = "finishRecording", at = @At("HEAD"))
	private void yurumc$finishDub(CallbackInfo ci) {
		if (this.yurumc$dub != null && this.level instanceof ServerLevel sl) {
			this.yurumc$stopMonitor(sl);
		}
		this.yurumc$dub = null;
		this.yurumc$dubStack = ItemStack.EMPTY;

		int first = -1;
		for (TapeData.Cue c : this.recording) {
			if (c.kind() == 1) {
				first = c.tick();
				break;
			}
		}
		if (first < 0) {
			// nothing was dubbed: pos 0 makes finishRecording leave the tape as it was
			this.pos = 0;
		} else if (first > 0) {
			List<TapeData.Cue> shifted = new ArrayList<>(this.recording.size());
			for (TapeData.Cue c : this.recording) {
				shifted.add(new TapeData.Cue(Math.max(0, c.tick() - first), c.sound(), c.volume(), c.pitch(), c.kind()));
			}
			this.recording = shifted;
			this.pos -= first;
		}
	}

	@Inject(method = "onTapeChanged", at = @At("TAIL"))
	private void yurumc$tapeChanged(CallbackInfo ci) {
		if (this.level instanceof ServerLevel sl) {
			// recording dropped because the tape was taken out
			if (this.mode != 2 && this.yurumc$dub != null) {
				this.yurumc$stopMonitor(sl);
				this.yurumc$dubStack = ItemStack.EMPTY;
			}
			// swapping one tape for another while playing kept the old tape's cue and audio positions
			if (this.mode == 1 && this.stack() != this.yurumc$tape) {
				this.stop();
				this.pos = 0;
			}
		}
		this.yurumc$tape = this.stack();
	}

	@Inject(method = "setRemoved", at = @At("HEAD"))
	private void yurumc$removed(CallbackInfo ci) {
		if (this.yurumc$dub != null && this.level instanceof ServerLevel sl) {
			this.yurumc$stopMonitor(sl);
		}
	}

	@Inject(method = "loadAdditional", at = @At("HEAD"))
	private void yurumc$clearDisc(CompoundTag tag, HolderLookup.Provider r, CallbackInfo ci) {
		this.inv.getItems().set(yurumc$DISC, ItemStack.EMPTY);
	}

	@Inject(method = "loadAdditional", at = @At("TAIL"))
	private void yurumc$loaded(CompoundTag tag, HolderLookup.Provider r, CallbackInfo ci) {
		this.yurumc$tape = this.stack();
	}

	@Inject(method = "capture", at = @At("HEAD"), cancellable = true)
	private void yurumc$noRoomSounds(ResourceLocation id, float vol, float pitch, byte kind, CallbackInfo ci) {
		ci.cancel();
	}

	@Inject(method = "captureVoice", at = @At("HEAD"), cancellable = true)
	private void yurumc$noVoice(byte[] frame, CallbackInfo ci) {
		ci.cancel();
	}

	@Redirect(method = "seek", at = @At(value = "INVOKE", target = "Lcom/airwaves/logic/TapeAudio;load(Lnet/minecraft/server/MinecraftServer;Ljava/lang/String;)Ljava/util/List;"))
	private List<TapeAudio.Frame> yurumc$noVoicePlayback(MinecraftServer server, String id) {
		return List.of();
	}
}
