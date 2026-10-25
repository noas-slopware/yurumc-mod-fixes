package tech.funami.yurumc.airwavesfix.mixin;

import com.airwaves.logic.RadioNetwork;
import java.util.Map;
import java.util.UUID;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RadioNetwork.class, remap = false)
public interface RadioNetworkAccessor {
	@Accessor("HANDHELD")
	static Map<UUID, ?> yurumc$handheld() {
		throw new AssertionError();
	}
}
