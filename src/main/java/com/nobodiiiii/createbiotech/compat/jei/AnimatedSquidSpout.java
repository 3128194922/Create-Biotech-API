package com.nobodiiiii.createbiotech.compat.jei;

import java.util.List;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.platform.ForgeCatnipServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

public class AnimatedSquidSpout extends AnimatedKineticsWithEntities {
	private static final double SQUID_ATTACHMENT_Y = -0.2d;
	private static final double SQUID_SCALE = 0.8d;
	private static final int SCENE_SCALE = 20;

	private List<FluidStack> fluids;

	public AnimatedSquidSpout withFluids(List<FluidStack> fluids) {
		this.fluids = fluids;
		return this;
	}

	@Override
	public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
		PoseStack matrixStack = graphics.pose();
		matrixStack.pushPose();
		matrixStack.translate(xOffset, yOffset, 100);

		matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
		matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));
		int scale = SCENE_SCALE;

		blockElement(AllBlocks.SPOUT.getDefaultState())
			.scale(scale)
			.render(graphics);

		float cycle = (AnimationTickHolder.getRenderTime() - offset * 8) % 30;
		float squeeze = cycle < 20 ? Mth.sin((float) (cycle / 20f * Math.PI)) : 0;
		squeeze *= 20;

		matrixStack.pushPose();

		blockElement(AllPartialModels.SPOUT_TOP)
			.scale(scale)
			.render(graphics);
		matrixStack.translate(0, -3 * squeeze / 32f, 0);
		blockElement(AllPartialModels.SPOUT_MIDDLE)
			.scale(scale)
			.render(graphics);
		matrixStack.translate(0, -3 * squeeze / 32f, 0);
		blockElement(AllPartialModels.SPOUT_BOTTOM)
			.scale(scale)
			.render(graphics);
		matrixStack.translate(0, -3 * squeeze / 32f, 0);

		matrixStack.popPose();

		Squid squid = SquidJeiRenderer.getOrCreateSquid();
		if (squid != null) {
			entityElement(squid)
				.atLocal(0.5d, SQUID_ATTACHMENT_Y, 0.5d)
				.scale(scale)
				.scaleEntity(SQUID_SCALE)
				.stateModifier(SquidJeiRenderer::animateTentacles)
				.render(graphics);
		}

		blockElement(AllBlocks.DEPOT.getDefaultState())
			.atLocal(0, 2, 0)
			.scale(scale)
			.render(graphics);

		DEFAULT_LIGHTING.applyLighting();
		BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance()
			.getBuilder());
		matrixStack.pushPose();
		UIRenderHelper.flipForGuiRender(matrixStack);
		matrixStack.scale(16, 16, 16);
		float from = 3f / 16f;
		float to = 17f / 16f;
		FluidStack fluidStack = fluids.get(0);
		ForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluidStack, from, from, from, to, to, to,
			graphics.bufferSource(), matrixStack, LightTexture.FULL_BRIGHT, false, true);
		matrixStack.popPose();

		matrixStack.pushPose();
		matrixStack.translate(scale / 2f, scale * 1.5f, scale / 2f);
		UIRenderHelper.flipForGuiRender(matrixStack);
		matrixStack.scale(16, 16, 16);

		BlockState inkBlock = Blocks.BLACK_CONCRETE.defaultBlockState();
		float renderTime = AnimationTickHolder.getRenderTime();
		float strength = squeeze / 20f;
		int particleCount = 6;
		float cycleDuration = 18f;
		float particleSize = 0.07f + strength * 0.05f;

		for (int i = 0; i < particleCount; i++) {
			float phase = ((renderTime + i * cycleDuration / particleCount) % cycleDuration) / cycleDuration;
			float y = 1.875f - phase * 1.25f;
			float xJitter = (float) Math.sin(i * 1.7f + renderTime * 0.05f) * 0.10f * strength;
			float zJitter = (float) Math.cos(i * 2.3f + renderTime * 0.05f) * 0.10f * strength;

			matrixStack.pushPose();
			matrixStack.translate(xJitter, y, zJitter);
			matrixStack.scale(particleSize, particleSize, particleSize);
			GuiGameElement.of(inkBlock)
				.atLocal(-0.5, 0.5, -0.5)
				.render(graphics);
			matrixStack.popPose();
		}
		matrixStack.popPose();
		buffer.endBatch();
		Lighting.setupFor3DItems();

		matrixStack.popPose();
	}
}
