package tech.funami.yurumc.liftfix;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.texture.OverlayTexture;

import java.util.ArrayList;
import java.util.List;

/**
 * Port of MTR 4.0.6 {@code org.mtr.mod.model.ModelLift1} (MIT, Minecraft-Transit-Railway) onto vanilla model parts.
 * MTR 4.1.0-beta removed the class and commented out the cabin render call in {@code RenderLifts}.
 * Geometry, UVs and the per-stage tiling are kept identical to the original.
 */
public final class LiftCabinModel {

	public enum Stage {LIGHT, INTERIOR, EXTERIOR}

	private static final float HALF_PI = (float) Math.PI / 2;
	private static final float PI = (float) Math.PI;

	private final Part main, main_ceiling, main_edge, main_edge_wall, main_edge_ceiling, main_corner, main_corner_wall, main_corner_ceiling;
	private final Part main_exterior, main_exterior_ceiling, main_exterior_edge, main_exterior_edge_ceiling, main_exterior_corner, main_exterior_corner_wall, main_exterior_corner_ceiling;
	private final Part main_light;
	private final Part door, door_left, door_right, door_wall, door_ceiling;
	private final Part door_exterior, door_left_exterior, door_right_exterior, door_wall_exterior, door_ceiling_exterior;
	private final Part wall_patch, wall_patch_wall;

	private final int heightCount;
	private final int heightOffset;
	private final int width;
	private final int depth;
	private final boolean isDoubleSided;

