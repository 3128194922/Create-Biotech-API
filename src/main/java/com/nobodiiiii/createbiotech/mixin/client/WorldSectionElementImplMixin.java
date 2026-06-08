package com.nobodiiiii.createbiotech.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.createmod.ponder.foundation.element.WorldSectionElementImpl;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

@Mixin(value = WorldSectionElementImpl.class, remap = false)
public abstract class WorldSectionElementImplMixin {

	@WrapOperation(
		method = "renderLayer(Lnet/createmod/ponder/api/level/PonderLevel;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/gui/GuiGraphics;FF)V",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
			remap = true),
		remap = true)
	private VertexConsumer createBiotech$delayPonderTranslucentSections(MultiBufferSource buffer, RenderType type,
		Operation<VertexConsumer> original) {
		if (type == RenderType.translucent() && buffer instanceof SuperRenderTypeBuffer superBuffer)
			return superBuffer.getLateBuffer(type);
		return original.call(buffer, type);
	}
}
