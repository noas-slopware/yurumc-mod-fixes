package tech.funami.yurumc.liftfix.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.phys.AABB;
import org.mtr.core.data.Lift;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.render.PositionAndRotation;
import org.mtr.render.RenderLifts;
import org.mtr.render.StoredMatrixTransformations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import tech.funami.yurumc.liftfix.LiftCabinRenderer;

/**
 * In MTR 4.1.0-beta.2 the per-lift lambda in RenderLifts#render computes the cabin's StoredMatrixTransformations and
 * then only draws the floor display, because the ModelLift1 call was commented out. Hook that same value and draw the
 * cabin with it, using the doorway state the lambda already computed.
 */
@Mixin(value = RenderLifts.class, remap = false)
public abstract class RenderLiftsMixin {

	@ModifyExpressionValue(
			method = "lambda$render$7",
			at = @At(value = "INVOKE", target = "Lorg/mtr/render/RenderVehicles;getStoredMatrixTransformations(ZLorg/mtr/render/PositionAndRotation;D)Lorg/mtr/render/StoredMatrixTransformations;"),
			remap = false
	)
	private static StoredMatrixTransformations yurumc$renderLiftCabin(
			StoredMatrixTransformations storedMatrixTransformations,
			@Local(name = "lift") Lift lift,
			@Local(name = "absolutePositionAndRotation") PositionAndRotation absolutePositionAndRotation,
			@Local(name = "openDoorways") ObjectArrayList<AABB> openDoorways,
			@Local(name = "doorway1") AABB doorway1,
			@Local(name = "doorway2") AABB doorway2
	) {
		LiftCabinRenderer.render(storedMatrixTransformations, lift, absolutePositionAndRotation.light, openDoorways.contains(doorway1), openDoorways.contains(doorway2));
		return storedMatrixTransformations;
	}
}
