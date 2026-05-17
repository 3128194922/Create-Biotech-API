package com.nobodiiiii.createbiotech.content.biopackager;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BioPackagerRenderer extends SmartBlockEntityRenderer<BioPackagerBlockEntity> {

	public BioPackagerRenderer(Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(BioPackagerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		ItemStack renderedBox = be.getRenderedBox();
		float trayOffset = be.getTrayOffset(partialTicks);
		BlockState blockState = be.getBlockState();
		Direction facing = blockState.getValue(BioPackagerBlock.FACING).getOpposite();

		SuperByteBuffer sbb = CachedBuffers.partial(getHatchModel(be), blockState);
		sbb.translate(Vec3.atLowerCornerOf(facing.getNormal()).scale(.49999f))
			.rotateYCenteredDegrees(AngleHelper.horizontalAngle(facing))
			.rotateXCenteredDegrees(AngleHelper.verticalAngle(facing))
			.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.solid()));

		sbb = CachedBuffers.partial(AllPartialModels.PACKAGER_TRAY_REGULAR, blockState);
		sbb.translate(Vec3.atLowerCornerOf(facing.getNormal()).scale(trayOffset))
			.rotateYCenteredDegrees(facing.toYRot())
			.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));

		if (!renderedBox.isEmpty()) {
			ms.pushPose();
			var msr = TransformStack.of(ms);
			msr.translate(Vec3.atLowerCornerOf(facing.getNormal()).scale(trayOffset))
				.translate(.5f, .5f, .5f)
				.rotateYDegrees(facing.toYRot())
				.translate(0, 2 / 16f, 0)
				.scale(1.49f, 1.49f, 1.49f);
			Minecraft.getInstance()
				.getItemRenderer()
				.renderStatic(null, renderedBox, ItemDisplayContext.FIXED, false, ms, buffer, be.getLevel(), light,
					overlay, 0);
			ms.popPose();
		}
	}

	private static PartialModel getHatchModel(BioPackagerBlockEntity be) {
		return isHatchOpen(be) ? AllPartialModels.PACKAGER_HATCH_OPEN : AllPartialModels.PACKAGER_HATCH_CLOSED;
	}

	private static boolean isHatchOpen(BioPackagerBlockEntity be) {
		return be.animationTicks > (be.animationInward ? 1 : 5)
			&& be.animationTicks < BioPackagerBlockEntity.CYCLE - (be.animationInward ? 5 : 1);
	}
}
