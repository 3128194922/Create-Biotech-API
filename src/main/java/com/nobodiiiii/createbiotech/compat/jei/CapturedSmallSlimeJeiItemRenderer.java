package com.nobodiiiii.createbiotech.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nobodiiiii.createbiotech.foundation.render.ItemEntityElement;
import org.jetbrains.annotations.Nullable;

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
	private static final int SLIME_SIZE = 2;

	private static final IClientItemExtensions ITEM_EXTENSIONS = new IClientItemExtensions() {
		private CapturedSmallSlimeJeiItemRenderer renderer;

		@Override
		public BlockEntityWithoutLevelRenderer getCustomRenderer() {
			if (renderer == null)
				renderer = new CapturedSmallSlimeJeiItemRenderer();
			return renderer;
		}
	};

	@Nullable
	private Slime cachedSlime;
	@Nullable
	private Level cachedLevel;

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

		Slime slime = getOrCreateSlime(level);
		if (slime == null)
			return;

		ItemEntityElement.of(slime)
			.asItem(displayContext, packedLight)
			.render(poseStack, buffer);
	}

	@Nullable
	private Slime getOrCreateSlime(Level level) {
		if (cachedSlime != null && cachedLevel == level)
			return cachedSlime;

		Slime slime = EntityType.SLIME.create(level);
		if (slime == null)
			return null;
		slime.setNoAi(true);
		slime.setSize(SLIME_SIZE, false);
		slime.tickCount = 0;
		cachedLevel = level;
		cachedSlime = slime;
		return slime;
	}
}
