package com.nobodiiiii.createbiotech.foundation.render;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.createmod.catnip.gui.ILightingSettings;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

public final class ItemEntityElement {
	public static final ILightingSettings DEFAULT_GUI_LIGHTING = Lighting::setupForEntityInInventory;
	private static final float DEFAULT_INVENTORY_ANGLE_X = 0.75f;
	private static final float DEFAULT_INVENTORY_ANGLE_Y = 0.6f;
	private static final double DEFAULT_GUI_X = 0.5d;
	private static final double DEFAULT_GUI_Y = 0.2d;
	private static final double DEFAULT_GUI_Z = 0.5d;
	private static final double DEFAULT_WORLD_X = 0.5d;
	private static final double DEFAULT_WORLD_Y = 0.45d;
	private static final double DEFAULT_WORLD_Z = 0.5d;
	private static final double DEFAULT_GUI_SCALE = 0.7d;
	private static final double DEFAULT_GROUND_SCALE = 0.75d;
	private static final double DEFAULT_OTHER_SCALE = 0.55d;

	private ItemEntityElement() {
	}

	public static <T extends Entity> ItemEntityRenderBuilder<T> of(T entity) {
		return new ItemEntityRenderBuilder<>(entity);
	}

	public static class ItemEntityRenderBuilder<T extends Entity> {
		private final EntityRenderHelper.RenderSettings<T> renderSettings;

		private double xLocal;
		private double yLocal;
		private double zLocal;
		private double xRot;
		private double yRot;
		private double zRot;
		private double scaleX = 1;
		private double scaleY = 1;
		private double scaleZ = 1;
		private Vec3 rotationOffset = Vec3.ZERO;
		@Nullable
		private ILightingSettings customLighting;
		@Nullable
		private Quaternionf poseOrientation;

		private ItemEntityRenderBuilder(T entity) {
			this.renderSettings = EntityRenderHelper.settings(entity);
		}

		public ItemEntityRenderBuilder<T> atLocal(double x, double y, double z) {
			this.xLocal = x;
			this.yLocal = y;
			this.zLocal = z;
			return this;
		}

		public ItemEntityRenderBuilder<T> rotate(double xRot, double yRot, double zRot) {
			this.xRot = xRot;
			this.yRot = yRot;
			this.zRot = zRot;
			return this;
		}

		public ItemEntityRenderBuilder<T> rotateCentered(double xRot, double yRot, double zRot) {
			return this.rotate(xRot, yRot, zRot)
				.withRotationOffset(VecHelper.getCenterOf(BlockPos.ZERO));
		}

		public ItemEntityRenderBuilder<T> scale(double scale) {
			return scale(scale, scale, scale);
		}

		public ItemEntityRenderBuilder<T> scale(double xScale, double yScale, double zScale) {
			this.scaleX = xScale;
			this.scaleY = yScale;
			this.scaleZ = zScale;
			return this;
		}

		public ItemEntityRenderBuilder<T> withRotationOffset(Vec3 offset) {
			this.rotationOffset = offset;
			return this;
		}

		public ItemEntityRenderBuilder<T> lighting(@Nullable ILightingSettings lighting) {
			this.customLighting = lighting;
			return this;
		}

		public ItemEntityRenderBuilder<T> packedLight(int packedLight) {
			renderSettings.packedLight(packedLight);
			return this;
		}

		public ItemEntityRenderBuilder<T> renderShadow(boolean renderShadow) {
			renderSettings.renderShadow(renderShadow);
			return this;
		}

		public ItemEntityRenderBuilder<T> partialTicks(float partialTicks) {
			renderSettings.partialTicks(partialTicks);
			return this;
		}

		public ItemEntityRenderBuilder<T> ticks(int tickCount) {
			renderSettings.ticks(tickCount);
			return this;
		}

		public ItemEntityRenderBuilder<T> dispatcherYaw(float dispatcherYaw) {
			renderSettings.dispatcherYaw(dispatcherYaw);
			return this;
		}

		public ItemEntityRenderBuilder<T> cameraOrientation(@Nullable Quaternionf cameraOrientation) {
			renderSettings.cameraOrientation(cameraOrientation);
			return this;
		}

