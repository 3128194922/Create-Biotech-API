package com.nobodiiiii.createbiotech.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.nobodiiiii.createbiotech.content.slimemimic.SlimeMimicHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;

@Mixin(SpawnEggItem.class)
public abstract class SpawnEggItemMixin {

	@WrapOperation(method = "useOn",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/EntityType;spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;ZZ)Lnet/minecraft/world/entity/Entity;"))
	private Entity createBiotech$markBlockSpawnedEntity(EntityType<?> entityType, ServerLevel level, ItemStack stack,
		Player player, BlockPos pos, MobSpawnType spawnType, boolean alignSpawn, boolean invertYOffset,
		Operation<Entity> original, @Local(argsOnly = true) UseOnContext context) {
		Entity entity = original.call(entityType, level, stack, player, pos, spawnType, alignSpawn, invertYOffset);
		if (SlimeMimicHandler.shouldSlimeifySpawn(context.getPlayer(), context.getHand()))
			SlimeMimicHandler.markSpawnedEntity(entity);
		return entity;
	}

	@WrapOperation(method = "use",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/EntityType;spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;ZZ)Lnet/minecraft/world/entity/Entity;"))
	private Entity createBiotech$markFluidSpawnedEntity(EntityType<?> entityType, ServerLevel level, ItemStack stack,
		Player player, BlockPos pos, MobSpawnType spawnType, boolean alignSpawn, boolean invertYOffset,
		Operation<Entity> original, @Local(argsOnly = true) InteractionHand usedHand) {
		Entity entity = original.call(entityType, level, stack, player, pos, spawnType, alignSpawn, invertYOffset);
		if (SlimeMimicHandler.shouldSlimeifySpawn(player, usedHand))
			SlimeMimicHandler.markSpawnedEntity(entity);
		return entity;
	}
}
