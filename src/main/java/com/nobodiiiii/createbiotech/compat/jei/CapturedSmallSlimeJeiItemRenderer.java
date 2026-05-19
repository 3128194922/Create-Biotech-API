package com.nobodiiiii.createbiotech.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nobodiiiii.createbiotech.foundation.render.ItemEntityElement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

@OnlyIn(Dist.CLIENT)
public class CapturedSmallSlimeJeiItemRenderer extends BlockEntityWithoutLevelRenderer {
	private static final int GUI_LIGHT = 15728880;
	private static final float ANGLE_X = 0.75f;
	private static final float ANGLE_Y = 0.6f;

	private static final IClientItemExtensions ITEM_EXTENSIONS = new IClientItemExtensions() {
		private CapturedSmallSlimeJeiItemRenderer renderer;

		@Override
		public BlockEntityWithoutLevelRenderer getCustomRenderer() {
			if (renderer == null)
				renderer = new CapturedSmallSlimeJeiItemRenderer();
			return renderer;
		}
	};

	private final SlimeEntityDrawable slimeDrawable =
		new SlimeEntityDrawable(16, 16, 10, 2, ANGLE_X, ANGLE_Y, -1, EntityType.SLIME);

	private CapturedSmallSlimeJeiItemRenderer() {
		super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
	}

	public static IClientItemExtensions itemExtensions() {
		return ITEM_EXTENSIONS;
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
		MultiBufferSource buffer, int packedLight, int packedOverlay) {
		Level level = Minecraft.getInstance().level;
		if (level == null)
			return;

		Slime slime = slimeDrawable.getOrCreateSlime(level);
		if (slime == null)
			return;

		poseStack.pushPose();
		applyTransform(displayContext, poseStack);

		boolean gui = displayContext == ItemDisplayContext.GUI;
		ItemEntityElement.of(slime)
			.lighting(gui ? ItemEntityElement.DEFAULT_GUI_LIGHTING : null)
			.packedLight(gui ? GUI_LIGHT : packedLight)
			.partialTicks(1.0f)
			.inventoryLike(ANGLE_X, ANGLE_Y)
			.render(poseStack, buffer);

		poseStack.popPose();
	}

	private static void applyTransform(ItemDisplayContext displayContext, PoseStack poseStack) {
		if (displayContext == ItemDisplayContext.GUI) {
			poseStack.translate(0.5f, 0.2f, 0.5f);
			poseStack.scale(0.7f, 0.7f, -0.7f);
			return;
		}
		poseStack.translate(0.5f, 0.45f, 0.5f);
		float scale = displayContext == ItemDisplayContext.GROUND ? 0.75f : 0.55f;
		poseStack.scale(scale, scale, -scale);
	}
}