	public LiftCabinModel(int height, int width, int depth, boolean isDoubleSided) {
		heightCount = height - 4;
		heightOffset = -heightCount * 8;
		this.width = width;
		this.depth = depth;
		this.isDoubleSided = isDoubleSided;

		final Builder b = new Builder();

		main = b.part();
		main.setPivot(0, 24, 0);
		main.uv(0, 34).cuboid(-8, 0, -8, 16, 0, 16);

		main_ceiling = b.part();
		main_ceiling.setPivot(0, 24, 0);
		main_ceiling.uv(79, 44).cuboid(-8, -32, -8, 16, 0, 16);

		main_edge = b.part();
		main_edge.setPivot(0, 24, 0);
		main_edge.uv(18, 44).cuboid(-4, 0, -8, 8, 0, 6);
		main_edge.uv(76, 33).cuboid(-4, -32, -3, 8, 32, 1);
		main_edge.uv(28, 52).cuboid(-4, -13, -4, 8, 1, 1);
		main_edge.uv(26, 50).cuboid(-4, -2, -4, 8, 1, 1);

		main_edge_wall = b.part();
		main_edge_wall.setPivot(0, 24, 0);
		main_edge_wall.uv(76, 33).cuboid(-4, -40, -3, 8, 8, 1);

		main_edge_ceiling = b.part();
		main_edge_ceiling.setPivot(0, 24, 0);
		main_edge_ceiling.uv(97, 44).cuboid(-4, -32, -8, 8, 0, 6);

		main_corner = b.part();
		main_corner.setPivot(0, 24, 0);
		main_corner.uv(20, 44).cuboid(2, 0, -8, 6, 0, 6);
		main_corner.uv(112, 62).cuboid(3, -32, -3, 5, 32, 1);
		main_corner.uv(29, 52).cuboid(5, -13, -4, 3, 1, 1);
		main_corner.uv(27, 50).cuboid(5, -2, -4, 3, 1, 1);
		main_corner.uv(104, 68).cuboid(2, -32, -3, 1, 32, 1);

		final Part handrail_bottom_r1 = main_corner.child();
		handrail_bottom_r1.setPivot(0, 0, 0);
		handrail_bottom_r1.setRotation(0, -1.5708F, 0);
		handrail_bottom_r1.uv(36, 50).cuboid(-8, -2, -4, 3, 1, 1);
		handrail_bottom_r1.uv(38, 52).cuboid(-8, -13, -4, 3, 1, 1);
		handrail_bottom_r1.uv(112, 62).cuboid(-8, -32, -3, 5, 32, 1);

		main_corner_wall = b.part();
		main_corner_wall.setPivot(0, 24, 0);
		main_corner_wall.uv(112, 62).cuboid(3, -40, -3, 5, 8, 1);
		main_corner_wall.uv(104, 68).cuboid(2, -40, -3, 1, 8, 1);

		final Part wall_r1 = main_corner_wall.child();
		wall_r1.setPivot(0, 0, 0);
		wall_r1.setRotation(0, -1.5708F, 0);
		wall_r1.uv(112, 62).cuboid(-8, -40, -3, 5, 8, 1);

		main_corner_ceiling = b.part();
		main_corner_ceiling.setPivot(0, 24, 0);
		main_corner_ceiling.uv(99, 44).cuboid(2, -32, -8, 6, 0, 6);

		main_exterior = b.part();
		main_exterior.setPivot(0, 24, 0);
		main_exterior.uv(0, 17).cuboid(-8, 0, -8, 16, 1, 16);

		main_exterior_ceiling = b.part();
		main_exterior_ceiling.setPivot(0, 24, 0);
		main_exterior_ceiling.uv(0, 0).cuboid(-8, -33, -8, 16, 1, 16);

		main_exterior_edge = b.part();
		main_exterior_edge.setPivot(0, 24, 0);
		main_exterior_edge.uv(18, 27).cuboid(-4, 0, -8, 8, 1, 6);

		main_exterior_edge_ceiling = b.part();
		main_exterior_edge_ceiling.setPivot(0, 24, 0);
		main_exterior_edge_ceiling.uv(18, 10).cuboid(-4, -33, -8, 8, 1, 6);

		main_exterior_corner = b.part();
		main_exterior_corner.setPivot(0, 24, 0);
		main_exterior_corner.uv(20, 27).cuboid(2, 0, -8, 6, 1, 6);

		main_exterior_corner_wall = b.part();
		main_exterior_corner_wall.setPivot(0, 24, 0);
		main_exterior_corner_wall.uv(108, 68).cuboid(2, -40, -3, 1, 8, 1);

		main_exterior_corner_ceiling = b.part();
		main_exterior_corner_ceiling.setPivot(0, 24, 0);
		main_exterior_corner_ceiling.uv(20, 10).cuboid(2, -33, -8, 6, 1, 6);

		main_light = b.part();
		main_light.setPivot(0, 24, 0);
		main_light.uv(79, 28).cuboid(-8, -32.5F, -8, 16, 0, 16);

		door = b.part();
		door.setPivot(0, 24, 0);
		door.uv(90, 66).cuboid(-16, -32, 4, 4, 32, 3);
		door.uv(14, 84).cuboid(12, -32, 4, 4, 32, 3);
		door.uv(20, 101).cuboid(-16, 0, 0, 32, 0, 8);

		door_left = b.part();
		door_left.setPivot(0, 24, 0);
		door_left.uv(52, 68).cuboid(-12, -32, 6, 12, 32, 0);

		door_right = b.part();
		door_right.setPivot(0, 24, 0);
		door_right.uv(28, 68).cuboid(0, -32, 6, 12, 32, 0);

		door_wall = b.part();
		door_wall.setPivot(0, 24, 0);
		door_wall.uv(48, 0).cuboid(-16, -40, 4, 32, 8, 3);

		door_ceiling = b.part();
		door_ceiling.setPivot(0, 24, 0);
		door_ceiling.uv(20, 101).cuboid(-16, -32, 0, 32, 0, 8);

		door_exterior = b.part();
		door_exterior.setPivot(0, 24, 0);
		door_exterior.uv(0, 84).cuboid(-16, -32, 4, 4, 32, 3);
		door_exterior.uv(76, 66).cuboid(12, -32, 4, 4, 32, 3);
		door_exterior.uv(28, 109).cuboid(-16, 0, 0, 32, 1, 8);

		door_left_exterior = b.part();
		door_left_exterior.setPivot(0, 24, 0);
		door_left_exterior.uv(0, 50).cuboid(-12, -32, 6, 12, 32, 2);

		door_right_exterior = b.part();
		door_right_exterior.setPivot(0, 24, 0);
		door_right_exterior.uv(48, 34).cuboid(0, -32, 6, 12, 32, 2);

		door_wall_exterior = b.part();
		door_wall_exterior.setPivot(0, 24, 0);
		door_wall_exterior.uv(48, 17).cuboid(-16, -40, 5, 32, 8, 3);

		door_ceiling_exterior = b.part();
		door_ceiling_exterior.setPivot(0, 24, 0);
		door_ceiling_exterior.uv(0, 119).cuboid(-16, -33, 0, 32, 1, 8);

		wall_patch = b.part();
		wall_patch.setPivot(0, 24, 0);

		final Part wall_r2 = wall_patch.child();
		wall_r2.setPivot(0, 0, 0);
		wall_r2.setRotation(0, -1.5708F, 0);
		wall_r2.uv(108, 95).cuboid(0, -32, 13, 4, 32, 1);
		wall_r2.uv(30, 50).cuboid(0, -2, 12, 4, 1, 1);
		wall_r2.uv(32, 52).cuboid(0, -13, 12, 4, 1, 1);

		final Part wall_r3 = wall_patch.child();
		wall_r3.setPivot(0, 0, 0);
		wall_r3.setRotation(0, 1.5708F, 0);
		wall_r3.uv(108, 95).cuboid(-4, -32, 13, 4, 32, 1);
		wall_r3.uv(30, 50).cuboid(-4, -2, 12, 4, 1, 1);
		wall_r3.uv(32, 52).cuboid(-4, -13, 12, 4, 1, 1);

		wall_patch_wall = b.part();
		wall_patch_wall.setPivot(0, 24, 0);

		final Part wall_r4 = wall_patch_wall.child();
		wall_r4.setPivot(0, 0, 0);
		wall_r4.setRotation(0, -1.5708F, 0);
		wall_r4.uv(108, 95).cuboid(0, -40, 13, 4, 8, 1);

		final Part wall_r5 = wall_patch_wall.child();
		wall_r5.setPivot(0, 0, 0);
		wall_r5.setRotation(0, 1.5708F, 0);
		wall_r5.uv(108, 95).cuboid(-4, -40, 13, 4, 8, 1);

		b.bake();
	}

