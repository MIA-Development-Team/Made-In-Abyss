package com.altnoir.mia.datagen;

import com.altnoir.mia.compat.curios.MiaCuriosProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class DataGenerators {
    public static void gatherData(DataGenerator generator, ExistingFileHelper existingFileHelper, CompletableFuture<HolderLookup.Provider> lookupProvider, boolean includeServer, boolean includeClient) {
        PackOutput packOutput = generator.getPackOutput();

//        generators.addProvider(includeServer, new LootTableProvider(packOutput, Collections.emptySet(),
//                List.of(new LootTableProvider.SubProviderEntry(MiaBlockLootTableProvider::new, LootContextParamSets.BLOCK)), lookupProvider));
        generator.addProvider(includeServer, MiaLootTableProvider.create(packOutput, lookupProvider));
        generator.addProvider(includeServer, new MiaRecipeProvider(packOutput, lookupProvider));

        BlockTagsProvider blockTagsProvider = new MiaBlockTagProvider(packOutput, lookupProvider, existingFileHelper);
        ItemTagsProvider itemTagsProvider = new MiaItemTagProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper);
        generator.addProvider(includeServer, blockTagsProvider);
        generator.addProvider(includeServer, itemTagsProvider);

        var worldGenProvider = new MiaWorldGenProvider(packOutput, lookupProvider);
        generator.addProvider(includeServer, worldGenProvider);

        generator.addProvider(includeServer, new MiaDataMapProvider(packOutput, lookupProvider));
        generator.addProvider(includeServer, new MiaPaintingVariantTagsProvider(packOutput, worldGenProvider.getRegistryProvider(), existingFileHelper));
        generator.addProvider(includeServer, new MiaBiomeTagsProvider(packOutput, worldGenProvider.getRegistryProvider(), existingFileHelper));

        generator.addProvider(includeServer, new MiaCuriosProvider(packOutput, existingFileHelper, lookupProvider));
        generator.addProvider(includeServer, new MiaCurseDataProvider(packOutput, lookupProvider));
        generator.addProvider(includeServer, new MiaTrialSpawnerProvider(packOutput, lookupProvider));

        generator.addProvider(includeClient, new MiaBlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(includeClient, new MiaItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(includeClient, new MiaParticleProvider(packOutput, existingFileHelper));

        generator.addProvider(includeClient, new MiaSoundsProvider(packOutput, existingFileHelper));
        generator.addProvider(includeClient, new MiaLangProvider(packOutput));
    }
}