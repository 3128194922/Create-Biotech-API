package com.nobodiiiii.createbiotech.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffers;

@Mixin(AbstractVillager.class)
public interface AbstractVillagerAccessor {

	@Accessor("offers")
	MerchantOffers createBiotech$getOffersField();

	@Accessor("offers")
	void createBiotech$setOffersField(MerchantOffers offers);
}