	/**
	 * Same as ModelLift1#render, with ModelPartExtension.render(x, y, z, rotateY) mapped onto {@link Part#render}.
	 */
	public void render(PoseStack poseStack, VertexConsumer vertexConsumer, Stage stage, int light, float doorLeftZ, float doorRightZ) {
		final Ctx c = new Ctx(poseStack, vertexConsumer, light);

		for (int i = 0; i <= width; i++) {
			for (int j = 0; j <= depth; j++) {
				final float x = (i - width / 2F) * 16;
				final float z = (j - depth / 2F) * 16;

				final boolean edge1X = i == 0;
				final boolean edge2X = i == width;
				final boolean edge1Z = j == 0;
				final boolean edge2Z = j == depth;

				switch (stage) {
					case LIGHT:
						if (!edge1X && !edge2X && !edge1Z && !edge2Z) {
							c.once(main_light, x, heightOffset, z);
						}
						break;
					case INTERIOR:
					case EXTERIOR:
						final boolean interior = stage == Stage.INTERIOR;
						final Part mainPiece = interior ? main : main_exterior;
						final Part mainCeilingPiece = interior ? main_ceiling : main_exterior_ceiling;
						final Part mainEdgePiece = interior ? main_edge : main_exterior_edge;
						final Part mainEdgeCeilingPiece = interior ? main_edge_ceiling : main_exterior_edge_ceiling;
						final Part mainCornerPiece = interior ? main_corner : main_exterior_corner;
						final Part mainCornerWallPiece = interior ? main_corner_wall : main_exterior_corner_wall;
						final Part mainCornerCeilingPiece = interior ? main_corner_ceiling : main_exterior_corner_ceiling;

						if (!edge1X && !edge2X && !edge1Z && !edge2Z) {
							c.once(mainPiece, x, 0, z);
							c.once(mainCeilingPiece, x, heightOffset, z);
						}

						if (edge1X && !edge2X && !edge1Z && !edge2Z) {
							c.r(mainEdgePiece, x, 0, z - 4, -HALF_PI);
							c.r(mainEdgePiece, x, 0, z + 4, -HALF_PI);
							if (interior) {
								wall(c, main_edge_wall, x, z - 4, -HALF_PI);
								wall(c, main_edge_wall, x, z + 4, -HALF_PI);
							}
							c.r(mainEdgeCeilingPiece, x, heightOffset, z - 4, -HALF_PI);
							c.r(mainEdgeCeilingPiece, x, heightOffset, z + 4, -HALF_PI);
						}
						if (!edge1X && edge2X && !edge1Z && !edge2Z) {
							c.r(mainEdgePiece, x, 0, z - 4, HALF_PI);
							c.r(mainEdgePiece, x, 0, z + 4, HALF_PI);
							if (interior) {
								wall(c, main_edge_wall, x, z - 4, HALF_PI);
								wall(c, main_edge_wall, x, z + 4, HALF_PI);
							}
							c.r(mainEdgeCeilingPiece, x, heightOffset, z - 4, HALF_PI);
							c.r(mainEdgeCeilingPiece, x, heightOffset, z + 4, HALF_PI);
						}
						if (!edge1X && !edge2X && !edge1Z && edge2Z && !isDoubleSided) {
							c.once(mainEdgePiece, x - 4, 0, z);
							c.once(mainEdgePiece, x + 4, 0, z);
							if (interior) {
								wallOnce(c, main_edge_wall, x - 4, z);
								wallOnce(c, main_edge_wall, x + 4, z);
							}
							c.once(mainEdgeCeilingPiece, x - 4, heightOffset, z);
							c.once(mainEdgeCeilingPiece, x + 4, heightOffset, z);
						}

						if (edge1X && !edge2X && !edge1Z && edge2Z && (width > 2 || !isDoubleSided)) {
							c.r(mainCornerPiece, x, 0, z, 0);
							if (interior) {
								wall(c, mainCornerWallPiece, x, z, 0);
							}
							c.r(mainCornerCeilingPiece, x, heightOffset, z, 0);
						}
						if (!edge1X && edge2X && !edge1Z && edge2Z && (width > 2 || !isDoubleSided)) {
							c.r(mainCornerPiece, x, 0, z, HALF_PI);
							if (interior) {
								wall(c, mainCornerWallPiece, x, z, HALF_PI);
							}
							c.r(mainCornerCeilingPiece, x, heightOffset, z, HALF_PI);
						}
						if (edge1X && !edge2X && edge1Z && !edge2Z && width > 2) {
							c.r(mainCornerPiece, x, 0, z, -HALF_PI);
							if (interior) {
								wall(c, mainCornerWallPiece, x, z, -HALF_PI);
							}
							c.r(mainCornerCeilingPiece, x, heightOffset, z, -HALF_PI);
						}
						if (!edge1X && edge2X && edge1Z && !edge2Z && width > 2) {
							c.r(mainCornerPiece, x, 0, z, PI);
							if (interior) {
								wall(c, mainCornerWallPiece, x, z, PI);
							}
							c.r(mainCornerCeilingPiece, x, heightOffset, z, PI);
						}
						break;
				}
			}
		}

		if (stage == Stage.INTERIOR || stage == Stage.EXTERIOR) {
			final boolean interior = stage == Stage.INTERIOR;
			final Part doorLeftPiece = interior ? door_left : door_left_exterior;
			final Part doorRightPiece = interior ? door_right : door_right_exterior;
			final Part doorPiece = interior ? door : door_exterior;
			final Part doorWallPiece = interior ? door_wall : door_wall_exterior;
			final Part doorCeilingPiece = interior ? door_ceiling : door_ceiling_exterior;
			final Part mainEdgePiece = interior ? main_edge : main_exterior_edge;
			final Part mainEdgeCeilingPiece = interior ? main_edge_ceiling : main_exterior_edge_ceiling;

			c.flipped(doorLeftPiece, -doorLeftZ, 0, 8 - depth * 8);
			c.flipped(doorRightPiece, doorLeftZ, 0, 8 - depth * 8);
			c.flipped(doorPiece, 0, 0, 8 - depth * 8);
			wallFlipped(c, doorWallPiece, 0, 8 - depth * 8);
			c.flipped(doorCeilingPiece, 0, heightOffset, 8 - depth * 8);

			if (isDoubleSided) {
				c.once(doorLeftPiece, -doorRightZ, 0, -8 + depth * 8);
				c.once(doorRightPiece, doorRightZ, 0, -8 + depth * 8);
				c.once(doorPiece, 0, 0, -8 + depth * 8);
				wallOnce(c, doorWallPiece, 0, -8 + depth * 8);
				c.once(doorCeilingPiece, 0, heightOffset, -8 + depth * 8);
			}

			if (interior && width == 2) {
				c.flipped(wall_patch, 0, 0, 8 - depth * 8);
				wallFlipped(c, wall_patch_wall, 0, 8 - depth * 8);
				if (isDoubleSided) {
					c.once(wall_patch, 0, 0, -8 + depth * 8);
					wallOnce(c, wall_patch_wall, 0, -8 + depth * 8);
				}
			}

			for (int i = 1; i < width - 2; i++) {
				c.flipped(mainEdgePiece, i * 8 - width * 8 + 4, 0, -depth * 8);
				c.flipped(mainEdgePiece, -i * 8 + width * 8 - 4, 0, -depth * 8);
				if (interior) {
					wallFlipped(c, main_edge_wall, i * 8 - width * 8 + 4, -depth * 8);
					wallFlipped(c, main_edge_wall, -i * 8 + width * 8 - 4, -depth * 8);
				}
				c.flipped(mainEdgeCeilingPiece, i * 8 - width * 8 + 4, heightOffset, -depth * 8);
				c.flipped(mainEdgeCeilingPiece, -i * 8 + width * 8 - 4, heightOffset, -depth * 8);
				if (isDoubleSided) {
					c.once(mainEdgePiece, i * 8 - width * 8 + 4, 0, depth * 8);
					c.once(mainEdgePiece, -i * 8 + width * 8 - 4, 0, depth * 8);
					if (interior) {
						wallOnce(c, main_edge_wall, i * 8 - width * 8 + 4, depth * 8);
						wallOnce(c, main_edge_wall, -i * 8 + width * 8 - 4, depth * 8);
					}
					c.once(mainEdgeCeilingPiece, i * 8 - width * 8 + 4, heightOffset, depth * 8);
					c.once(mainEdgeCeilingPiece, -i * 8 + width * 8 - 4, heightOffset, depth * 8);
				}
			}
		}
	}

