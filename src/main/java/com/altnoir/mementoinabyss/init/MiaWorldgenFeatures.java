package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.BlockTrunkConfiguration;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.BlockTrunkFeature;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.ClusterConfiguration;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.ClusterFeature;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.GiantFossilizedStumpConfiguration;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.GiantFossilizedStumpFeature;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.Layer2Feature;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.Layer2FeatureConfiguration;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.LongVinesConfiguration;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.LongVinesFeature;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.PrimoHugeFungusConfiguration;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.PrimoHugeFungusFeature;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.ReedPoolFeature;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.SegmentedVegetationProvider;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.SlabRuinsConfiguration;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.SlabRuinsFeature;
import com.altnoir.mementoinabyss.infrastructure.worldgen.tree.DuskveilFoliagePlacer;
import com.altnoir.mementoinabyss.infrastructure.worldgen.tree.InvertedTreeFeature;
import com.altnoir.mementoinabyss.infrastructure.worldgen.tree.VerdantTreeConfiguration;
import com.altnoir.mementoinabyss.infrastructure.worldgen.tree.VerdantTreeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MiaWorldgenFeatures {
    private static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, MementoInAbyss.ID);
    private static final DeferredRegister<BlockStateProviderType<?>> STATE_PROVIDERS =
            DeferredRegister.create(Registries.BLOCK_STATE_PROVIDER_TYPE, MementoInAbyss.ID);
    private static final DeferredRegister<FoliagePlacerType<?>> FOLIAGE_PLACERS =
            DeferredRegister.create(Registries.FOLIAGE_PLACER_TYPE, MementoInAbyss.ID);
    public static final DeferredHolder<
                    FoliagePlacerType<?>, FoliagePlacerType<DuskveilFoliagePlacer>>
            DUSKVEIL_FOLIAGE =
                    FOLIAGE_PLACERS.register(
                            "duskveil", () -> new FoliagePlacerType<>(DuskveilFoliagePlacer.CODEC));
    public static final DeferredHolder<
                    BlockStateProviderType<?>, BlockStateProviderType<SegmentedVegetationProvider>>
            SEGMENTED_VEGETATION =
                    STATE_PROVIDERS.register(
                            "segmented_vegetation",
                            () -> new BlockStateProviderType<>(SegmentedVegetationProvider.CODEC));
    public static final DeferredHolder<Feature<?>, ReedPoolFeature> REED_POOL =
            FEATURES.register(
                    "reed_pool", () -> new ReedPoolFeature(VegetationPatchConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, Layer2Feature> LAYER2_FEATURE =
            FEATURES.register(
                    "layer2_feature", () -> new Layer2Feature(Layer2FeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, InvertedTreeFeature> INVERTED_TREE =
            FEATURES.register(
                    "inverted_tree", () -> new InvertedTreeFeature(TreeConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, VerdantTreeFeature> VERDANT_TREE =
            FEATURES.register(
                    "verdant_tree", () -> new VerdantTreeFeature(VerdantTreeConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, GiantFossilizedStumpFeature>
            GIANT_FOSSILIZED_STUMP =
                    FEATURES.register(
                            "giant_fossilized_stump",
                            () ->
                                    new GiantFossilizedStumpFeature(
                                            GiantFossilizedStumpConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, BlockTrunkFeature> BLOCK_TRUNK =
            FEATURES.register(
                    "block_trunk", () -> new BlockTrunkFeature(BlockTrunkConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, LongVinesFeature> LONG_VINES =
            FEATURES.register(
                    "long_vines", () -> new LongVinesFeature(LongVinesConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, ClusterFeature> CLUSTER =
            FEATURES.register(
                    "cluster", () -> new ClusterFeature(ClusterConfiguration.CODEC, false));
    public static final DeferredHolder<Feature<?>, ClusterFeature> BIG_CLUSTER =
            FEATURES.register(
                    "big_cluster", () -> new ClusterFeature(ClusterConfiguration.CODEC, true));
    public static final DeferredHolder<Feature<?>, SlabRuinsFeature> SLAB_RUINS =
            FEATURES.register(
                    "slab_ruins", () -> new SlabRuinsFeature(SlabRuinsConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, PrimoHugeFungusFeature> HUGE_PRIMO_FUNGUS =
            FEATURES.register(
                    "huge_primo_fungus",
                    () -> new PrimoHugeFungusFeature(PrimoHugeFungusConfiguration.CODEC));

    public static void register(IEventBus bus) {
        FEATURES.register(bus);
        STATE_PROVIDERS.register(bus);
        FOLIAGE_PLACERS.register(bus);
    }

    private MiaWorldgenFeatures() {}
}