		public ItemEntityRenderBuilder<T> poseOrientation(@Nullable Quaternionf poseOrientation) {
			this.poseOrientation = poseOrientation == null ? null : new Quaternionf(poseOrientation);
			return this;
		}

		public ItemEntityRenderBuilder<T> yaw(float yaw) {
			renderSettings.yaw(yaw);
			return this;
		}

		public ItemEntityRenderBuilder<T> bodyYaw(float bodyYaw) {
			renderSettings.bodyYaw(bodyYaw);
			return this;
		}

		public ItemEntityRenderBuilder<T> headYaw(float headYaw) {
			renderSettings.headYaw(headYaw);
			return this;
		}

		public ItemEntityRenderBuilder<T> pitch(float pitch) {
			renderSettings.pitch(pitch);
			return this;
		}

		public ItemEntityRenderBuilder<T> face(Direction direction) {
			renderSettings.face(direction);
			return this;
		}

		public ItemEntityRenderBuilder<T> inventoryLike(float angleXComponent, float angleYComponent) {
			Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI);
			Quaternionf camera = new Quaternionf().rotateX(angleYComponent * 20.0F * Mth.DEG_TO_RAD);
			pose.mul(camera);
			pose.rotateZ((float) Math.PI);

			float yaw = 180.0F + angleXComponent * 40.0F;
			return poseOrientation(pose)
				.cameraOrientation(camera)
				.bodyYaw(180.0F + angleXComponent * 20.0F)
				.yaw(yaw)
				.pitch(-angleYComponent * 20.0F)
				.headYaw(yaw)
				.dispatcherYaw(0.0F);
		}

		public ItemEntityRenderBuilder<T> inventoryLikeDefault() {
			return inventoryLike(DEFAULT_INVENTORY_ANGLE_X, DEFAULT_INVENTORY_ANGLE_Y);
		}

		public ItemEntityRenderBuilder<T> asItem(ItemDisplayContext displayContext, int packedLight) {
			partialTicks(1.0f);
			inventoryLikeDefault();

			if (displayContext == ItemDisplayContext.GUI) {
				return lighting(DEFAULT_GUI_LIGHTING)
					.packedLight(LightTexture.FULL_BRIGHT)
					.atLocal(DEFAULT_GUI_X, DEFAULT_GUI_Y, DEFAULT_GUI_Z)
					.scale(DEFAULT_GUI_SCALE, DEFAULT_GUI_SCALE, -DEFAULT_GUI_SCALE);
			}

			double scale = displayContext == ItemDisplayContext.GROUND ? DEFAULT_GROUND_SCALE : DEFAULT_OTHER_SCALE;
			return lighting(null)
				.packedLight(packedLight)
				.atLocal(DEFAULT_WORLD_X, DEFAULT_WORLD_Y, DEFAULT_WORLD_Z)
				.scale(scale, scale, -scale);
		}

		public void render(PoseStack poseStack, MultiBufferSource buffer) {
			poseStack.pushPose();
			prepareLighting();

			poseStack.translate(xLocal, yLocal, zLocal);
			poseStack.scale((float) scaleX, (float) scaleY, (float) scaleZ);
			poseStack.translate(rotationOffset.x, rotationOffset.y, rotationOffset.z);
			poseStack.mulPose(Axis.ZP.rotationDegrees((float) zRot));
			poseStack.mulPose(Axis.XP.rotationDegrees((float) xRot));
			poseStack.mulPose(Axis.YP.rotationDegrees((float) yRot));
			poseStack.translate(-rotationOffset.x, -rotationOffset.y, -rotationOffset.z);
			if (poseOrientation != null)
				poseStack.mulPose(new Quaternionf(poseOrientation));

			renderSettings.flushBuffers(false);
			EntityRenderHelper.render(renderSettings, poseStack, buffer);

			poseStack.popPose();
			cleanUpLighting();
		}

		private void prepareLighting() {
			if (customLighting != null)
				customLighting.applyLighting();
		}

		private void cleanUpLighting() {
			if (customLighting != null)
				Lighting.setupFor3DItems();
		}
	}
}
