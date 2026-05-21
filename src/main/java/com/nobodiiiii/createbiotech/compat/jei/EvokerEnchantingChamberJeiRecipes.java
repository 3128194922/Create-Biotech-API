package com.nobodiiiii.createbiotech.compat.jei;

import java.util.ArrayList;
import java.util.List;

import com.nobodiiiii.createbiotech.CreateBiotech;
import com.nobodiiiii.createbiotech.content.experience.ExperienceConstants;
import com.nobodiiiii.createbiotech.content.squidprinter.EnchantmentBookCopyItem;
import com.nobodiiiii.createbiotech.registry.CBItems;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.registries.ForgeRegistries;

public final class EvokerEnchantingChamberJeiRecipes {

	private EvokerEnchantingChamberJeiRecipes() {
	}

	public static List<EvokerEnchantingChamberJeiRecipe> create() {
		List<EvokerEnchantingChamberJeiRecipe> recipes = new ArrayList<>();
		for (ResourceLocation enchId : ForgeRegistries.ENCHANTMENTS.getKeys()
			.stream()
			.sorted()
			.toList()) {
			Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(enchId);
			if (enchantment == null)
				continue;
			int level = Math.max(1, enchantment.getMaxLevel());
			ItemStack templateBook = new ItemStack(Items.ENCHANTED_BOOK);
			EnchantedBookItem.addEnchantment(templateBook, new EnchantmentInstance(enchantment, level));

			ItemStack inputCopy = EnchantmentBookCopyItem.fromTemplate(templateBook, CBItems.ENCHANTMENT_BOOK_COPY.get());
			int xpCost = level * ExperienceConstants.CHAMBER_XP_PER_LEVEL;

			ResourceLocation id = CreateBiotech.asResource(
				"evoker_enchanting_chamber/" + enchId.getNamespace() + "_" + enchId.getPath() + "_" + level);
			recipes.add(new EvokerEnchantingChamberJeiRecipe(id, inputCopy, templateBook, xpCost));
		}
		return recipes;
	}
}
