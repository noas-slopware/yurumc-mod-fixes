package tech.funami.yurumc.airwavesfix.mixin;

import com.airwaves.be.DimensionalRelayBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.funami.yurumc.airwavesfix.RelayTickets;

/**
 * The relay's config says "a running Dimensional Relay keeps its own chunk loaded", but it forced the chunk whether
 * or not it was running: an unpowered relay was a free chunk loader. Only keep it loaded while active, and do it
 * with a ticket owned by the relay instead of the shared vanilla forced-chunk flag (see {@link RelayTickets}).
 */
@Mixin(value = DimensionalRelayBE.class, remap = false)
public abstract class DimensionalRelayBEMixin {
	@Shadow
	public boolean active;
	@Shadow
	private boolean forced;
	/** Saved by an Airwaves build without this fix, so the chunk is held by the vanilla flag rather than a ticket. */
	@Unique
	private boolean yurumc$legacyForce;

	@ModifyArg(method = "refresh", at = @At(value = "INVOKE", target = "Lcom/airwaves/be/DimensionalRelayBE;keepLoaded(Z)V"))
	private boolean yurumc$onlyWhileActive(boolean configured) {
		return configured && this.active;
	}

	@Inject(method = "keepLoaded", at = @At("HEAD"))
	private void yurumc$releaseLegacyForce(boolean on, CallbackInfo ci) {
		DimensionalRelayBE self = (DimensionalRelayBE) (Object) this;
		if (this.yurumc$legacyForce && self.getLevel() instanceof ServerLevel sl) {
			BlockPos pos = self.getBlockPos();
			sl.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, false);
			this.yurumc$legacyForce = false;
			this.forced = false;
		}
	}

	@Redirect(method = "keepLoaded", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setChunkForced(IIZ)Z"))
	private boolean yurumc$ticket(ServerLevel sl, int x, int z, boolean on) {
		return RelayTickets.CONTROLLER.forceChunk(sl, ((DimensionalRelayBE) (Object) this).getBlockPos(), x, z, on, true);
	}

	@Inject(method = "write", at = @At("TAIL"))
	private void yurumc$markTicket(CompoundTag tag, HolderLookup.Provider r, boolean clientPacket, CallbackInfo ci) {
		if (!clientPacket) {
			tag.putBoolean("yurumc_ticket", true);
		}
	}

	@Inject(method = "read", at = @At("TAIL"))
	private void yurumc$readTicket(CompoundTag tag, HolderLookup.Provider r, boolean clientPacket, CallbackInfo ci) {
		if (!clientPacket) {
			this.yurumc$legacyForce = this.forced && !tag.getBoolean("yurumc_ticket");
		}
	}
}
