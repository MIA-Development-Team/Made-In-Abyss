package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MIA.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var gen = event.getGenerator();
        var packOutput = gen.getPackOutput();
        var existingFileHelper = event.getExistingFileHelper();

        gen.addProvider(event.includeServer(), new MIARecipesProvider(packOutput));
        gen.addProvider(event.includeServer(), new MIABlockStatesProvider(packOutput, existingFileHelper));
        gen.addProvider(event.includeServer(), new MIAItemModelProvider(packOutput, existingFileHelper));
        gen.addProvider(event.includeClient(), new MIALangProvider(packOutput, MIA.MODID, "en_us", gen));
    }
}
