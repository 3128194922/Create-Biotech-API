package com.nobodiiiii.createbiotech.registry;

import com.nobodiiiii.createbiotech.CreateBiotech;
import com.nobodiiiii.createbiotech.content.ghasthotairballoon.GhastHotAirBalloonContraption;
import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;

import net.minecraft.core.Holder.Reference;
import net.minecraft.core.Registry;

public class CBContraptionTypes {

	public static final Reference<ContraptionType> GHAST_HOT_AIR_BALLOON =
		register("ghast_hot_air_balloon", GhastHotAirBalloonContraption::new);

	private static Reference<ContraptionType> register(String name,
		java.util.function.Supplier<? extends com.simibubi.create.content.contraptions.Contraption> factory) {
		ContraptionType type = new ContraptionType(factory);
		return Registry.registerForHolder(CreateBuiltInRegistries.CONTRAPTION_TYPE,
			CreateBiotech.asResource(name), type);
	}

	public static void init() {}

	private CBContraptionTypes() {}
}
