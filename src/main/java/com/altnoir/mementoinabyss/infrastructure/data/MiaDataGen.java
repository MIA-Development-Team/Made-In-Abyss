package com.altnoir.mementoinabyss.infrastructure.data;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.foundation.utility.FilesHelper;
import com.altnoir.mementoinabyss.infrastructure.worldgen.MiaPaintingVariants;
import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.MiaBiomes;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensionTypes;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensions;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaWorldClocks;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.GreatFaultFeatures;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.GreatFaultPlacements;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.MiaAbyssFeatures;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.MiaAbyssPlacements;
import com.altnoir.mementoinabyss.infrastructure.worldgen.noise.MiaDensityFunctions;
import com.altnoir.mementoinabyss.infrastructure.worldgen.noise.MiaNoiseData;
import com.altnoir.mementoinabyss.infrastructure.worldgen.noise.MiaNoiseGeneratorSettings;
import com.altnoir.mementoinabyss.infrastructure.worldgen.structure.MiaProcessorLists;
import com.altnoir.mementoinabyss.infrastructure.worldgen.structure.MiaStructurePools;
import com.altnoir.mementoinabyss.infrastructure.worldgen.structure.MiaStructureSets;
import com.altnoir.mementoinabyss.infrastructure.worldgen.structure.MiaStructures;
import com.altnoir.mementoinabyss.infrastructure.worldgen.tree.MiaTreeFeatures;
import com.altnoir.mementoinabyss.init.MiaBlocks;
import com.altnoir.mementoinabyss.init.MiaTags;
import com.tterrag.registrate.providers.ProviderType;
import java.util.function.BiConsumer;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class MiaDataGen {
    public static void gatherDataHighPriority(GatherDataEvent.Client event) {
        if (event.getModContainer().getModId().contains(MementoInAbyss.ID))
            addExtraRegistrateData();
    }

    public static void gatherData(GatherDataEvent.Client event) {
        if (!event.getModContainer().getModId().equals(MementoInAbyss.ID)) return;

        event.createProvider(MiaCurseDataProvider::new);
        event.createProvider(MiaCuriosDataProvider::new);
        event.createProvider(MiaArtifactEnhancementRecipeProvider.Runner::new);
        event.createProvider(MiaLootTableProvider::create);
        event.createDatapackRegistryObjects(
                new RegistrySetBuilder()
                        .add(Registries.WORLD_CLOCK, MiaWorldClocks::bootstrap)
                        .add(Registries.DIMENSION_TYPE, MiaDimensionTypes::bootstrap)
                        .add(Registries.NOISE, MiaNoiseData::bootstrap)
                        .add(Registries.DENSITY_FUNCTION, MiaDensityFunctions::bootstrap)
                        .add(Registries.NOISE_SETTINGS, MiaNoiseGeneratorSettings::bootstrap)
                        .add(
                                Registries.CONFIGURED_FEATURE,
                                context -> {
                                    MiaAbyssFeatures.bootstrap(context);
                                    MiaTreeFeatures.bootstrap(context);
                                    GreatFaultFeatures.bootstrap(context);
                                })
                        .add(
                                Registries.PLACED_FEATURE,
                                context -> {
                                    MiaAbyssPlacements.bootstrap(context);
                                    GreatFaultPlacements.bootstrap(context);
                                })
                        .add(Registries.BIOME, MiaBiomes::bootstrap)
                        .add(Registries.PROCESSOR_LIST, MiaProcessorLists::bootstrap)
                        .add(Registries.TEMPLATE_POOL, MiaStructurePools::bootstrap)
                        .add(Registries.STRUCTURE, MiaStructures::bootstrap)
                        .add(Registries.STRUCTURE_SET, MiaStructureSets::bootstrap)
                        .add(Registries.PAINTING_VARIANT, MiaPaintingVariants::bootstrap)
                        .add(Registries.LEVEL_STEM, MiaDimensions::bootstrap));
    }

    private static void addExtraRegistrateData() {
        MementoInAbyss.registrate()
                .addDataGenerator(
                        ProviderType.LANG,
                        provider -> {
                            BiConsumer<String, String> langConsumer = provider::add;

                            provideDefaultLang("interface", langConsumer);
                            provideDefaultLang("tooltips", langConsumer);
                        });
        MementoInAbyss.registrate()
                .addDataGenerator(
                        ProviderType.BLOCK_TAGS,
                        provider ->
                                provider.tag(MiaTags.BlockTags.ABYSS_MUD_ORE_REPLACEABLE.tag)
                                        .add(Blocks.MUD));
        MementoInAbyss.registrate()
                .addDataGenerator(
                        ProviderType.ITEM_TAGS,
                        provider -> {
                            provider.tag(MiaTags.ItemTags.SKYFOG_LOGS.tag)
                                    .add(
                                            MiaBlocks.SKYFOG_LOG.asItem(),
                                            MiaBlocks.SKYFOG_WOOD.asItem(),
                                            MiaBlocks.STRIPPED_SKYFOG_LOG.asItem(),
                                            MiaBlocks.STRIPPED_SKYFOG_WOOD.asItem());
                            provider.tag(MiaTags.ItemTags.VERDANT_STEMS.tag)
                                    .add(
                                            MiaBlocks.VERDANT_STEM.asItem(),
                                            MiaBlocks.VERDANT_HYPHAE.asItem(),
                                            MiaBlocks.STRIPPED_VERDANT_STEM.asItem(),
                                            MiaBlocks.STRIPPED_VERDANT_HYPHAE.asItem());
                            provider.tag(MiaTags.ItemTags.INVERTED_LOGS.tag)
                                    .add(
                                            MiaBlocks.INVERTED_LOG.asItem(),
                                            MiaBlocks.INVERTED_WOOD.asItem(),
                                            MiaBlocks.STRIPPED_INVERTED_LOG.asItem(),
                                            MiaBlocks.STRIPPED_INVERTED_WOOD.asItem());
                        });
    }

    private static void provideDefaultLang(String fileName, BiConsumer<String, String> consumer) {
        var path = "assets/mementoinabyss/lang/default/" + fileName + ".json";
        var jsonElement = FilesHelper.loadJsonResource(path);
        if (jsonElement == null) {
            throw new IllegalStateException(
                    String.format("Could not find default lang file: %s", path));
        }
        var jsonObject = jsonElement.getAsJsonObject();
        for (var entry : jsonObject.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue().getAsString();
            consumer.accept(key, value);
        }
    }
}
