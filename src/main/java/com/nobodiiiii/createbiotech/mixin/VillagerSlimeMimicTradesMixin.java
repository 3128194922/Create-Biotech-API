package com.nobodiiiii.createbiotech.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.nobodiiiii.createbiotech.content.slimemimic.SlimeMimicVillagerTrades;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.trading.MerchantOffers;

@Mixin(Villager.class)
public abstract class VillagerSlimeMimicTradesMixin {

	@Inject(method = "setVillagerData", at = @At("HEAD"))
	private void createBiotech$clearStoredResultsOnProfessionChange(VillagerData villagerData, CallbackInfo ci) {
		Villager villager = (Villager) (Object) this;
		if (villager.getVillagerData().getProfession() != villagerData.getProfession())
			SlimeMimicVillagerTrades.clearStoredOriginalResults(villager);
	}

	@Inject(method = "setOffers", at = @At("TAIL"))
	private void createBiotech$rewriteOffersAfterSet(MerchantOffers offers, CallbackInfo ci) {
		SlimeMimicVillagerTrades.rewriteSellItems((Villager) (Object) this);
	}

	@Inject(method = "updateTrades", at = @At("TAIL"))
	private void createBiotech$rewriteOffersAfterTradeRefresh(CallbackInfo ci) {
		SlimeMimicVillagerTrades.rewriteSellItems((Villager) (Object) this);
	}
}
