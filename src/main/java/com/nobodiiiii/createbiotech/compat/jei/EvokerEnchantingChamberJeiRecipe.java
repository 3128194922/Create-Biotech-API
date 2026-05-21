package com.nobodiiiii.createbiotech.compat.jei;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record EvokerEnchantingChamberJeiRecipe(ResourceLocation id, ItemStack inputCopy, ItemStack outputBook,
	int xpCost) {
}
