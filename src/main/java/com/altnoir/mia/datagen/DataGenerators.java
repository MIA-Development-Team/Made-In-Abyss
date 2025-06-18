package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.compat.curios.MIACuriosProvider;
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

        generators.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(MiaBlockLootTableProvider::new, LootContextParamSets.BLOCK)), lookupProvider));
        generators.addProvider(event.includeServer(), new MiaRecipeProvider(packOutput, lookupProvider));

        BlockTagsProvider blockTagsProvider = new MiaBlockTagProvider(packOutput, lookupProvider, existingFileHelper);
        ItemTagsProvider itemTagsProvider = new MiaItemTagProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper);
        generators.addProvider(event.includeServer(), blockTagsProvider);
        generators.addProvider(event.includeServer(), itemTagsProvider);
        generators.addProvider(event.includeServer(), new MiaDataMapProvider(packOutput, lookupProvider));

        generators.addProvider(event.includeServer(), new MiaWorldGenProvider(packOutput, lookupProvider));
        generators.addProvider(event.includeServer(), new MIACuriosProvider(packOutput, existingFileHelper, lookupProvider));
        generators.addProvider(event.includeServer(), new MiaCurseDataProvider(packOutput, lookupProvider));

        generators.addProvider(event.includeClient(), new MiaBlockStateProvider(packOutput, existingFileHelper));
        generators.addProvider(event.includeClient(), new MiaItemModelProvider(packOutput, existingFileHelper));
        generators.addProvider(event.includeClient(), new MiaLangProvider(packOutput, "en_us"));
    }
}
