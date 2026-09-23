package com.altnoir.mia.worldgen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.util.MiaUtil;
import com.altnoir.mia.worldgen.biome.great_fault.GreatFaultFeatures;
import com.altnoir.mia.worldgen.biome.the_abyss.TheAbyssFeatures;
import com.altnoir.mia.worldgen.feature.tree.MiaTreeFeatures;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class MiaFeatureUtils {
    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        MiaTreeFeatures.bootstrap(context);
        TheAbyssFeatures.bootstrap(context);
        GreatFaultFeatures.bootstrap(context);
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> resourceKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, MiaUtil.id(MIA.MOD_ID, name));
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> treeKey(String name) {
        return resourceKey("tree/" + name);
    }

    public static void register(
            BootstrapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key, Feature<NoneFeatureConfiguration> feature
    ) {
        register(context, key, feature, FeatureConfiguration.NONE);
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(
            BootstrapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration
    ) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
