package com.nobodiiiii.createbiotech.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nobodiiiii.createbiotech.client.render.SlimeMimicRenderLayer;
import com.nobodiiiii.createbiotech.content.slimemimic.SlimeMimicHandler;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

	@WrapOperation(
		method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
	private void createBiotech$skipBaseBodyForSlimeMimic(EntityModel<?> model, PoseStack poseStack,
		VertexConsumer consumer, int packedLight, int overlay, float red, float green, float blue, float alpha,
		Operation<Void> original, @Local(argsOnly = true) LivingEntity entity) {
		if (SlimeMimicHandler.isSlimeMimic(entity) && SlimeMimicRenderLayer.supportsSlimeMimicModel(model)
			&& !entity.isInvisible())
			return;
		original.call(model, poseStack, consumer, packedLight, overlay, red, green, blue, alpha);
	}
}
