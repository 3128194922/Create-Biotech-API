package com.nobodiiiii.createbiotech.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.nobodiiiii.createbiotech.content.slimemimic.SlimeMimicVillagerTrades;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffers;

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerSlimeMimicTradesMixin {

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void createBiotech$rewriteOffersAfterLoad(CompoundTag tag, CallbackInfo ci) {
		SlimeMimicVillagerTrades.rewriteSellItems((AbstractVillager) (Object) this);
	}

	@Inject(method = "getOffers", at = @At("RETURN"), cancellable = true)
	private void createBiotech$rewriteOffersOnAccess(CallbackInfoReturnable<MerchantOffers> cir) {
		AbstractVillager villager = (AbstractVillager) (Object) this;
		SlimeMimicVillagerTrades.rewriteSellItems(villager);
		MerchantOffers offers = ((AbstractVillagerAccessor) villager).createBiotech$getOffersField();
		if (offers != null)
			cir.setReturnValue(offers);
	}
}