	private void wall(Ctx c, Part part, float x, float z, float rotateY) {
		for (int i = 0; i < heightCount; i++) {
			c.r(part, x, -i * 8, z, rotateY);
		}
	}

	private void wallOnce(Ctx c, Part part, float x, float z) {
		for (int i = 0; i < heightCount; i++) {
			c.once(part, x, -i * 8, z);
		}
	}

	private void wallFlipped(Ctx c, Part part, float x, float z) {
		for (int i = 0; i < heightCount; i++) {
			c.flipped(part, x, -i * 8, z);
		}
	}

	/**
	 * ModelTrainBase.renderOnce / renderOnceFlipped / ModelPartExtension.render semantics.
	 */
	private record Ctx(PoseStack poseStack, VertexConsumer vertexConsumer, int light) {
		void r(Part part, float x, float y, float z, float rotateY) {
			part.render(poseStack, vertexConsumer, light, x, y, z, rotateY);
		}

		void once(Part part, float x, float y, float z) {
			r(part, x, y, z, 0);
		}

		void flipped(Part part, float x, float y, float z) {
			r(part, -x, y, z, PI);
		}
	}

	/**
	 * Mirrors ModelPartExtension: a group whose pivot/yaw are set at render time, and each cuboid or child added as a
	 * sub-part posed at the pivot/rotation that was current when it was added.
	 */
	private static final class Part {
		private final Builder builder;
		private final PartDefinition definition;
		private final String name;
		private final Part parent;
		private float pivotX, pivotY, pivotZ, rotationX, rotationY, rotationZ;
		private int u, v;
		private ModelPart modelPart;

