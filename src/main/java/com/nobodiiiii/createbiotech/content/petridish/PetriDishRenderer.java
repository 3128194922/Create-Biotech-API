package com.nobodiiiii.createbiotech.content.petridish;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PetriDishRenderer extends SmartBlockEntityRenderer<PetriDishBlockEntity> {

	private static final BlockState SLIME_BLOCK = Blocks.SLIME_BLOCK.defaultBlockState();
	private static final float SLIME_BASE_Y = 4.25f / 16.0f;
	private static final float[] STAGE_SCALES = { 0.0f, 0.28f, 0.42f, 0.56f, 0.70f };

	private final BlockRenderDispatcher blockRenderer;

	public PetriDishRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
		blockRenderer = context.getBlockRenderDispatcher();
	}

	@Override
	protected void renderSafe(PetriDishBlockEntity be, float partialTicks, PoseStack poseStack,
		MultiBufferSource buffer, int packedLight, int packedOverlay) {
		super.renderSafe(be, partialTicks, poseStack, buffer, packedLight, packedOverlay);

		int growthStage = be.getSlimeGrowthStage();
		if (growthStage <= 0)
			return;

		float scale = STAGE_SCALES[growthStage];
		float min = 0.5f - scale / 2.0f;

		poseStack.pushPose();
		poseStack.translate(min, SLIME_BASE_Y, min);
		poseStack.scale(scale, scale, scale);
		blockRenderer.renderSingleBlock(SLIME_BLOCK, poseStack, buffer, packedLight, packedOverlay);
		poseStack.popPose();
	}
}
