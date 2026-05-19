package com.nobodiiiii.createbiotech.foundation.render;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.gui.ILightingSettings;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class EntityModelElement {
	public static final ILightingSettings DEFAULT_GUI_LIGHTING = Lighting::setupForEntityInInventory;

	private EntityModelElement() {
	}

	public static <T extends Entity> EntityModelRenderBuilder<T> of(T entity) {
		return new EntityModelRenderBuilder<>(entity);
	}

	@FunctionalInterface
	public interface ModelRenderer<T extends Entity> {
		void render(T entity, PoseStack poseStack, MultiBufferSource buffer, int packedLight);
	}

	@FunctionalInterface
	public interface EntityStateModifier<T extends Entity> {
		@Nullable
		StateRestorer apply(T entity, float partialTicks);
	}

	@FunctionalInterface
	public interface StateRestorer {
		void restore();
	}

	public static class EntityModelRenderBuilder<T extends Entity> {
		private final T entity;

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
		private int packedLight;
		private float partialTicks = AnimationTickHolder.getPartialTicks();
		@Nullable
		private EntityStateModifier<T> stateModifier;

		private EntityModelRenderBuilder(T entity) {
			this.entity = entity;
		}

		public EntityModelRenderBuilder<T> atLocal(double x, double y, double z) {
			this.xLocal = x;
			this.yLocal = y;
			this.zLocal = z;
			return this;
		}

		public EntityModelRenderBuilder<T> rotate(double xRot, double yRot, double zRot) {
			this.xRot = xRot;
			this.yRot = yRot;
			this.zRot = zRot;
			return this;
		}

		public EntityModelRenderBuilder<T> rotateCentered(double xRot, double yRot, double zRot) {
			return this.rotate(xRot, yRot, zRot)
				.withRotationOffset(VecHelper.getCenterOf(BlockPos.ZERO));
		}

		public EntityModelRenderBuilder<T> scale(double scale) {
			return scale(scale, scale, scale);
		}

		public EntityModelRenderBuilder<T> scale(double xScale, double yScale, double zScale) {
			this.scaleX = xScale;
			this.scaleY = yScale;
			this.scaleZ = zScale;
			return this;
		}

		public EntityModelRenderBuilder<T> withRotationOffset(Vec3 offset) {
			this.rotationOffset = offset;
			return this;
		}

		public EntityModelRenderBuilder<T> lighting(@Nullable ILightingSettings lighting) {
			this.customLighting = lighting;
			return this;
		}

		public EntityModelRenderBuilder<T> packedLight(int packedLight) {
			this.packedLight = packedLight;
			return this;
		}

		public EntityModelRenderBuilder<T> partialTicks(float partialTicks) {
			this.partialTicks = partialTicks;
			return this;
		}

		public EntityModelRenderBuilder<T> stateModifier(@Nullable EntityStateModifier<T> stateModifier) {
			this.stateModifier = stateModifier;
			return this;
		}

		public void render(PoseStack poseStack, MultiBufferSource buffer, ModelRenderer<T> modelRenderer) {
			poseStack.pushPose();
			prepareLighting();

			poseStack.translate(xLocal, yLocal, zLocal);
			poseStack.scale((float) scaleX, (float) scaleY, (float) scaleZ);
			poseStack.translate(rotationOffset.x, rotationOffset.y, rotationOffset.z);
			poseStack.mulPose(Axis.ZP.rotationDegrees((float) zRot));
			poseStack.mulPose(Axis.XP.rotationDegrees((float) xRot));
			poseStack.mulPose(Axis.YP.rotationDegrees((float) yRot));
			poseStack.translate(-rotationOffset.x, -rotationOffset.y, -rotationOffset.z);

			StateRestorer customStateRestorer = null;
			try {
				if (stateModifier != null)
					customStateRestorer = stateModifier.apply(entity, partialTicks);
				modelRenderer.render(entity, poseStack, buffer, packedLight);
			} finally {
				if (customStateRestorer != null)
					customStateRestorer.restore();
				poseStack.popPose();
				cleanUpLighting();
			}
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
