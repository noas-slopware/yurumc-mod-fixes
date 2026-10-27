package tech.funami.yurumc.wildcardlinkfix.mixin;

import com.hlysine.create_connected.content.redstonelinkwildcard.LinkWildcardNetworkHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * When the last link on a frequency leaves the network, Create Connected's wildcard handler tells the leaving link
 * that its power is now 0. Links leave the network from {@code invalidate()}, which Create's {@code SmartBlockEntity}
 * runs after {@code setRemoved()}, so on a chunk unload that signal goes to a block entity that is already dead
 * (Create's own handler drops dead links before it signals the network, and only re-signals a leaving link of its own
 * {@code LinkBehaviour} type). AeroEngine's afterburner reacts to it by looking up its airflow nozzle with
 * {@code getBlockState}, which asks for the chunk that is being unloaded right now: the server thread then waits forever
 * for a chunk that only it could finish unloading. Only signal the leaving link if it is still alive, so a live receiver
 * whose frequency changed is still zeroed.
 */
@Mixin(value = LinkWildcardNetworkHandler.class, remap = false)
public abstract class LinkWildcardNetworkHandlerMixin {

	@WrapOperation(method = "removeFromNetwork",
		at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/redstone/link/IRedstoneLinkable;setReceivedStrength(I)V"),
		remap = false)
	private static void yurumc$onlySignalAliveLinks(IRedstoneLinkable actor, int power, Operation<Void> original) {
		if (actor.isAlive()) {
			original.call(actor, power);
		}
	}
}
