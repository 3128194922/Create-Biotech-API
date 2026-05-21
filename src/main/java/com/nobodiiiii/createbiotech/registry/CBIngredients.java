package com.nobodiiiii.createbiotech.registry;

import com.nobodiiiii.createbiotech.CreateBiotech;
import com.nobodiiiii.createbiotech.content.cardboardbox.CapturedEntityBoxIngredient;

import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@EventBusSubscriber(modid = CreateBiotech.MOD_ID, bus = Bus.MOD)
public class CBIngredients {

	@SubscribeEvent
	public static void register(RegisterEvent event) {
		if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS))
			CraftingHelper.register(CreateBiotech.asResource("captured_entity_box"),
				CapturedEntityBoxIngredient.Serializer.INSTANCE);
	}

	private CBIngredients() {}
}
