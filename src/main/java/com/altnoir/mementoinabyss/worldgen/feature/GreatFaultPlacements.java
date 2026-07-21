package com.altnoir.mementoinabyss.worldgen.feature;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class GreatFaultPlacements {
    public static final ResourceKey<PlacedFeature> ABYSS_LIGHT = ResourceKey.create(
            Registries.PLACED_FEATURE, MementoInAbyss.asResource("great_fault/abyss_light"));
    public static final ResourceKey<PlacedFeature> CAERULITE_GEODE = ResourceKey.create(
            Registries.PLACED_FEATURE, MementoInAbyss.asResource("great_fault/caerulite_geode"));

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> features = context.lookup(Registries.CONFIGURED_FEATURE);
        Holder<ConfiguredFeature<?, ?>> light = features.getOrThrow(GreatFaultFeatures.ABYSS_LIGHT);
        context.register(ABYSS_LIGHT, new PlacedFeature(light, java.util.List.of(
                CountPlacement.of(250),
                CountPlacement.of(16),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(16), VerticalAnchor.belowTop(16)),
                BiomeFilter.biome())));
        Holder<ConfiguredFeature<?, ?>> geode = features.getOrThrow(GreatFaultFeatures.CAERULITE_GEODE);
        context.register(CAERULITE_GEODE, new PlacedFeature(geode, java.util.List.of(
                net.minecraft.world.level.levelgen.placement.RarityFilter.onAverageOnceEvery(24),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(16), VerticalAnchor.belowTop(16)),
                BiomeFilter.biome())));
    }

    private GreatFaultPlacements() {}
}
