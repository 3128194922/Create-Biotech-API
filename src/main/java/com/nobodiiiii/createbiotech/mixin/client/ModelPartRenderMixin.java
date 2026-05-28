package com.nobodiiiii.createbiotech.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nobodiiiii.createbiotech.client.render.SlimeMimicRenderLayer;

import net.minecraft.client.model.geom.ModelPart;

@Mixin(ModelPart.class)
public abstract class ModelPartRenderMixin {

	@Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",
		at = @At("HEAD"), cancellable = true)
	private void createBiotech$redirectSlimeMimicPartRender(PoseStack poseStack, VertexConsumer consumer,
		int packedLight, int overlay, float red, float green, float blue, float alpha, CallbackInfo ci) {
		if (SlimeMimicRenderLayer.interceptModelPart((ModelPart) (Object) this, poseStack, packedLight, overlay))
			ci.cancel();
	}
}
