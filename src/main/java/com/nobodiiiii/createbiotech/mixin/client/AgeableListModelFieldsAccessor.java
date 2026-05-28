package com.nobodiiiii.createbiotech.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.model.AgeableListModel;

@Mixin(AgeableListModel.class)
public interface AgeableListModelFieldsAccessor {

	@Accessor("scaleHead")
	boolean createBiotech$scaleHead();

	@Accessor("babyYHeadOffset")
	float createBiotech$getBabyYHeadOffset();

	@Accessor("babyZHeadOffset")
	float createBiotech$getBabyZHeadOffset();

	@Accessor("babyHeadScale")
	float createBiotech$getBabyHeadScale();

	@Accessor("babyBodyScale")
	float createBiotech$getBabyBodyScale();

	@Accessor("bodyYOffset")
	float createBiotech$getBodyYOffset();
}
