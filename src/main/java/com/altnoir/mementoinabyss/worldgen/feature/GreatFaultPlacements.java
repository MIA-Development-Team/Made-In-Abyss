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
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public final class GreatFaultPlacements {
    public static final ResourceKey<PlacedFeature> CAERULITE_GEODE = ResourceKey.create(
            Registries.PLACED_FEATURE, MementoInAbyss.asResource("great_fault/caerulite_geode"));

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> features = context.lookup(Registries.CONFIGURED_FEATURE);
        Holder<ConfiguredFeature<?, ?>> geode = features.getOrThrow(GreatFaultFeatures.CAERULITE_GEODE);
        context.register(CAERULITE_GEODE, new PlacedFeature(geode, java.util.List.of(
                RarityFilter.onAverageOnceEvery(24),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(16), VerticalAnchor.belowTop(16)),
                BiomeFilter.biome())));
    }

    private GreatFaultPlacements() {}
}
