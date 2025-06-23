package com.altnoir.mia.worldgen.biome;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.worldgen.MiaFeatureUtils;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.level.material.Fluids;

public class AbyssBrinkFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> SPRING_WATER = MiaFeatureUtils.resourceKey("spring_water");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        MiaFeatureUtils.register(
                context,
                SPRING_WATER,
                Feature.SPRING,
                new SpringConfiguration(
                        Fluids.WATER.defaultFluidState(),
                        true,
                        4,
                        1,
                        HolderSet.direct(
                                Block::builtInRegistryHolder,
                                MiaBlocks.ABYSS_ANDESITE.get(),
                                Blocks.STONE,
                                Blocks.GRANITE,
                                Blocks.DIORITE,
                                Blocks.ANDESITE,
                                Blocks.DEEPSLATE,
                                Blocks.TUFF,
                                Blocks.CALCITE,
                                Blocks.DIRT
                        )
                )
        );
    }
}
