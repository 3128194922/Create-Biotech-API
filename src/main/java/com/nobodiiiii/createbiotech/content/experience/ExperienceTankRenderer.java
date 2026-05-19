package com.nobodiiiii.createbiotech.content.experience;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.Level;

public class ExperienceTankRenderer implements BlockEntityRenderer<ExperienceTankBlockEntity> {

	private static final float ORB_SPEED = 0.06f; // blocks per tick - constant
	private static final float ORB_TURN_RATE = 0.04f;

	public ExperienceTankRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(ExperienceTankBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
		int packedLight, int packedOverlay) {
		if (!be.isController() || !be.hasWindow() || be.getStoredExperience() <= 0)
			return;
		Level level = be.getLevel();
		if (level == null)
			return;

		float fill = be.getFillState();
		int height = be.getHeight();
		int width = be.getWidth();
		int volume = width * width * height;
		int orbCount = Math.max(1, Math.min(64, (int) Math.ceil(fill * volume * 4.0f)));
		float ageTicks = level.getGameTime() + partialTick;

		float xMin = 0.2f;
		float xMax = width - 0.2f;
		float yMin = 0.2f;
		float yMax = height - 0.2f;
		float zMin = 0.2f;
		float zMax = width - 0.2f;

		for (int i = 0; i < orbCount; i++) {
			// Different prime offsets per orb create distinct Lissajous-style paths
			float seedA = i * 13.7f;
			float seedB = i * 17.3f + 5.1f;
			float seedC = i * 23.1f + 11.7f;
			float seedD = i * 29.7f + 7.3f;
			float seedE = i * 31.3f + 17.9f;
			float seedF = i * 37.9f + 23.5f;

			// Different frequency multipliers per axis & per orb -> chaotic non-overlapping motion
			float fx1 = 1.0f + (i % 5) * 0.13f;
			float fx2 = 0.7f + (i % 4) * 0.11f;
			float fy1 = 0.83f + (i % 6) * 0.09f;
			float fy2 = 1.27f + (i % 3) * 0.14f;
			float fz1 = 1.13f + (i % 7) * 0.07f;
			float fz2 = 0.91f + (i % 5) * 0.12f;

			// ORB_SPEED * ageTicks gives a constant linear "phase" - speed independent of tank size
			float t = ageTicks * ORB_SPEED;

			float nx = 0.5f * ((float) Math.sin(t * fx1 + seedA) + (float) Math.sin(t * fx2 + seedB) * 0.5f);
			float ny = 0.5f * ((float) Math.sin(t * fy1 + seedC) + (float) Math.sin(t * fy2 + seedD) * 0.5f);
			float nz = 0.5f * ((float) Math.sin(t * fz1 + seedE) + (float) Math.sin(t * fz2 + seedF) * 0.5f);

			nx = (nx + 1.5f) / 3.0f;
			ny = (ny + 1.5f) / 3.0f;
			nz = (nz + 1.5f) / 3.0f;

			double x = xMin + nx * (xMax - xMin);
			double y = yMin + ny * (yMax - yMin);
			double z = zMin + nz * (zMax - zMin);

			int icon = (int) ((seedA * 0.37f) + i) & 0x0F;

			poseStack.pushPose();
			poseStack.translate(x, y, z);
			ExperienceOrbModelRenderer.render(poseStack, buffer, packedLight, ageTicks * ORB_TURN_RATE * 25f + seedA,
				icon, 1.0f);
			poseStack.popPose();
		}
	}
}
