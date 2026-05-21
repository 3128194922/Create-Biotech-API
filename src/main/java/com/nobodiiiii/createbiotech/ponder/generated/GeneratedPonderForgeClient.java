package com.nobodiiiii.createbiotech.ponder.generated;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "create_biotech", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class GeneratedPonderForgeClient {
    private GeneratedPonderForgeClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> PonderIndex.addPlugin(new GeneratedPonderPlugin()));
    }
}