		private Part(Builder builder, PartDefinition definition, String name, Part parent) {
			this.builder = builder;
			this.definition = definition;
			this.name = name;
			this.parent = parent;
		}

		void setPivot(float x, float y, float z) {
			pivotX = x;
			pivotY = y;
			pivotZ = z;
		}

		void setRotation(float x, float y, float z) {
			rotationX = x;
			rotationY = y;
			rotationZ = z;
		}

		Part uv(int u, int v) {
			this.u = u;
			this.v = v;
			return this;
		}

		void cuboid(float x, float y, float z, int sizeX, int sizeY, int sizeZ) {
			definition.addOrReplaceChild(builder.nextName(), CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, sizeX, sizeY, sizeZ), pose());
		}

		Part child() {
			final String childName = builder.nextName();
			return new Part(builder, definition.addOrReplaceChild(childName, CubeListBuilder.create(), pose()), childName, this);
		}

		private PartPose pose() {
			return PartPose.offsetAndRotation(pivotX, pivotY, pivotZ, rotationX, rotationY, rotationZ);
		}

		void render(PoseStack poseStack, VertexConsumer vertexConsumer, int light, float x, float y, float z, float rotateY) {
			modelPart.x = x;
			modelPart.y = y;
			modelPart.z = z;
			modelPart.yRot = rotateY;
			modelPart.render(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY);
		}
	}

	private static final class Builder {
		private final MeshDefinition mesh = new MeshDefinition();
		private final List<Part> topLevelParts = new ArrayList<>();
		private int counter;

		String nextName() {
			return "p" + counter++;
		}

		Part part() {
			final String name = nextName();
			final Part part = new Part(this, mesh.getRoot().addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO), name, null);
			topLevelParts.add(part);
			return part;
		}

		void bake() {
			final ModelPart root = LayerDefinition.create(mesh, 128, 128).bakeRoot();
			topLevelParts.forEach(part -> part.modelPart = root.getChild(part.name));
		}
	}
}
