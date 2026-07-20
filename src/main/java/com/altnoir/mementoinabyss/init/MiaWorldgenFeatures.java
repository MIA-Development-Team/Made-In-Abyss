package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.worldgen.tree.InvertedTreeFeature;
import com.altnoir.mementoinabyss.worldgen.feature.BlockTrunkConfiguration;
import com.altnoir.mementoinabyss.worldgen.feature.BlockTrunkFeature;
import com.altnoir.mementoinabyss.worldgen.feature.LongVinesConfiguration;
import com.altnoir.mementoinabyss.worldgen.feature.LongVinesFeature;
import com.altnoir.mementoinabyss.worldgen.feature.ClusterConfiguration;
import com.altnoir.mementoinabyss.worldgen.feature.ClusterFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MiaWorldgenFeatures {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, MementoInAbyss.ID);
    public static final DeferredHolder<Feature<?>, InvertedTreeFeature> INVERTED_TREE = FEATURES.register(
            "inverted_tree", () -> new InvertedTreeFeature(TreeConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, BlockTrunkFeature> BLOCK_TRUNK = FEATURES.register(
            "block_trunk", () -> new BlockTrunkFeature(BlockTrunkConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, LongVinesFeature> LONG_VINES = FEATURES.register(
            "long_vines", () -> new LongVinesFeature(LongVinesConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, ClusterFeature> CLUSTER = FEATURES.register(
            "cluster", () -> new ClusterFeature(ClusterConfiguration.CODEC, false));
    public static final DeferredHolder<Feature<?>, ClusterFeature> BIG_CLUSTER = FEATURES.register(
            "big_cluster", () -> new ClusterFeature(ClusterConfiguration.CODEC, true));
    public static void register(IEventBus bus) { FEATURES.register(bus); }
    private MiaWorldgenFeatures() {}
}
