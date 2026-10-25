package tech.funami.yurumc.paladinfix.mixin;

import com.unlikepaladin.pfm.blocks.models.neoforge.PFMNeoForgeBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.impl.renderer.VanillaModelEncoder;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Supplier;

/**
 * Paladin's Furniture item quads only exist through NeoForge's {@code getRenderPasses(ItemStack, boolean)}: the
 * plain {@code getQuads(state, face, random)} of its furniture models returns nothing for items. NeoForge's item
 * renderer asks for the render passes, but Forgified Fabric API's FRAPI/Indigo item path does not
 * (Sinytra/ForgifiedFabricAPI#186): when a FRAPI model wrapper such as Continuity's sits on top, Indigo calls
 * {@code emitItemQuads}, whose default goes straight to {@code getQuads} and the item renders invisible in
 * inventories, JEI and the hand. Emit the render passes instead, as NeoForge's renderer would.
 */
@Mixin(value = PFMNeoForgeBakedModel.class, remap = false)
public abstract class PFMNeoForgeBakedModelMixin {

	public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
		for (BakedModel pass : ((PFMNeoForgeBakedModel) (Object) this).getRenderPasses(stack, false)) {
			VanillaModelEncoder.emitItemQuads(pass, null, randomSupplier, context);
		}
	}
}
