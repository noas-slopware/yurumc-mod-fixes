package tech.funami.yurumc.liftfix;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.mtr.core.data.Lift;
import org.mtr.data.IGui;
import org.mtr.render.MainRenderer;
import org.mtr.render.QueuedRenderLayer;
import org.mtr.render.RenderLifts;
import org.mtr.render.StoredMatrixTransformations;

import java.util.HashMap;
import java.util.Map;

/**
 * Re-adds the lift cabin draw that MTR 4.1.0-beta.2 commented out in RenderLifts. Mirrors 4.0.6
 * ModelTrainBase#render(lightsOn = true, isTranslucent = false) for ModelLift1.
 */
public final class LiftCabinRenderer {

	// ModelLift1: DoorAnimationType.CONSTANT, getDoorMax() = 24 / 4, getDoorDuration() = 0.5; RenderLifts.LIFT_DOOR_VALUE = 0.75
	private static final int DOOR_MAX = 24 / 4;
	private static final float DOOR_DURATION = 0.5F;
	private static final float LIFT_DOOR_VALUE = 0.75F;

	private static final Map<Long, LiftCabinModel> MODELS = new HashMap<>();

	private LiftCabinRenderer() {
	}

	public static void render(StoredMatrixTransformations storedMatrixTransformations, Lift lift, int light, boolean doorway1Open, boolean doorway2Open) {
		final int height = (int) Math.round(lift.getHeight() * 2);
		final int width = (int) Math.round(lift.getWidth());
		final int depth = (int) Math.round(lift.getDepth());
		final boolean isDoubleSided = lift.getIsDoubleSided();
		final long key = ((long) height << 33) | ((long) (width & 0xFFFF) << 17) | ((long) (depth & 0xFFFF) << 1) | (isDoubleSided ? 1 : 0);
		final LiftCabinModel model = MODELS.computeIfAbsent(key, k -> new LiftCabinModel(height, width, depth, isDoubleSided));

		final ResourceLocation texture = RenderLifts.getLiftResource(lift.getStyle()).getTexture();
		final float doorLeftZ = doorZ(doorway1Open ? lift.getDoorValue() / LIFT_DOOR_VALUE : 0);
		final float doorRightZ = doorZ(doorway2Open ? lift.getDoorValue() / LIFT_DOOR_VALUE : 0);

		final StoredMatrixTransformations transformations = storedMatrixTransformations.copy();
		transformations.add(poseStack -> poseStack.translate(0, -1.5, 0)); // ModelLift1#baseTransform

		schedule(texture, QueuedRenderLayer.LIGHT, transformations, model, LiftCabinModel.Stage.LIGHT, IGui.DEFAULT_LIGHT, doorLeftZ, doorRightZ);
		schedule(texture, QueuedRenderLayer.INTERIOR, transformations, model, LiftCabinModel.Stage.INTERIOR, IGui.MAX_LIGHT_INTERIOR, doorLeftZ, doorRightZ);
		schedule(texture, QueuedRenderLayer.EXTERIOR, transformations, model, LiftCabinModel.Stage.EXTERIOR, light, doorLeftZ, doorRightZ);
	}

	private static float doorZ(float value) {
		return DOOR_MAX * Mth.clamp(value, 0, 1) / DOOR_DURATION;
	}

	private static void schedule(ResourceLocation texture, QueuedRenderLayer layer, StoredMatrixTransformations transformations, LiftCabinModel model, LiftCabinModel.Stage stage, int light, float doorLeftZ, float doorRightZ) {
		MainRenderer.scheduleRender(texture, false, layer, (poseStack, vertexConsumer, offset) -> {
			transformations.transform(poseStack, offset);
			model.render(poseStack, vertexConsumer, stage, light, doorLeftZ, doorRightZ);
			poseStack.popPose();
		});
	}
}
