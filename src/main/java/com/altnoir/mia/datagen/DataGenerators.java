package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = MIA.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generators = event.getGenerator();
        PackOutput packOutput = generators.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        //generators.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(),
        //        List.of(new LootTableProvider.SubProviderEntry(MIABlockLootTableProvider::new, LootContextParamSets.BLOCK)), lookupProvider));
        generators.addProvider(event.includeServer(), new MIARecipeProvider(packOutput, lookupProvider));

        BlockTagsProvider blockTagsProvider = new MIABlockTagProvider(packOutput, lookupProvider, existingFileHelper);
        ItemTagsProvider itemTagsProvider = new MIAItemTagProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper);
        generators.addProvider(event.includeServer(), blockTagsProvider);
        generators.addProvider(event.includeServer(), itemTagsProvider);

        generators.addProvider(event.includeServer(), new MIADatapackProvider(packOutput, lookupProvider));

        generators.addProvider(event.includeClient(), new MIABlockStateProvider(packOutput, existingFileHelper));
        generators.addProvider(event.includeClient(), new MIAItemModelProvider(packOutput, existingFileHelper));
    }
}
