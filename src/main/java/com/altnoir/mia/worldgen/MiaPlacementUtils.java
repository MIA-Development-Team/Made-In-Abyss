package com.altnoir.mia.worldgen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.util.MiaUtil;
import com.altnoir.mia.worldgen.biome.abyss_brink.AbyssBrinkPlacements;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.List;

public class MiaPlacementUtils {
    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        AbyssBrinkPlacements.bootstrap(context);
    }

    public static ResourceKey<PlacedFeature> resourceKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, MiaUtil.id(MIA.MOD_ID, name));
    }
    public static void register(
            BootstrapContext<PlacedFeature> context,
            ResourceKey<PlacedFeature> key,
            Holder<ConfiguredFeature<?, ?>> configuration,
            List<PlacementModifier> modifiers
    ) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
    public static void register(
            BootstrapContext<PlacedFeature> context,
            ResourceKey<PlacedFeature> key,
            Holder<ConfiguredFeature<?, ?>> configuration,
            PlacementModifier... modifiers
    ) {
        register(context, key, configuration, List.of(modifiers));
    }
}
