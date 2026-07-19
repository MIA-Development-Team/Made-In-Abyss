package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.worldgen.tree.InvertedTreeFeature;
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
    public static void register(IEventBus bus) { FEATURES.register(bus); }
    private MiaWorldgenFeatures() {}
}
