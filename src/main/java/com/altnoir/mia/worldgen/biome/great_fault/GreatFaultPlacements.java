package com.altnoir.mia.worldgen.biome.great_fault;

import com.altnoir.mia.worldgen.MiaPlacementUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;

public class GreatFaultPlacements {
    public static final ResourceKey<PlacedFeature> ABYSS_LIGHT = greatFaultKey("abyss_light");

    private static final PlacementModifier GREAT_FAULT_HEIGHT = HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(16), VerticalAnchor.belowTop(16));

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> holdergetter = context.lookup(Registries.CONFIGURED_FEATURE);

        Holder<ConfiguredFeature<?, ?>> abyss_light = holdergetter.getOrThrow(GreatFaultFeatures.ABYSS_LIGHT);

        MiaPlacementUtils.register(
                context, ABYSS_LIGHT, abyss_light,
                CountPlacement.of(250),
                CountPlacement.of(16),
                InSquarePlacement.spread(),
                GREAT_FAULT_HEIGHT,
                BiomeFilter.biome()
        );
    }

    private static ResourceKey<PlacedFeature> greatFaultKey(String name) {
        return MiaPlacementUtils.resourceKey("great_fault/" + name);
    }
}
