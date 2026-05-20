package com.nobodiiiii.createbiotech.content.evokerenchantingchamber;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class EvokerEnchantingChamberItemRenderer extends CustomRenderedItemModelRenderer {

	private static final float ITEM_SCALE = 0.65f;
	private static final float ITEM_Y_OFFSET = -0.18f;

	@Nullable
	private EvokerEnchantingChamberBlockEntity cachedBlockEntity;
	@Nullable
	private ClientLevel cachedLevel;

	@Override
	protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
		ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		EvokerEnchantingChamberBlockEntity blockEntity = getOrCreateBlockEntity(Minecraft.getInstance().level);
		if (blockEntity == null)
			return;

		BlockEntityRenderer<EvokerEnchantingChamberBlockEntity> blockEntityRenderer =
			Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);
		if (!(blockEntityRenderer instanceof EvokerEnchantingChamberRenderer chamberRenderer))
			return;

		boolean guiLighting = transformType == ItemDisplayContext.GUI;
		ms.pushPose();
		ms.translate(0, ITEM_Y_OFFSET, 0);
		ms.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
		ms.translate(-0.5f, -0.5f, -0.5f);

		if (guiLighting)
			Lighting.setupForEntityInInventory();
		try {
			chamberRenderer.render(blockEntity, Minecraft.getInstance().getFrameTime(), ms, buffer, light, overlay);
		} finally {
			if (guiLighting)
				Lighting.setupFor3DItems();
			ms.popPose();
		}
	}

	private @Nullable EvokerEnchantingChamberBlockEntity getOrCreateBlockEntity(@Nullable Level level) {
		if (!(level instanceof ClientLevel clientLevel))
			return cachedBlockEntity;

		if (cachedBlockEntity == null || cachedLevel != clientLevel) {
			cachedLevel = clientLevel;
			cachedBlockEntity = new EvokerEnchantingChamberBlockEntity(BlockPos.ZERO, createRenderState());
		}

		cachedBlockEntity.setLevel(clientLevel);
		return cachedBlockEntity;
	}

	private static BlockState createRenderState() {
		return com.nobodiiiii.createbiotech.registry.CBBlocks.EVOKER_ENCHANTING_CHAMBER.get()
			.defaultBlockState()
			.setValue(EvokerEnchantingChamberBlock.FACING, Direction.NORTH)
			.setValue(EvokerEnchantingChamberBlock.HALF, DoubleBlockHalf.LOWER);
	}
}
